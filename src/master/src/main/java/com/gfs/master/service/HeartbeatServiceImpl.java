package com.gfs.master.service;

import com.gfs.master.Constants;
import com.gfs.master.model.ClientChunksMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    @Scheduled(fixedDelay = 10000)
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
            } else{
                //TODO : Update chunks metadata
            }
        }
    }

    /**
     * Updates the heartbeat of chunkserver with latest heartbeat time
     * @param remoteSocket : url of remote chunkserver
     */
    public static void updateHeartBeatOfServer(String remoteSocket, ClientChunksMetadata chunksMetadata) {
        log.info("Heartbeat received from {}", remoteSocket);
        lastHeartBeatTimeOfServers.put(remoteSocket, new Date());
    }
}
