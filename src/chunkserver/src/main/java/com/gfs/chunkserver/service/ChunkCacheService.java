package com.gfs.chunkserver.service;

import java.util.HashMap;

/**
 * created by nikunjagarwal on 27-01-2022
 */
public class ChunkCacheService {
    private static ChunkCacheService instance;
    private HashMap<String, String> chunkCache; // <chunkhandle,chunkData>

    private ChunkCacheService(){
        chunkCache = new HashMap<>();
    }

    /**
     * Implement singleton object pattern to have one common object
     * @return ChunkCacheService
     */
    public synchronized static ChunkCacheService getInstance(){
        if(instance == null){
            instance = new ChunkCacheService();
        }
        return instance;
    }

    public synchronized void insertIntoChunkCache(String chunkHandle, String chunkData){
        chunkCache.put(chunkHandle,chunkData);
    }

    public String getChunkDataFromCache(String chunkHandle) {
        return chunkCache.get(chunkHandle);
    }
}
