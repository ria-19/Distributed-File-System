package com.gfs.master.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * created by nikunjagarwal on 23-01-2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChunkMetaadataResponse {
    private String filename;
    private Integer offset;
    private ChunkMetadata chunkMetadata;
}
