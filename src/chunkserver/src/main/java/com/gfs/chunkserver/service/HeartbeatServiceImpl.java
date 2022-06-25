package com.gfs.chunkserver.service;

import com.gfs.chunkserver.exception.ConnectionException;
import com.gfs.chunkserver.model.request.ChunkServerRequest;
import com.gfs.chunkserver.model.ChunkServerChunkMetadata;
import com.gfs.chunkserver.model.Location;
import com.gfs.chunkserver.model.Source;
import com.gfs.chunkserver.utils.JsonHandler;
import com.gfs.chunkserver.utils.RetryUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * created by nikunjagarwal on 17-01-2022
 */
@Component
@Slf4j
public class HeartbeatServiceImpl {


    private static String chunkserverHost;
    private static int chunkserverPort;
    private static String masterServerHost;
    private static int masterServerPort;

    public HeartbeatServiceImpl(@Value("${masterserver.host}") String masterServerHost,
                                @Value("${masterserver.port}") int masterServerPort,
                                @Value("${chunkserver.port}") int chunkserverPort,
                                @Value("${chunkserver.host}") String chunkserverHost
                            ) {
        HeartbeatServiceImpl.masterServerHost = masterServerHost;
        HeartbeatServiceImpl.masterServerPort = masterServerPort;
        HeartbeatServiceImpl.chunkserverHost = chunkserverHost;
        HeartbeatServiceImpl.chunkserverPort = chunkserverPort;
    }

    /**
     * It sends 2 types of heartbeats to Master Server at regular intervals
     * 1. simple heartbeat at x intervals    : without chunk metadata
     * 2. metadata heartbeat at 6x intervals : with chunk metadata
     * @param objectOutputStream : outputstream for that socket connection
     */
    private static void sendHearbeatToMaster(ObjectOutputStream objectOutputStream) throws IOException, InterruptedException {
        int heartBeatCounter = 0;
        while(true) {
            log.info("Sending heartbeat to master server");
            ChunkServerRequest chunkServerRequest = new ChunkServerRequest();
            chunkServerRequest.setChunkServerUrl(chunkserverHost+":"+chunkserverPort);
            if(heartBeatCounter == 6) {
                chunkServerRequest.setContainsChunksMetadata(true);
                chunkServerRequest.setChunkServerChunkMetadataList(fetchChunkServerMetadata());
                heartBeatCounter = 0;
            } else {
                chunkServerRequest.setContainsChunksMetadata(false);
            }
            String chunkstring = JsonHandler.convertObjectToString(chunkServerRequest);
            objectOutputStream.writeObject(chunkstring);
            heartBeatCounter++;
            Thread.sleep(10000);
        }
    }

    /**
     * This method establishes connection with master
     * @return Socket: socket connected with master server
     */
    private static Socket establishConnectionWithMaster() throws ConnectionException{
        Socket socket = null;
        RetryUtils retryUtils = new RetryUtils(3);
        while(retryUtils.canRetry()) {
            try {
                log.info("Establishing connection with master for heartbeat. Master host={}, port={}", masterServerHost, masterServerPort);
                socket = new Socket(masterServerHost, masterServerPort);
                log.info("Connected to master server : {}", socket);
                retryUtils.stopRetry();
            } catch (IOException e) {
                log.error("Server connection exception :", e);
                retryUtils.retry();
            }
        }
        if(!retryUtils.canRetry()) {
            throw new ConnectionException("Unable to reach master server.");
        }
        return socket;
    }

    /**
     * This function starts sending heartbeats to master server
     */
    public static void startHeartbeatForMaster() {
        try {
            Socket socket = establishConnectionWithMaster();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(JsonHandler.convertObjectToString(Source.CHUNKSERVER));
            sendHearbeatToMaster(objectOutputStream);
            socket.close();
        } catch (Exception e) {
            log.error("Exception :", e);
        }
    }

    private static ArrayList<ChunkServerChunkMetadata> fetchChunkServerMetadata() {
        ChunkserverMetadataServiceImpl chunkserverMetadataService = ChunkserverMetadataServiceImpl.getInstance();
        return  chunkserverMetadataService.fetchChunkMetadata();
    }
}
