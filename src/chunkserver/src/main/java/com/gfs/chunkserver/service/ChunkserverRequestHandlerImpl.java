package com.gfs.chunkserver.service;

import com.gfs.chunkserver.model.ChunkCacheData;
import com.gfs.chunkserver.model.RequestType;
import com.gfs.chunkserver.model.request.ChunkserverChunkserverFinalWriteRequest;
import com.gfs.chunkserver.model.response.Response;
import com.gfs.chunkserver.model.response.ResponseStatus;
import com.gfs.chunkserver.utils.JsonHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * created by nikunjagarwal on 27-01-2022
 */
@Slf4j
@Service
public class ChunkserverRequestHandlerImpl {

    public void handleChunkserverRequest(Socket socket, ObjectInputStream objectInputStream) {
        try {
            String remoteSocketAddress = socket.getRemoteSocketAddress().toString();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            String request = (String) objectInputStream.readObject();
            log.info("Client Request of Type : {} from {}", request, remoteSocketAddress);
            RequestType requestType = JsonHandler.convertStringToObject(request, RequestType.class);
            switch (requestType) {
                case WRITETOFILEFROMCACHE:
                    writeFile(objectOutputStream, objectInputStream);
                    break;
                default:
                    log.error("Wrong Request Type at HandleClientRequestTask");
                    break;
            }
        } catch (Exception e) {
            log.error("Error : {}", e);
        }
    }

    public void writeFile(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) {
        try{
            String chunkserverRequestString = (String) objectInputStream.readObject();
            ChunkserverChunkserverFinalWriteRequest chunkserverChunkserverFinalWriteRequest = JsonHandler.convertStringToObject(chunkserverRequestString, ChunkserverChunkserverFinalWriteRequest.class);
            writeToOwnCache(chunkserverChunkserverFinalWriteRequest.getChunkHandle());
            Response response = new Response(ResponseStatus.SUCCESS, null);
            objectOutputStream.writeObject(JsonHandler.convertObjectToString(response));
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error in HandleClientRequestTask:", e);
        }
    }

    private void writeToOwnCache(String chunkHandle) {
        ChunkCacheService chunkCacheService = ChunkCacheService.getInstance();
        ChunkCacheData chunkCacheData = chunkCacheService.getChunkDataFromCache(chunkHandle);
        FileHandlingService.writeFile(chunkCacheData.getChunkPath(), chunkCacheData.getData());
        // update metadata
        chunkCacheService.deleteFromChunkCache(chunkHandle);
    }
}
