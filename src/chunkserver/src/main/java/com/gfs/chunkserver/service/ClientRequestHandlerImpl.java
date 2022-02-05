package com.gfs.chunkserver.service;

import com.gfs.chunkserver.model.*;
import com.gfs.chunkserver.model.request.ChunkserverChunkserverFinalWriteRequest;
import com.gfs.chunkserver.model.request.ClientChunkserverReadRequest;
import com.gfs.chunkserver.model.request.ClientChunkserverWriteAckRequest;
import com.gfs.chunkserver.model.request.ClientChunkserverWriteRequest;
import com.gfs.chunkserver.model.response.ChunkServerClientReadResponse;
import com.gfs.chunkserver.model.response.ChunkserverResponse;
import com.gfs.chunkserver.model.response.ResponseStatus;
import com.gfs.chunkserver.utils.FileHandlingService;
import com.gfs.chunkserver.utils.JsonHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Data
@AllArgsConstructor
@Slf4j
@Service
public class ClientRequestHandlerImpl {

    /**
     * This method handles different types of client requests and redirects it to different functions
     * @param socket:
     * @param objectInputStream:
     */
    public void handleClientRequest(Socket socket, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) {
        try {
            String remoteSocketAddress = socket.getRemoteSocketAddress().toString();
            String request = (String) objectInputStream.readObject();
            log.info("Client Request of Type : {} from {}", request, remoteSocketAddress);
            RequestType requestType = JsonHandler.convertStringToObject(request, RequestType.class);
            String clientRequestString = (String) objectInputStream.readObject();
            ChunkserverResponse chunkserverResponse = new ChunkserverResponse();
            switch (requestType) {
                case READCHUNK:
                    ClientChunkserverReadRequest clientChunkserverReadRequest = JsonHandler.convertStringToObject(clientRequestString, ClientChunkserverReadRequest.class);
                    chunkserverResponse = readFile(clientChunkserverReadRequest);
                    break;
                case WRITETOCACHE:
                    ClientChunkserverWriteRequest clientChunkserverWriteRequest = JsonHandler.convertStringToObject(clientRequestString, ClientChunkserverWriteRequest.class);
                    chunkserverResponse = writeFileToCache(clientChunkserverWriteRequest);
                    break;
                case WRITETOFILEFROMCACHE:
                    ClientChunkserverWriteAckRequest clientChunkserverWriteAckRequest = JsonHandler.convertStringToObject(clientRequestString, ClientChunkserverWriteAckRequest.class);
                    chunkserverResponse = writeAckToPrimary(clientChunkserverWriteAckRequest);
                    break;
                default:
                    log.error("Wrong Request Type at HandleClientRequestTask");
                    break;
            }
            objectOutputStream.writeObject(JsonHandler.convertObjectToString(chunkserverResponse));
        } catch (Exception e) {
            log.error("Error : {}", e);
        }
    }

    /**
     * This method handles chunk read requests. It receives a chunkhandle and then
     * reads and returns the chunk data using file service.
     * @param :
     */
    public ChunkserverResponse<ChunkServerClientReadResponse> readFile(ClientChunkserverReadRequest clientChunkserverReadRequest) {
        ChunkserverResponse<ChunkServerClientReadResponse> chunkserverResponse = new ChunkserverResponse<>();
        try{
            String filedata = FileHandlingService.readFile(clientChunkserverReadRequest.getChunkHandle());
            ChunkServerClientReadResponse chunkServerClientReadResponse = new ChunkServerClientReadResponse(filedata);
            chunkserverResponse.setResponseStatus(ResponseStatus.SUCCESS);
            chunkserverResponse.setData(chunkServerClientReadResponse);
        } catch (IOException e) {
            log.error("Error in HandleClientRequestTask:", e);
        }
        return chunkserverResponse;
    }

    /**
     * This method writes chunk data received from client to cache using CacheService.
     * @param :
     * @return :
     */
    private ChunkserverResponse writeFileToCache(ClientChunkserverWriteRequest clientChunkserverWriteRequest){
        ChunkCacheService chunkCacheService = ChunkCacheService.getInstance();
        ChunkCacheData chunkCacheData = new ChunkCacheData(clientChunkserverWriteRequest.getData());
        chunkCacheService.insertIntoChunkCache(clientChunkserverWriteRequest.getChunkHandle(), chunkCacheData);
        return new ChunkserverResponse(ResponseStatus.SUCCESS, null);
    }

