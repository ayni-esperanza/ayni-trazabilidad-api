package com.trazabilidad.ayni.shared.util;

/**
 * Constantes globales de la aplicación.
 */
public final class Constants {

    // Constructor privado para prevenir instanciación (Utility class pattern)
    private Constants() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no puede ser instanciada");
    }

    /**
     * Roles del sistema
     * Deben coincidir con los datos iniciales en la base de datos
     */
    public static final class Roles {
        private Roles() {
        }

        public static final String ADMINISTRADOR = "ADMINISTRADOR";
        public static final String GERENTE = "GERENTE";
        public static final String INGENIERO = "INGENIERO";
        public static final String ASISTENTE = "ASISTENTE";
        public static final String AYUDANTE = "AYUDANTE";
    }

    /**
     * Módulos del sistema para permisos
     */
    public static final class Modulos {
        private Modulos() {
        }

        public static final String USUARIOS = "usuarios";
        public static final String ROLES = "roles";
        public static final String PERMISOS = "permisos";
        public static final String SOLICITUDES = "solicitudes";
        public static final String PROCESOS = "procesos";
        public static final String TAREAS = "tareas";
        public static final String EVIDENCIAS = "evidencias";
        public static final String TABLERO = "tablero";
        public static final String ESTADISTICAS = "estadisticas";
        public static final String REPORTES = "reportes";
        public static final String CONFIGURACION = "configuracion";
        public static final String AUDITORIA = "auditoria";
    }

    /**
     * Acciones para permisos
     */
    public static final class Acciones {
        private Acciones() {
        }

        public static final String CREAR = "crear";
        public static final String LEER = "leer";
        public static final String ACTUALIZAR = "actualizar";
        public static final String ELIMINAR = "eliminar";
        public static final String EXPORTAR = "exportar";
        public static final String IMPORTAR = "importar";
    }

    /**
     * Configuración JWT
     */
    public static final class Security {
        private Security() {
        }

        public static final String TOKEN_PREFIX = "Bearer ";
        public static final String HEADER_STRING = "Authorization";
        public static final String AUTHORITIES_KEY = "authorities";
    }

    /**
     * Mensajes de error comunes
     */
    public static final class ErrorMessages {
        private ErrorMessages() {
        }

        public static final String ENTITY_NOT_FOUND = "La entidad solicitada no fue encontrada";
        public static final String DUPLICATE_ENTITY = "Ya existe una entidad con esos datos";
        public static final String UNAUTHORIZED = "No tiene autorización para realizar esta acción";
        public static final String INVALID_CREDENTIALS = "Credenciales inválidas";
        public static final String INACTIVE_USER = "El usuario está inactivo";
        public static final String VALIDATION_ERROR = "Error de validación en los datos proporcionados";
    }

    /**
     * Configuración de paginación por defecto
     */
    public static final class Pagination {
        private Pagination() {
        }

        public static final int DEFAULT_PAGE = 0;
        public static final int DEFAULT_SIZE = 100;
        public static final int MAX_SIZE = 1000;
        public static final String DEFAULT_SORT = "id";
    }

    /**
     * Configuración de archivos
     */
    public static final class Files {
        private Files() {
        }

        public static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB en bytes
        public static final String[] ALLOWED_IMAGE_TYPES = { "image/jpeg", "image/jpg", "image/png", "image/gif" };
    }
}
