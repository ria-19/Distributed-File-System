package com.gfs.master.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

/**
 * created by nikunjagarwal on 22-01-2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChunkMetadata {
    private int chunkHandle;
    // ChunkserverUrl, Location
    private HashMap<String, Location> locations;
    private String leaseServer;
}
