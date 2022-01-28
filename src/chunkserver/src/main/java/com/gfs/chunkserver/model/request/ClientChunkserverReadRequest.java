package com.gfs.chunkserver.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientChunkserverReadRequest{
    private String chunkHandle;
}
