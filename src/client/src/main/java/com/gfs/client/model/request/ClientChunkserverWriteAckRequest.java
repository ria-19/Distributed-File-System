package com.gfs.client.model.request;

import com.gfs.client.model.Location;
import lombok.*;

import java.util.ArrayList;

/**
 * created by nikunjagarwal on 28-01-2022
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ClientChunkserverWriteAckRequest extends ClientRequest {
    private String chunkHandle;
    ArrayList<Location> secondaryCSLocations;
}
