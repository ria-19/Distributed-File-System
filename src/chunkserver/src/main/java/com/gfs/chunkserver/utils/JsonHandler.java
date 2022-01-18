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

    ObjectMapper objectMapper = new ObjectMapper();

    public String convertObjectToString(Object object) throws JsonProcessingException {
        String finalString = objectMapper.writeValueAsString(object);
        return finalString;
    }

    public <T> T a(String stringValue, Class<T> className) throws JacksonException {
        T object = objectMapper.readValue(stringValue, className);
        return object;
    }
}