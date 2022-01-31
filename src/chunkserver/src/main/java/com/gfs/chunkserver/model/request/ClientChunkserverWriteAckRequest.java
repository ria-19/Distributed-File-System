package com.gfs.chunkserver.model.request;

import com.gfs.chunkserver.model.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

/**
 * created by nikunjagarwal on 28-01-2022
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ClientChunkserverWriteAckRequest {
    private String chunkHandle;
    ArrayList<Location> locations;
}
