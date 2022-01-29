package com.gfs.client.service;

import com.gfs.client.model.*;
import com.gfs.client.model.request.ClientChunkserverReadRequest;
import com.gfs.client.model.request.ClientChunkserverWriteAckRequest;
import com.gfs.client.model.request.ClientChunkserverWriteRequest;
import com.gfs.client.model.request.ClientRequest;
import com.gfs.client.utils.JsonHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * created by nikunjagarwal on 30-01-2022
 */
@Service
@Slf4j
public class ChunkserverConnectorServiceImpl {
    
    public Response readChunkDataFromChunkServer(ClientChunkserverReadRequest clientChunkserverReadRequest, Location location){
        return sendRequestToChunkServer(location.getChunkserverUrl(), clientChunkserverReadRequest, RequestType.READCHUNK);
    }

    //TODO: to be completed post client master integration
    public Response writeChunkDataToChunkServer() {
        return null;
    }

    private Response sendAckToPrimaryToWriteData(ClientChunkserverWriteAckRequest clientChunkserverWriteAckRequest, Location primaryChunkserverLocation){
        return sendRequestToChunkServer(primaryChunkserverLocation.getChunkserverUrl(), clientChunkserverWriteAckRequest, RequestType.WRITETOFILEFROMCACHE);
    }

    private Response sendDataToChunkServersForCache(ClientChunkserverWriteRequest clientChunkserverWriteRequest, ArrayList<Location> chunkServerLocations){
        log.info("Sending data to all chunkservers for caching. Chunkservers={}", chunkServerLocations);
        int noOfSuccessfulSends = 0;
        Response finalResponse = new Response(ResponseStatus.SUCCESS, null);
        for (Location location: chunkServerLocations) {
            String chunkServerSocketAddress = location.getChunkserverUrl();
            Response response = sendRequestToChunkServer(chunkServerSocketAddress, clientChunkserverWriteRequest, RequestType.WRITETOCACHE);
            if(response.getResponseStatus() == ResponseStatus.SUCCESS)
                noOfSuccessfulSends++;
        }
        if(!(noOfSuccessfulSends == chunkServerLocations.size())){
            finalResponse.setResponseStatus(ResponseStatus.ERROR);
        }
        return finalResponse;
    }

    private Response sendRequestToChunkServer(String chunkServerSocketAddress, ClientRequest clientRequest, RequestType requestType) {
        log.info("Establishing connection and sending request to chunkserver={}, clientRequest={}, requestType={}", chunkServerSocketAddress, clientRequest, requestType);
        Response response = new Response();
        try{
            String chunkServerHost = chunkServerSocketAddress.split(":")[0];
            int chunkServerPort = Integer.parseInt(chunkServerSocketAddress.split(":")[1]);
            Socket socket = new Socket(chunkServerHost, chunkServerPort);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(JsonHandler.convertObjectToString(Source.CLIENT));
            objectOutputStream.writeObject(JsonHandler.convertObjectToString(requestType));
            objectOutputStream.writeObject(JsonHandler.convertObjectToString(clientRequest));
            String responseString = (String) objectInputStream.readObject();
            response = JsonHandler.convertStringToObject(responseString, Response.class);
            socket.close();
            log.info("Response={}", response);
        } catch (Exception e){
            log.error("Error:",e);
        }
        return response;
    }
}
