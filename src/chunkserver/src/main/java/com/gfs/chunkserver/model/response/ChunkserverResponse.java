package com.gfs.chunkserver.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * created by nikunjagarwal on 28-01-2022
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChunkserverResponse <T>{
    private ResponseStatus responseStatus;
    private T data;
}
