package com.gfs.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * created by nikunjagarwal on 04-02-2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MasterClientResponse {
    private String filename;
    private Integer offset;
    private ChunkMetadata chunkMetadata;
}
