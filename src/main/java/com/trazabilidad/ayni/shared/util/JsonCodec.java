package com.trazabilidad.ayni.shared.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JsonCodec {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private JsonCodec() {
    }

    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception ex) {
            return null;
        }
    }

    public static <T> T fromJson(String rawJson, Class<T> clazz, T defaultValue) {
        if (rawJson == null || rawJson.isBlank()) {
            return defaultValue;
        }

        try {
            return MAPPER.readValue(rawJson, clazz);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public static <T> T fromJson(String rawJson, TypeReference<T> type, T defaultValue) {
        if (rawJson == null || rawJson.isBlank()) {
            return defaultValue;
        }

        try {
            return MAPPER.readValue(rawJson, type);
        } catch (Exception ex) {
            return defaultValue;
        }
    }
}
