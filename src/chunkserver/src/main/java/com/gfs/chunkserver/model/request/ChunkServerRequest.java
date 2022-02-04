package com.gfs.chunkserver.model.request;

import com.gfs.chunkserver.model.ChunkServerChunkMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

/**
 * created by nikunjagarwal on 19-01-2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChunkServerRequest{
    private String chunkServerUrl;
    private boolean containsChunksMetadata;
    private ArrayList<ChunkServerChunkMetadata> chunkServerChunkMetadataList;
}
