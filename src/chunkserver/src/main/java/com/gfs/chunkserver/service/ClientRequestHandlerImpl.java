package com.gfs.chunkserver.service;

import com.gfs.chunkserver.model.*;
import com.gfs.chunkserver.model.request.ChunkserverChunkserverFinalWriteRequest;
import com.gfs.chunkserver.model.request.ClientChunkserverReadRequest;
import com.gfs.chunkserver.model.request.ClientChunkserverWriteAckRequest;
import com.gfs.chunkserver.model.request.ClientChunkserverWriteRequest;
import com.gfs.chunkserver.model.response.ChunkServerClientReadResponse;
import com.gfs.chunkserver.model.response.ChunkserverResponse;
import com.gfs.chunkserver.model.response.Response;
import com.gfs.chunkserver.model.response.ResponseStatus;
import com.gfs.chunkserver.utils.JsonHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Data
@AllArgsConstructor
@Slf4j
public class ClientRequestHandlerImpl {

    public void handleClientRequest(Socket socket, ObjectInputStream objectInputStream) {
        try {
            String remoteSocketAddress = socket.getRemoteSocketAddress().toString();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            String request = (String) objectInputStream.readObject();
            log.info("Client Request of Type : {} from {}", request, remoteSocketAddress);
            RequestType requestType = JsonHandler.convertStringToObject(request, RequestType.class);

            switch (requestType) {
                case READCHUNK:
                    readFile(objectOutputStream, objectInputStream);
                    break;
                case WRITETOCACHE:
                    writeFileToCache(objectOutputStream, objectInputStream);
                    break;
                default:
                    log.error("Wrong Request Type at HandleClientRequestTask");
                    break;
            }
        } catch (Exception e) {
            log.error("Error : {}", e);
        }
    }

    public void readFile(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) {
        try{
            String clientRequestString = (String) objectInputStream.readObject();
            log.info("Filename to read from : {}" ,clientRequestString);
            ClientChunkserverReadRequest clientChunkserverReadRequest = JsonHandler.convertStringToObject(clientRequestString, ClientChunkserverReadRequest.class);
            String filedata = FileHandlingService.readFile(clientChunkserverReadRequest.getChunkHandle());
            ChunkServerClientReadResponse chunkServerClientReadResponse = new ChunkServerClientReadResponse(filedata);
            ChunkserverResponse<ChunkServerClientReadResponse> chunkserverResponse = new ChunkserverResponse<>();
            chunkserverResponse.setResponseStatus(ResponseStatus.SUCCESS);
            chunkserverResponse.setData(chunkServerClientReadResponse);
            objectOutputStream.writeObject(chunkserverResponse);
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error in HandleClientRequestTask:", e);
        }
    }


    private void writeFileToCache(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream){
        try{
            String clientRequestString = (String) objectInputStream.readObject();
            ClientChunkserverWriteRequest clientChunkserverWriteRequest = JsonHandler.convertStringToObject(clientRequestString, ClientChunkserverWriteRequest.class);
            ChunkCacheService chunkCacheService = ChunkCacheService.getInstance();
            ChunkCacheData chunkCacheData = new ChunkCacheData(clientChunkserverWriteRequest.getChunkPath(), clientChunkserverWriteRequest.getData());
            chunkCacheService.insertIntoChunkCache(clientChunkserverWriteRequest.getChunkHandle(), chunkCacheData);
            objectOutputStream.writeObject("Success");
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error in HandleClientRequestTask:", e);
        }
    }

    private void writeAckToPrimary(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream){
        try{
            String clientRequestString = (String) objectInputStream.readObject();
            ClientChunkserverWriteAckRequest clientChunkserverWriteAckRequest = JsonHandler.convertStringToObject(clientRequestString, ClientChunkserverWriteAckRequest.class);
            writeToOwnCache(clientChunkserverWriteAckRequest.getChunkHandle());
            for (Location location : clientChunkserverWriteAckRequest.getLocations()) {
                String secondaryChunkServerSocketAddress = location.getChunkserverUrl();
                String secondaryChunkServerHost = secondaryChunkServerSocketAddress.split(":")[0];
                int secondaryChunkServerPort = Integer.parseInt(secondaryChunkServerSocketAddress.split(":")[1]);
                try{
                    Socket socketForSecondaryCS = new Socket(secondaryChunkServerHost, secondaryChunkServerPort);
                    ObjectInputStream objectInputStreamForSecondaryCS = new ObjectInputStream(socketForSecondaryCS.getInputStream());
                    ObjectOutputStream objectOutputStreamForSecondaryCS = new ObjectOutputStream(socketForSecondaryCS.getOutputStream());
                    ChunkserverChunkserverFinalWriteRequest chunkserverChunkserverFinalWriteRequest = new ChunkserverChunkserverFinalWriteRequest(clientChunkserverWriteAckRequest.getChunkHandle());
                    objectOutputStreamForSecondaryCS.writeObject(JsonHandler.convertObjectToString(Source.CHUNKSERVER));
                    objectOutputStreamForSecondaryCS.writeObject(JsonHandler.convertObjectToString(RequestType.WRITETOFILEFROMCACHE));
                    objectOutputStreamForSecondaryCS.writeObject(JsonHandler.convertObjectToString(chunkserverChunkserverFinalWriteRequest));
                    String responseString = (String)objectInputStreamForSecondaryCS.readObject();
                    Response response = JsonHandler.convertStringToObject(responseString, Response.class);
                    if(!response.getResponseStatus().equals(ResponseStatus.SUCCESS)) {
                        log.error("Write didn't happen in CS={}, reponse={}", location, response);
                    }
                    socketForSecondaryCS.close();
                } catch (Exception e){
                    log.error("Error : {}", e);
                }
            }
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