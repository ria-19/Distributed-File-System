package com.gfs.chunkserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientWriteRequest {
    private String chunkHandle;
    private String data;
}
