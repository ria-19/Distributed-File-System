package com.gfs.chunkserver.service;

import com.gfs.chunkserver.model.ChunkCacheData;

import java.util.HashMap;

/**
 * created by nikunjagarwal on 27-01-2022
 * This service stores the client write requests in a map
 */
public class ChunkCacheService {
    private static ChunkCacheService instance;
    private HashMap<String, ChunkCacheData> chunkCache; // <chunkhandle,chunkData>

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

    public synchronized void insertIntoChunkCache(String chunkHandle, ChunkCacheData chunkCacheData){
        chunkCache.put(chunkHandle,chunkCacheData);
    }

    public ChunkCacheData getChunkDataFromCache(String chunkHandle) {
        return chunkCache.get(chunkHandle);
    }

    public synchronized void deleteFromChunkCache(String chunkHandle){
        chunkCache.remove(chunkHandle);
    }
}
