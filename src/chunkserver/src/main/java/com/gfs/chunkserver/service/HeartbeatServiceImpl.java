package com.gfs.chunkserver.service;

import com.gfs.chunkserver.model.ChunkServerRequest;
import com.gfs.chunkserver.model.ChunkServerChunkMetadata;
import com.gfs.chunkserver.model.Location;
import com.gfs.chunkserver.model.Source;
import com.gfs.chunkserver.utils.JsonHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * created by nikunjagarwal on 17-01-2022
 */
@Component
@Slf4j
public class HeartbeatServiceImpl {

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
            if(heartBeatCounter == 3) {
                chunkServerRequest.setContainsChunksMetadata(true);
                chunkServerRequest.setChunkServerChunkMetadataList(fetchChunkServerMetadata());
                heartBeatCounter = 0;
            } else {
                chunkServerRequest.setContainsChunksMetadata(false);
            }
            String chunkstring = JsonHandler.convertObjectToString(chunkServerRequest);
            objectOutputStream.writeObject(chunkstring);
            heartBeatCounter++;
            Thread.sleep(5000);
        }
    }

    /**
     * This method establishes connection with master
     * @param host: master server url
     * @param port : master server port
     * @return Socket: socket connected with master server
     */
    private static Socket establishConnectionWithMaster(String host, int port) throws IOException, SocketException {
        Socket socket = new Socket(host, port);
        log.info("Connected to server : {}", socket);
        return socket;
    }

    /**
     * This function starts sending heartbeats to master server
     */
    public static void startHeartbeatForMaster(String host, int port) {
        //TODO: enable retry mechanism
        try {
            Socket socket = establishConnectionWithMaster(host, port);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(JsonHandler.convertObjectToString(Source.CHUNKSERVER));
            sendHearbeatToMaster(objectOutputStream);
            socket.close();
        } catch (Exception e) {
            log.error("Error:", e);
        }
    }

    private static ArrayList<ChunkServerChunkMetadata> fetchChunkServerMetadata() {
        ArrayList<ChunkServerChunkMetadata> chunkMetadataList = new ArrayList<>();
        //TODO: Fetch actual metadata instead of mock chunkmetadata
        ChunkServerChunkMetadata chunkMetadata = new ChunkServerChunkMetadata();
        chunkMetadata.setChunkHandle("12345");
        Location location = new Location("/mock-path",2);
        chunkMetadata.setLocation(location);
        chunkMetadataList.add(chunkMetadata);
        return  chunkMetadataList;
    }
}
