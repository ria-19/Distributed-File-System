package com.gfs.master.service;

import com.gfs.master.model.*;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * created by nikunjagarwal on 21-01-2022
 * This service maintains an in memory metadata and all tasks related to it
 */
@Component
public class MetadataServiceImpl {
    private static MetadataServiceImpl instance;
    private HashMap<String, File> fileMap; // <filename,File>
    private HashMap<Integer, ChunkMetadata> chunkMap; // <chunkhandle, ChunkMetadata>

    private MetadataServiceImpl(){
        this.fileMap = new HashMap<>();
        this.chunkMap = new HashMap<>();
    }

    /**
     * Implement singleton object pattern to have one common object
     * @return MetadataServiceImpl
     */
    public synchronized static MetadataServiceImpl getInstance(){
        if(instance == null){
            instance = new MetadataServiceImpl();
        }
        return instance;
    }

    /**
     * This method returns chunk metadata to the client servers
     *
     * @param filename: name of the file to be fetched
     * @param offset: offset of the file
     * @return ChunkMetaadataResponse: chunkmetadata for the given file and offset
     */
    public ChunkMetadataResponse getChunkMetadataMetadata(String filename, Integer offset) {
        Integer chunkHandle = getChunkHandle(filename, offset);
        if(chunkHandle == null) {
            return null;
        }
        ChunkMetadataResponse chunkMetaadataResponse = new ChunkMetadataResponse();
        chunkMetaadataResponse.builder().filename(filename).offset(offset).chunkMetadata(this.chunkMap.get(chunkHandle)).build();
        return chunkMetaadataResponse;
    }

    /**
     * Update chunks metadata received from the chunkservers
     * @param chunkServerChunkMetadataList: request containing metadata received from chunk servers
     */
    public synchronized void updateChunkMetadata(ArrayList<ChunkServerChunkMetadata>  chunkServerChunkMetadataList) {
        for(ChunkServerChunkMetadata chunkServerChunkMetadata : chunkServerChunkMetadataList) {
            Integer chunkHandle = chunkServerChunkMetadata.getChunkHandle();
            ChunkMetadata chunkMetadata = this.chunkMap.get(chunkHandle);
            HashMap<String, Location> chunklocationMap  = chunkMetadata.getLocations();
            if(chunklocationMap == null){
                chunklocationMap = new HashMap<>();
            }
            chunklocationMap.put(chunkServerChunkMetadata.getLocation().getChunkserverUrl(), chunkServerChunkMetadata.getLocation());
            chunkMetadata.setLocations(chunklocationMap);
            this.chunkMap.put(chunkHandle, chunkMetadata);
        }
    }

    /**
     * fetches chunk handle from the filename, offset
     * @param filename: name of the file to be fetched
     * @param offset: offset of the file
     * @return integer: chunk handle for the given filename offset
     */
    private Integer getChunkHandle(String filename, Integer offset) {
        File file = this.fileMap.get(filename);
        if(file == null)
            return null;
        Integer chunkHandle = file.getOffsetChunkHandleMap().get(offset);
        return chunkHandle;
    }

    /**
     * Creates metadata for a newly created file
     * @param filename: name of the new file
     * @param offsets: list of offsets for the given file
     * @return ArrayList<ChunkMetadataResponse> : list of newly generated chunkhandles for fiename and offset
     */
    public ArrayList<ChunkMetadataResponse> updateNewFileMetadata(String filename, ArrayList<Integer> offsets) {
        // TODO : to be revisited while implementing write mechanism
        File file = new File(filename, new HashMap<>());
        HashMap<Integer, Integer> offsetChunkHandles = new HashMap<>();
        ArrayList<ChunkMetadataResponse> chunkMetadataResponseList = new ArrayList<>();
        for(Integer offset : offsets) {
            Integer chunkHandle = generateNewChunkHandle(filename, offset);
            offsetChunkHandles.put(offset, chunkHandle);
            ChunkMetadata chunkMetadata = new ChunkMetadata();
            chunkMetadata.builder().chunkHandle(chunkHandle);
            ChunkMetadataResponse chunkMetadataResponse = new ChunkMetadataResponse(filename, offset, chunkMetadata);
            chunkMetadataResponseList.add(chunkMetadataResponse);
        }
        return chunkMetadataResponseList;
    }

    /**
     * generate new unique chunk handle from the given filename, offset
     * @param filename:
     * @param offset:
     * @return Integer: newly created chunkhandle
     */
    private Integer generateNewChunkHandle(String filename, Integer offset) {
        // TODO : to implement generation of chunkHandle from filename and offset
        return 1;
    }

}
