package com.trazabilidad.ayni.shared.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.Normalizer;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Emite eventos JSON de auditoria sin registrar argumentos ni datos de peticiones. */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class AuditLogAspect {

    private static final int MAX_ERROR_LENGTH = 500;
    private static final Pattern SENSITIVE_ASSIGNMENT = Pattern.compile(
            "(?i)\\b(password|passwd|contrasena|secret|token|jwt|authorization|cookie)\\b\\s*[:=]\\s*([^\\s,;]+)");
    private static final Pattern BEARER_TOKEN = Pattern.compile("(?i)Bearer\\s+[^\\s,;]+");
    private static final Pattern JWT_VALUE = Pattern.compile("\\b[A-Za-z0-9_-]{8,}\\.[A-Za-z0-9_-]{8,}\\.[A-Za-z0-9_-]{8,}\\b");

    private final ObjectMapper objectMapper;

    @Around("@annotation(auditable)")
    public Object auditar(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long inicio = System.nanoTime();
        try {
            Object resultado = joinPoint.proceed();
            registrarEvento(joinPoint, auditable, "OK", inicio, resolverEntidadId(joinPoint, auditable, resultado), null);
            return resultado;
        } catch (Throwable error) {
            registrarEvento(joinPoint, auditable, "ERROR", inicio, resolverEntidadId(joinPoint, auditable, null), error);
            throw error;
        }
    }

    private void registrarEvento(ProceedingJoinPoint joinPoint, Auditable auditable, String resultado,
            long inicio, Object entidadId, Throwable error) {
        try {
            Map<String, Object> evento = new LinkedHashMap<>();
            evento.put("tipo", "AUDIT");
            evento.put("fecha", Instant.now().toString());
            evento.put("usuario", resolverUsuario());
            evento.put("accion", auditable.accion());
            evento.put("entidad", auditable.entidad());
            evento.put("metodo", resolverMetodo(joinPoint));
            evento.put("resultado", resultado);
            evento.put("duracionMs", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicio));
            if (entidadId != null) {
                evento.put("entidadId", entidadId);
            }
            if (error != null) {
                evento.put("mensajeError", sanitizarMensajeError(error));
            }
            log.info("{}", objectMapper.writeValueAsString(evento));
        } catch (JsonProcessingException | RuntimeException loggingError) {
            log.warn("No se pudo serializar un evento AUDIT: {}", loggingError.getClass().getSimpleName());
        }
    }

    private String resolverUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return "sistema";
        }
        if (!authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "anonimo";
        }
        String nombre = authentication.getName();
        return nombre == null || nombre.isBlank() ? "anonimo" : nombre;
    }

    private String resolverMetodo(ProceedingJoinPoint joinPoint) {
        String clase = joinPoint.getTarget() == null
                ? ((MethodSignature) joinPoint.getSignature()).getDeclaringType().getSimpleName()
                : joinPoint.getTarget().getClass().getSimpleName();
        return clase + "." + joinPoint.getSignature().getName();
    }

    private Object resolverEntidadId(ProceedingJoinPoint joinPoint, Auditable auditable, Object resultado) {
        Object idResultado = extraerIdResultado(desenvolverResultado(resultado));
        return idResultado != null ? idResultado : extraerIdArgumentos(joinPoint, auditable.entidad());
    }

    private Object desenvolverResultado(Object resultado) {
        if (resultado instanceof ResponseEntity<?> responseEntity) {
            return desenvolverResultado(responseEntity.getBody());
        }
        if (resultado instanceof Optional<?> optional) {
            return optional.map(this::desenvolverResultado).orElse(null);
        }
        return resultado;
    }

    private Object extraerIdResultado(Object resultado) {
        if (resultado == null) {
            return null;
        }
        try {
            Method getId = resultado.getClass().getMethod("getId");
            if (getId.getParameterCount() != 0) {
                return null;
            }
            return idSeguro(getId.invoke(resultado));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private Object extraerIdArgumentos(ProceedingJoinPoint joinPoint, String entidad) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] argumentos = joinPoint.getArgs();
        String nombreEntidad = normalizarNombre(entidad);
        Object mejorId = null;
        int mejorPuntaje = 0;

        for (int index = 0; index < Math.min(parameters.length, argumentos.length); index++) {
            String nombreParametro = normalizarNombre(parameters[index].getName());
            int puntaje = puntajeParametroId(nombreParametro, nombreEntidad);
            Object candidato = idSeguro(argumentos[index]);
            if (puntaje > mejorPuntaje && candidato != null) {
                mejorId = candidato;
                mejorPuntaje = puntaje;
            }
        }
        return mejorId;
    }

    private int puntajeParametroId(String parametro, String entidad) {
        if (parametro.equals(entidad + "id")) {
            return 3;
        }
        if (parametro.equals("id")) {
            return 2;
        }
        return parametro.endsWith("id") ? 1 : 0;
    }

    private Object idSeguro(Object valor) {
        if (valor instanceof Number || valor instanceof UUID) {
            return valor;
        }
        if (valor instanceof String texto && !texto.isBlank() && texto.length() <= 128) {
            return texto;
        }
        return null;
    }

    private String normalizarNombre(String valor) {
        if (valor == null) {
            return "";
        }
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9]", "")
                .toLowerCase();
    }

    private String sanitizarMensajeError(Throwable error) {
        String mensaje = error.getMessage();
        if (mensaje == null || mensaje.isBlank()) {
            mensaje = error.getClass().getSimpleName();
        }
        mensaje = mensaje.replace('\r', ' ').replace('\n', ' ');
        mensaje = SENSITIVE_ASSIGNMENT.matcher(mensaje).replaceAll("$1=[REDACTED]");
        mensaje = BEARER_TOKEN.matcher(mensaje).replaceAll("Bearer [REDACTED]");
        mensaje = JWT_VALUE.matcher(mensaje).replaceAll("[REDACTED_JWT]");
        return mensaje.length() <= MAX_ERROR_LENGTH ? mensaje : mensaje.substring(0, MAX_ERROR_LENGTH);
    }
}