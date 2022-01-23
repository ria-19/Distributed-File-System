package com.gfs.master.model.request;


import com.gfs.master.model.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * created by nikunjagarwal on 23-01-2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChunkServerChunkMetadata {
    private String chunkHandle;
    private Location location;
}
