package com.gfs.client.model;

import lombok.Data;

/**
 * created by nikunjagarwal on 27-01-2022
 */
@Data
public class Response <T>{
    private ResponseStatus responseStatus;
    private T data;
}
