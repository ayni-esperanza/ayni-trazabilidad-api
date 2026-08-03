package com.trazabilidad.ayni.shared.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class AuditLogAspectTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuditLogAspect aspect = new AuditLogAspect(objectMapper);
    private final Logger logger = (Logger) LoggerFactory.getLogger(AuditLogAspect.class);
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        logger.detachAppender(appender);
        appender.stop();
    }

    @Test
    void registraEventoOkConUsuarioEIdDelResultado() throws Throwable {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("ana", "no-se-registra", java.util.List.of()));
        Method method = ServicioEjemplo.class.getMethod("actualizar", Long.class);
        ProceedingJoinPoint joinPoint = joinPoint(method, new Object[] { 42L }, new ResultadoEjemplo(99L), null);

        Object resultado = aspect.auditar(joinPoint, method.getAnnotation(Auditable.class));

        assertEquals(99L, ((ResultadoEjemplo) resultado).getId());
        JsonNode evento = ultimoEvento();
        assertEquals("AUDIT", evento.get("tipo").asText());
        assertEquals("ana", evento.get("usuario").asText());
        assertEquals("EDITAR", evento.get("accion").asText());
        assertEquals("Pedido", evento.get("entidad").asText());
        assertEquals("ServicioEjemplo.actualizar", evento.get("metodo").asText());
        assertEquals("OK", evento.get("resultado").asText());
        assertEquals(99L, evento.get("entidadId").asLong());
    }

    @Test
    void registraErrorRedactadoConIdDelArgumentoYRelanzaExcepcion() throws Throwable {
        Method method = ServicioEjemplo.class.getMethod("eliminar", Long.class);
        IllegalStateException error = new IllegalStateException(
                "Authorization=Bearer.token.secreto password=clave-super-secreta");
        ProceedingJoinPoint joinPoint = joinPoint(method, new Object[] { 77L }, null, error);

        IllegalStateException lanzada = assertThrows(IllegalStateException.class,
                () -> aspect.auditar(joinPoint, method.getAnnotation(Auditable.class)));

        assertSame(error, lanzada);
        JsonNode evento = ultimoEvento();
        assertEquals("sistema", evento.get("usuario").asText());
        assertEquals("ERROR", evento.get("resultado").asText());
        assertEquals(77L, evento.get("entidadId").asLong());
        assertFalse(evento.get("mensajeError").asText().contains("Bearer.token.secreto"));
        assertFalse(evento.get("mensajeError").asText().contains("clave-super-secreta"));
    }

    private ProceedingJoinPoint joinPoint(Method method, Object[] args, Object resultado, Throwable error) throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getTarget()).thenReturn(new ServicioEjemplo());
        when(joinPoint.getArgs()).thenReturn(args);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getName()).thenReturn(method.getName());
        if (error == null) {
            when(joinPoint.proceed()).thenReturn(resultado);
        } else {
            when(joinPoint.proceed()).thenThrow(error);
        }
        return joinPoint;
    }

    private JsonNode ultimoEvento() throws Exception {
        return objectMapper.readTree(appender.list.get(appender.list.size() - 1).getFormattedMessage());
    }

    static class ServicioEjemplo {
        @Auditable(accion = "EDITAR", entidad = "Pedido")
        public ResultadoEjemplo actualizar(Long id) {
            return null;
        }

        @Auditable(accion = "ELIMINAR", entidad = "Producto")
        public void eliminar(Long id) {
        }
    }

    static class ResultadoEjemplo {
        private final Long id;

        ResultadoEjemplo(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }
}