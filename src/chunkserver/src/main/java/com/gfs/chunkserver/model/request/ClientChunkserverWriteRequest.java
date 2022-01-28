package com.gfs.chunkserver.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientChunkserverWriteRequest {
    private String chunkHandle;
    private String chunkPath;
    private String data;
}
