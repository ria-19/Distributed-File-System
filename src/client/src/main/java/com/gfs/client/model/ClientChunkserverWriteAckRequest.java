package com.gfs.client.model;

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
public class ClientChunkserverWriteAckRequest extends ClientRequest{
    private String chunkHandle;
    ArrayList<Location> locations;
}
