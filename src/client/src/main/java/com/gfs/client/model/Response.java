package com.gfs.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * created by nikunjagarwal on 27-01-2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response <T>{
    private ResponseStatus responseStatus;
    private T data;
}
