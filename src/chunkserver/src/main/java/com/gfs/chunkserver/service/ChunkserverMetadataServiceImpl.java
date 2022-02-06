package com.gfs.chunkserver.service;

import com.gfs.chunkserver.model.ChunkServerChunkMetadata;
import com.gfs.chunkserver.model.Location;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * created by nikunjagarwal on 28-01-2022
 * This class maintains the metadata of the chunks present in this chunkserver
 * This metadata is used to update master via heartbeat
 */
public class ChunkserverMetadataServiceImpl {

    private static ChunkserverMetadataServiceImpl instance;
    public HashMap<String, ChunkServerChunkMetadata> chunkMetadataMap;
    @Value("${chunkserver.host}")
    private String chunkServerHostString;
    @Value("${chunkserver.port}")
    private int chunkServerPort;

    private ChunkserverMetadataServiceImpl(){
        this.chunkMetadataMap = new HashMap<>();
    }

    public synchronized static ChunkserverMetadataServiceImpl getInstance(){
        if(instance == null){
            instance = new ChunkserverMetadataServiceImpl();
        }
        return instance;
    }


    /**
     * fetches latest chunkmetadata for active chunks in the chunkserver
     * @return ArrayList<ChunkServerChunkMetadata> : latest chunks metadata
     */
    public ArrayList<ChunkServerChunkMetadata> fetchChunkMetadata() {
        ArrayList<ChunkServerChunkMetadata> chunkMetadata = new ArrayList<>();
        chunkMetadataMap.forEach((chunkhandle, chunkServerChunkMetadata) -> chunkMetadata.add(chunkServerChunkMetadata));
        return chunkMetadata;
    }

    /**
     * inserts newly created chunk in the chunkCache
     * @param chunkHandle : chunkhandle of new chunk to be inserted
     */
    public synchronized void insertChunkMetadata(String chunkHandle) {
        ChunkServerChunkMetadata chunkMetadata = new ChunkServerChunkMetadata();
        Location location = new Location(chunkServerHostString + ":" + chunkServerPort, 1);
        chunkMetadata.setChunkHandle(chunkHandle);
        chunkMetadata.setLocation(location);
        chunkMetadataMap.put(chunkHandle, chunkMetadata);
    }


    /**
     * Updates the version of chunk
     * @param chunkHandle : chunkhandle of chunk to be updated
     */
    public synchronized void updateChunkMetadata(String chunkHandle) {
        ChunkServerChunkMetadata chunkMetadata = chunkMetadataMap.get(chunkHandle);
        Location location = chunkMetadata.getLocation();
        location.setVersionNo(location.getVersionNo()+1);
        chunkMetadata.setLocation(location);
        chunkMetadataMap.put(chunkHandle, chunkMetadata);
    }
}
