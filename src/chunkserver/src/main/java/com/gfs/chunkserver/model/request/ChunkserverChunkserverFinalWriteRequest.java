package com.gfs.chunkserver.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * created by nikunjagarwal on 28-01-2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChunkserverChunkserverFinalWriteRequest {
    private String chunkHandle;
}
