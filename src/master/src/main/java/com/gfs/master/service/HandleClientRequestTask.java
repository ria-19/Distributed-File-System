package com.gfs.master.service;

import com.fasterxml.jackson.core.JacksonException;
import com.gfs.master.model.RequestType;
import com.gfs.master.model.request.ClientRequest;
import com.gfs.master.model.response.ChunkMetadataResponse;
import com.gfs.master.utils.JsonHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * created by nikunjagarwal on 21-01-2022
 * This class handles all requests from Clients
 */
@Data
@AllArgsConstructor
@Slf4j
public class HandleClientRequestTask implements Runnable{

    private Socket socket;
    private ObjectInputStream objectInputStream;

    @Override
    public void run() {
        try {
            String remoteSocketAddress = socket.getRemoteSocketAddress().toString();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            while(true) {
                String clientRequestTypeString = (String)objectInputStream.readObject();
                RequestType requestType = JsonHandler.convertStringToObject(clientRequestTypeString, RequestType.class);
                String clientRequestString = (String)objectInputStream.readObject();
                log.info("Client {} request from {} for {}", requestType, remoteSocketAddress, clientRequestString);
                ClientRequest clientRequest = JsonHandler.convertStringToObject(clientRequestString, ClientRequest.class);
                switch (requestType) {
                    case READ:
                        objectOutputStream.writeObject(sendFileMetadata(clientRequest));
                        break;
                    case WRITE:
                       objectOutputStream.writeObject(sendNewFileMetadata(clientRequest));
                        break;
                }
            }
        } catch (Exception e) {
            log.error("error in HandleClientRequestTask ", e);
        }
    }
    public ChunkMetadataResponse sendFileMetadata(ClientRequest clientRequest) {
        MetadataServiceImpl metadataService = MetadataServiceImpl.getInstance();
        return metadataService.getChunkMetadataMetadata(clientRequest.getFilename(), clientRequest.getOffset());
    }

    public ChunkMetadataResponse sendNewFileMetadata (ClientRequest clientRequest) {
        MetadataServiceImpl metadataService = MetadataServiceImpl.getInstance();
        return metadataService.updateNewFileMetadata(clientRequest.getFilename(), clientRequest.getOffset());
    }
}
