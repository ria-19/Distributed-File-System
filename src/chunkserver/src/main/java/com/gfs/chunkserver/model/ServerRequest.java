package com.gfs.chunkserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * created by nikunjagarwal on 19-01-2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerRequest <T>{
    private final String source = "CHUNKSERVER";
    private T request;
}
