package com.gfs.master.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

/**
 * created by nikunjagarwal on 21-01-2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChunkServerRequest extends ServerRequest{
    private boolean containsChunksMetadata;
    private ArrayList<ChunkServerChunkMetadata> clientChunksMetadata;
}
