package com.gfs.master.service;

import com.gfs.master.Constants;
import com.gfs.master.model.request.ChunkServerChunkMetadata;
import com.gfs.master.model.request.ChunkServerRequest;
import com.gfs.master.model.Location;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * created by nikunjagarwal on 16-01-2022
 */
@Component
@Slf4j
@EnableScheduling
public class HeartbeatServiceImpl {
    static HashMap<String, Date> lastHeartBeatTimeOfServers;

    public HeartbeatServiceImpl(){
        lastHeartBeatTimeOfServers = new HashMap<>();
    }

    /**
     * Scheduled function which iterates over all heartbeats received and
     * removes the dead servers
     */
    @Scheduled(fixedDelay = Constants.heartBeatCheckTimeInterval)
    public static void checkLiveChunkServers() {
        log.info("Checking Live ChunkServers");
        long currentTimeInMillis = new Date().getTime();
        for(Map.Entry lastHeartBeatTime: lastHeartBeatTimeOfServers.entrySet()) {
            String serverUrl = (String) lastHeartBeatTime.getKey();
            Date lastPingTime = (Date) lastHeartBeatTime.getValue();
            long diffInMilliSeconds = currentTimeInMillis - lastPingTime.getTime();
            if(diffInMilliSeconds >= Constants.serverTimeoutTime) {
                log.info("Server {} removed : ", serverUrl);
                lastHeartBeatTimeOfServers.remove(serverUrl);
                //TODO : Update chunks metadata and remove this chunkserver
            }
        }
        log.info("Active servers and their last heartbeats: {}", lastHeartBeatTimeOfServers);
    }

    /**
     * Updates the heartbeat of chunkserver with latest heartbeat time
     * @param chunkServerRequest: chunkserver request containing chunk metadata
     */
    public static void updateHeartBeatOfServer(ChunkServerRequest chunkServerRequest) {
        log.info("Updating heartbeat inside updateHeartBeatOfServer() from {}", chunkServerRequest.getChunkServerUrl());
        lastHeartBeatTimeOfServers.put(chunkServerRequest.getChunkServerUrl(), new Date());
        if(chunkServerRequest.isContainsChunksMetadata()){
            ArrayList<ChunkServerChunkMetadata> updatedChunkServerChunkMetadataList = addChunkServerDetails(chunkServerRequest.getChunkServerChunkMetadataList());
            MetadataServiceImpl metadataService = MetadataServiceImpl.getInstance();
            metadataService.updateChunkMetadata(updatedChunkServerChunkMetadataList);
        }
    }

    /**
     * this method adds the last updated time for chunkserver metadata
     * @param chunkServerChunkMetadataList: list containing chunk metadata
     * @return ArrayList<ChunkServerChunkMetadata>: updated list of chunk metadata
     */
    private static ArrayList<ChunkServerChunkMetadata> addChunkServerDetails(ArrayList<ChunkServerChunkMetadata> chunkServerChunkMetadataList){
        log.debug("Inside addChunkServerDetails()");
        ArrayList<ChunkServerChunkMetadata> updatedChunkServerChunkMetadataList = new ArrayList<>();
        for(ChunkServerChunkMetadata chunkServerChunkMetadata: chunkServerChunkMetadataList){
            Location location = chunkServerChunkMetadata.getLocation();
            location.setLastUpdated(new Date());
            chunkServerChunkMetadata.setLocation(location);
            updatedChunkServerChunkMetadataList.add(chunkServerChunkMetadata);
        }
        return updatedChunkServerChunkMetadataList;
    }


    /**
     * fetches list of active servers
     * @return ArrayList<String>: list of all active servers
     */
    public static ArrayList<String> fetchActiveChunkservers() {
        ArrayList<String> chunkServers = new ArrayList<>();
        lastHeartBeatTimeOfServers.forEach( (chunkserver, lastBeat) -> chunkServers.add(chunkserver));
        return chunkServers;
    }
}
