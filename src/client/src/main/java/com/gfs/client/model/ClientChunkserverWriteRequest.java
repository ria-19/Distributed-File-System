package com.gfs.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientChunkserverWriteRequest {
    private String chunkHandle;
    private String data;
}
