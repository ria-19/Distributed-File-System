package com.gfs.master.service;

import com.gfs.master.model.*;
import com.gfs.master.model.request.ChunkServerChunkMetadata;
import com.gfs.master.model.response.MasterClientMetadataResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * created by nikunjagarwal on 21-01-2022
 * This service maintains an in memory metadata and all tasks related to it
 */
@Slf4j
@Component
public class MetadataServiceImpl {
    private static MetadataServiceImpl instance;
    private HashMap<String, File> fileMap; // <filename,File>
    private HashMap<String, ChunkMetadata> chunkMap; // <chunkhandle, ChunkMetadata>

    private MetadataServiceImpl(){
        this.fileMap = new HashMap<>();
        this.chunkMap = new HashMap<>();
        setMockDataDuringIntitalization(); // TODO : to be removed later
        log.info("Filemap={}, Chunkmap={}", fileMap, chunkMap);
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
    public MasterClientMetadataResponse getChunkMetadata(String filename, Integer offset) {
        log.info("Inside getChunkMetadataMetadata() with filename={}, offset={}", filename, offset);
        String chunkHandle = getChunkHandle(filename, offset);
        if(chunkHandle == null) {
            return null;
        }
        MasterClientMetadataResponse chunkMetadataResponse = MasterClientMetadataResponse.builder().filename(filename).offset(offset).chunkMetadata(this.chunkMap.get(chunkHandle)).build();
        log.info("Fetched chunk metadata={}", chunkMetadataResponse);
        return chunkMetadataResponse;
    }

    /**
     * Update chunks metadata received from the chunkservers
     * @param chunkServerChunkMetadataList: request containing metadata received from chunk servers
     */
    public synchronized void updateChunkMetadata(ArrayList<ChunkServerChunkMetadata>  chunkServerChunkMetadataList) {
        log.info("Inside updateChunkMetadata() with chunkServerChunkMetadata={}", chunkServerChunkMetadataList);
        for(ChunkServerChunkMetadata chunkServerChunkMetadata : chunkServerChunkMetadataList) {
            String chunkHandle = chunkServerChunkMetadata.getChunkHandle();
            ChunkMetadata chunkMetadata = this.chunkMap.get(chunkHandle);
            if(chunkMetadata == null){
                log.error("Chunk not found for chunk handle : {}", chunkHandle);
            }
            HashMap<String, Location> chunklocationMap  = chunkMetadata.getLocations();
            if(chunklocationMap == null){
                chunklocationMap = new HashMap<>();
            }
            chunklocationMap.put(chunkServerChunkMetadata.getLocation().getChunkserverUrl(), chunkServerChunkMetadata.getLocation());
            chunkMetadata.setLocations(chunklocationMap);
            this.chunkMap.put(chunkHandle, chunkMetadata);
        }
        log.info("Filemap after update:{}", fileMap);
        log.info("Chunkmap after update: {}", chunkMap);
    }

    /**
     * fetches chunk handle from the filename, offset
     * @param filename: name of the file to be fetched
     * @param offset: offset of the file
     * @return integer: chunk handle for the given filename offset
     */
    private String getChunkHandle(String filename, Integer offset) {
        File file = this.fileMap.get(filename);
        log.info("Fetched file={}", file);
        if(file == null)
            return null;
        return file.getOffsetChunkHandleMap().get(offset);
    }

    /**
     * Creates metadata for a newly created file
     * @param filename: name of the new file
     * @param offsets: list of offsets for the given file
     * @return ArrayList<ChunkMetadataResponse> : list of newly generated chunkhandles for fiename and offset
     */
    public ArrayList<MasterClientMetadataResponse> updateNewFilesMetadata(String filename, ArrayList<Integer> offsets) {
        // TODO : to be revisited while implementing write mechanism
        File file = new File(filename, new HashMap<>());
        HashMap<Integer, String> offsetChunkHandles = new HashMap<>();
        ArrayList<MasterClientMetadataResponse> chunkMetadataResponseList = new ArrayList<>();
        for(Integer offset : offsets) {
            String chunkHandle = generateNewChunkHandle();
            offsetChunkHandles.put(offset, chunkHandle);
            ChunkMetadata chunkMetadata = new ChunkMetadata();
            chunkMetadata.setChunkHandle(chunkHandle);
            MasterClientMetadataResponse chunkMetadataResponse = new MasterClientMetadataResponse(filename, offset, chunkMetadata);
            chunkMetadataResponseList.add(chunkMetadataResponse);
        }
        return chunkMetadataResponseList;
    }

    /**
     * Creates metadata for a newly created file
     * @param filename: name of the new file
     * @param offset: a single offset for the given file
     * @return ChunkMetadataResponse : newly generated chunkhandle for fiename and offset
     */
    public MasterClientMetadataResponse fetchNewFileMetadata(String filename, Integer offset) {
        String chunkHandle = generateNewChunkHandle();
        HashMap<String, Location> chunkserverLocations = fetchRandomChunkserverLocations();
        String leaseServer = getRandomLeaseChunkserver(chunkserverLocations);
        ChunkMetadata chunkMetadata = new ChunkMetadata(chunkHandle, chunkserverLocations, leaseServer);
        return new MasterClientMetadataResponse(filename, offset, chunkMetadata);
    }

    /**
     * generate new unique chunk handle from the given filename, offset
     * @return Integer: newly created chunkhandle
     */
    private String generateNewChunkHandle() {
        return UUID.randomUUID().toString();
    }

    //TODO: to be removed later
    private void setMockDataDuringIntitalization(){
        HashMap<Integer, String > offsetMap =  new HashMap<>();
        offsetMap.put(1, "12345");
        File file = new File("mock-file-1", offsetMap);
        fileMap.put("mock-file-1", file);
        chunkMap.put("12345",new ChunkMetadata("12345", new HashMap<>(), ""));
    }

    private HashMap<String, Location> fetchRandomChunkserverLocations() {
        HashMap<String, Location> randomChunkservers = new HashMap<>();
        ArrayList<String> activeChunkservers = HeartbeatServiceImpl.fetchActiveChunkservers();
        for(int i = 0; i < Math.min(activeChunkservers.size(), 3); i++ ){
            Location chunkserverLocation = new Location(activeChunkservers.get(i), null, 1);
            randomChunkservers.put(chunkserverLocation.getChunkserverUrl(), chunkserverLocation);
        }
        return randomChunkservers;
    }

    private String getRandomLeaseChunkserver(HashMap<String, Location> locations) {
        if(locations.isEmpty())
            return null;
        return locations.entrySet().iterator().next().getKey();
    }

}
