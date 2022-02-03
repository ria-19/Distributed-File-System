package com.gfs.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * created by nikunjagarwal on 04-02-2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChunkMetadata {
    private String chunkHandle;
    private ArrayList<Location> locations;
    private String leaseServer;
}
