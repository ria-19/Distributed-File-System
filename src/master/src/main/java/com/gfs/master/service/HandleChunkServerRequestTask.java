package com.gfs.master.service;

import com.gfs.master.model.request.ChunkServerRequest;
import com.gfs.master.utils.JsonHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * created by nikunjagarwal on 16-01-2022
 * This class handles all requests from ChunkServers
 */
@Data
@AllArgsConstructor
@Slf4j
public class HandleChunkServerRequestTask implements Runnable{
    private Socket socket;
    private ObjectInputStream objectInputStream;

    /**
     * handles requests from chunkservers
     */
    @Override
    public void run() {
        try {
            String remoteSocketAddress = socket.getRemoteSocketAddress().toString();
            while(true){
                String chunkServerRequestString = (String)objectInputStream.readObject();
                log.info("Heartbeat Request from Chunkserver {} : {}", remoteSocketAddress, chunkServerRequestString);
                ChunkServerRequest chunkServerRequest = JsonHandler.convertStringToObject(chunkServerRequestString, ChunkServerRequest.class);
                HeartbeatServiceImpl.updateHeartBeatOfServer(chunkServerRequest);
            }
        } catch (Exception e) {
            log.error("error : ", e);
        }
    }
}
