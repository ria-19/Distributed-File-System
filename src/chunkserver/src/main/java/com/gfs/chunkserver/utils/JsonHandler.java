package com.gfs.chunkserver.utils;

/**
 * created by nikunjagarwal on 19-01-2022
 */

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * created by nikunjagarwal on 19-01-2022
 */
public class JsonHandler {

    static ObjectMapper objectMapper = new ObjectMapper();

    public static String convertObjectToString(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    public static <T> T convertStringToObject(String stringValue, Class<T> className) throws JacksonException {
        return objectMapper.readValue(stringValue, className);
    }
}