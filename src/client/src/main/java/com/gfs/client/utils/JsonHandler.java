package com.gfs.client.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonHandler {

    static ObjectMapper objectMapper = new ObjectMapper();

    public static String convertObjectToString(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    public static <T> T convertStringToObject(String stringValue, Class<T> className) throws JacksonException {
        return objectMapper.readValue(stringValue, className);
    }
}
