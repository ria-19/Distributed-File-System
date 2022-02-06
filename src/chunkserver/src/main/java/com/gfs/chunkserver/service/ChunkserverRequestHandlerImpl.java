package com.gfs.chunkserver.service;

import com.gfs.chunkserver.model.ChunkCacheData;
import com.gfs.chunkserver.model.RequestType;
import com.gfs.chunkserver.model.request.ChunkserverChunkserverFinalWriteRequest;
import com.gfs.chunkserver.model.response.ChunkserverResponse;
import com.gfs.chunkserver.model.response.ResponseStatus;
import com.gfs.chunkserver.utils.FileHandlingService;
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



    /**
     * This method handles different types of request where source is another chunkserver
     * @param socket: socket connected to the other chunkserver
     * @param objectInputStream: objectOutputStream for that socket
     */
    public void handleChunkserverRequest(Socket socket, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) {
        try {
            String remoteSocketAddress = socket.getRemoteSocketAddress().toString();
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
            log.error("Error :", e);
        }
    }

    /**
     * This method handles Write to Cache request. It uses the chunk handle provided by another
     * chunkserver and writes the data in a file.
     * @param objectInputStream: objectInputStream for the connected socket
     * @param objectOutputStream:objectOutputStream for the connected socket
     */
    public void writeFile(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) {
        try{
            String chunkserverRequestString = (String) objectInputStream.readObject();
            ChunkserverChunkserverFinalWriteRequest chunkserverChunkserverFinalWriteRequest = JsonHandler.convertStringToObject(chunkserverRequestString, ChunkserverChunkserverFinalWriteRequest.class);
            ResponseStatus responseStatus = writeToFileFromCache(chunkserverChunkserverFinalWriteRequest.getChunkHandle());
            ChunkserverResponse chunkserverResponse = new ChunkserverResponse(responseStatus, null, null);
            objectOutputStream.writeObject(JsonHandler.convertObjectToString(chunkserverResponse));
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error in HandleClientRequestTask:", e);
        }
    }

    /**
     * This method fetches the chunkdata from cache service, and writes that data in a file
     * using FileService
     * @param chunkHandle: chunkhandle for the
     */
    private ResponseStatus writeToFileFromCache(String chunkHandle) {
        ChunkCacheService chunkCacheService = ChunkCacheService.getInstance();
        ChunkCacheData chunkCacheData = chunkCacheService.getChunkDataFromCache(chunkHandle);
        if (chunkCacheData == null) {
            return ResponseStatus.ERROR;
        }
        FileHandlingService.writeFile(chunkHandle, chunkCacheData.getData());
        ChunkserverMetadataServiceImpl chunkserverMetadataService = ChunkserverMetadataServiceImpl.getInstance();
        chunkserverMetadataService.insertChunkMetadata(chunkHandle);
        chunkCacheService.deleteFromChunkCache(chunkHandle);
        return ResponseStatus.SUCCESS;
    }
}
