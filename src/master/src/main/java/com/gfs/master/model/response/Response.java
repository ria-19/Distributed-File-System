package com.gfs.master.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * created by nikunjagarwal on 04-02-2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response <T>{
    private ResponseStatus responseStatus;
    private T data;
}