    /**
     * This method handles write Ack requests from clients. First, it fetches data from cache and
     * writes in a file using chunkhandle. Then it connects to secondary servers and sends the request
     * to write in a file.
     * @param :
     * @return :
     */
    private ChunkserverResponse writeAckToPrimary(ClientChunkserverWriteAckRequest clientChunkserverWriteAckRequest){
        writeToFileFromCache(clientChunkserverWriteAckRequest.getChunkHandle());
        for (Location location : clientChunkserverWriteAckRequest.getSecondaryCSLocations()) {
            String secondaryChunkServerSocketAddress = location.getChunkserverUrl();
            ChunkserverChunkserverFinalWriteRequest chunkserverChunkserverFinalWriteRequest = new ChunkserverChunkserverFinalWriteRequest(clientChunkserverWriteAckRequest.getChunkHandle());
            try{
                ChunkserverResponse chunkserverResponse = sendWriteFileRequestToOtherChunkServers(secondaryChunkServerSocketAddress, chunkserverChunkserverFinalWriteRequest);
                if(!chunkserverResponse.getResponseStatus().equals(ResponseStatus.SUCCESS)) {
                    log.error("Write didn't happen in CS={}, reponse={}", location, chunkserverResponse);
                    return new ChunkserverResponse(ResponseStatus.ERROR, null);
                }
            } catch (Exception e){
                log.error("Error : {}", e);
                return new ChunkserverResponse(ResponseStatus.ERROR, null);
            }
        }
        return new ChunkserverResponse(ResponseStatus.SUCCESS, null);
    }

    private void writeToFileFromCache(String chunkHandle) {
        ChunkCacheService chunkCacheService = ChunkCacheService.getInstance();
        ChunkCacheData chunkCacheData = chunkCacheService.getChunkDataFromCache(chunkHandle);
        FileHandlingService.writeFile(chunkHandle, chunkCacheData.getData());
        // update metadata
        chunkCacheService.deleteFromChunkCache(chunkHandle);
    }

    /**
     * This method sends Write to File requests to other chunk servers
     * and returns the response
     * @param :
     * @return :
     */
    private ChunkserverResponse sendWriteFileRequestToOtherChunkServers(String secondaryChunkServerSocketAddress,
                                                                        ChunkserverChunkserverFinalWriteRequest chunkserverChunkserverFinalWriteRequest) {
        String secondaryChunkServerHost = secondaryChunkServerSocketAddress.split(":")[0];
        int secondaryChunkServerPort = Integer.parseInt(secondaryChunkServerSocketAddress.split(":")[1]);
        ChunkserverResponse chunkserverResponse = new ChunkserverResponse();
        try{
            Socket socketForSecondaryCS = new Socket(secondaryChunkServerHost, secondaryChunkServerPort);
            ObjectInputStream objectInputStreamForSecondaryCS = new ObjectInputStream(socketForSecondaryCS.getInputStream());
            ObjectOutputStream objectOutputStreamForSecondaryCS = new ObjectOutputStream(socketForSecondaryCS.getOutputStream());
            objectOutputStreamForSecondaryCS.writeObject(JsonHandler.convertObjectToString(Source.CHUNKSERVER));
            objectOutputStreamForSecondaryCS.writeObject(JsonHandler.convertObjectToString(RequestType.WRITETOFILEFROMCACHE));
            objectOutputStreamForSecondaryCS.writeObject(JsonHandler.convertObjectToString(chunkserverChunkserverFinalWriteRequest));
            String responseString = (String)objectInputStreamForSecondaryCS.readObject();
            chunkserverResponse = JsonHandler.convertStringToObject(responseString, ChunkserverResponse.class);
            socketForSecondaryCS.close();
        } catch (Exception e) {
            log.error("Error : {}", e);
        }
        return chunkserverResponse;
    }
}