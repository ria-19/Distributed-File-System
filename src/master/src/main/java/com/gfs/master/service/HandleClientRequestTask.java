package com.gfs.master.service;

import com.gfs.master.model.RequestType;
import com.gfs.master.model.request.ClientRequest;
import com.gfs.master.model.response.MasterClientMetadataResponse;
import com.gfs.master.model.response.Response;
import com.gfs.master.model.response.ResponseStatus;
import com.gfs.master.utils.JsonHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
    private ObjectOutputStream objectOutputStream;

    @Override
    public void run() {
        try {
            String remoteSocketAddress = socket.getRemoteSocketAddress().toString();
            String clientRequestTypeString = (String)objectInputStream.readObject();
            RequestType requestType = JsonHandler.convertStringToObject(clientRequestTypeString, RequestType.class);
            String clientRequestString = (String)objectInputStream.readObject();
            log.info("Client {} request from {} for {}", requestType, remoteSocketAddress, clientRequestString);
            ClientRequest clientRequest = JsonHandler.convertStringToObject(clientRequestString, ClientRequest.class);
            switch (requestType) {
                case READ:
                    Response<MasterClientMetadataResponse> readResponse = sendFileMetadata(clientRequest);
                    objectOutputStream.writeObject(JsonHandler.convertObjectToString(readResponse));
                    break;
                case WRITE:
                    Response<MasterClientMetadataResponse> writeResponse = sendNewFileMetadata(clientRequest);
                    objectOutputStream.writeObject(JsonHandler.convertObjectToString(writeResponse));
                    break;
                default:
                    log.error("Invalid type of request");
                }
            socket.close();
        } catch (Exception e) {
            log.error("error in HandleClientRequestTask ", e);
        }
    }
    public Response<MasterClientMetadataResponse> sendFileMetadata(ClientRequest clientRequest) {
        MetadataServiceImpl metadataService = MetadataServiceImpl.getInstance();
        MasterClientMetadataResponse masterClientMetadataResponse = metadataService.getChunkMetadata(clientRequest.getFilename(), clientRequest.getOffset());
        Response<MasterClientMetadataResponse> masterClientReadResponse = new Response<>(ResponseStatus.SUCCESS, masterClientMetadataResponse, null);
        return masterClientReadResponse;
    }


    public Response<MasterClientMetadataResponse> sendNewFileMetadata (ClientRequest clientRequest) {
        MetadataServiceImpl metadataService = MetadataServiceImpl.getInstance();
        MasterClientMetadataResponse masterClientMetadataResponse = metadataService.fetchNewFileMetadata(clientRequest.getFilename(), clientRequest.getOffset());
        Response<MasterClientMetadataResponse> masterClientReadResponse = new Response<>(ResponseStatus.SUCCESS, masterClientMetadataResponse, null);
        return masterClientReadResponse;
    }
}
