package com.gfs.master.model.response;

import com.gfs.master.model.ChunkMetadata;
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
public class MasterClientMetadataResponse {
    private String filename;
    private Integer offset;
    private ChunkMetadata chunkMetadata;
}
