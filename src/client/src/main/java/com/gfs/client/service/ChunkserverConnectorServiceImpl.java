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
import java.util.HashMap;

/**
 * created by nikunjagarwal on 30-01-2022
 */
@Service
@Slf4j
public class ChunkserverConnectorServiceImpl {

    public Response writeChunkDataToChunkServer(ChunkMetadata chunkMetadata, String data) {
        log.info("Inside writeChunkDataToChunkServer with chunkMetadata={}",chunkMetadata);
        HashMap<String, Location> locationHashMap = chunkMetadata.getLocations();
        sendDataToChunkServersForCache(chunkMetadata, locationHashMap, data);
        return sendAckToPrimaryToWriteData(chunkMetadata, locationHashMap);
    }

    public Response readChunkDataFromChunkServer(ChunkMetadata chunkMetadata) {
        log.info("Inside writeChunkDataToChunkServer with chunkMetadata={}",chunkMetadata);
        HashMap<String, Location> locationHashMap = chunkMetadata.getLocations();
        ArrayList<Location> locations = getChunkserverArray(locationHashMap);
        if(locations.size() == 0)
            return new Response(ResponseStatus.ERROR, null, null);
        ClientChunkserverReadRequest clientChunkserverReadRequest = new ClientChunkserverReadRequest(chunkMetadata.getChunkHandle());
        return sendRequestToChunkServer(locations.get(0).getChunkserverUrl(), clientChunkserverReadRequest, RequestType.READCHUNK);
    }


    private Response sendAckToPrimaryToWriteData(ChunkMetadata chunkMetadata, HashMap<String, Location> locationHashMap){
        log.info("Inside sendAckToPrimaryToWriteData with chunkMetadata={}, locationHashMap={}",chunkMetadata, locationHashMap);
        String primaryCSString = chunkMetadata.getLeaseServer();
        Location primaryChunkserverLocation = locationHashMap.get(primaryCSString);
        ArrayList<Location> secondaryCS = getSecondaryCSLocations(locationHashMap, primaryCSString);
        ClientChunkserverWriteAckRequest clientChunkserverWriteAckRequest = new ClientChunkserverWriteAckRequest(chunkMetadata.getChunkHandle(), secondaryCS);
        return sendRequestToChunkServer(primaryChunkserverLocation.getChunkserverUrl(), clientChunkserverWriteAckRequest, RequestType.WRITETOFILEFROMCACHE);
    }

    private Response sendDataToChunkServersForCache(ChunkMetadata chunkMetadata, HashMap<String, Location> locationHashMap, String data){
        log.info("Sending data to all chunkservers for caching. chunkMetadata={}, locations={}", chunkMetadata, locationHashMap);
        ClientChunkserverWriteRequest clientChunkserverWriteRequest = new ClientChunkserverWriteRequest(chunkMetadata.getChunkHandle(), data);
        ArrayList<Location> chunkServerLocations = getChunkserverArray(locationHashMap);

        int noOfSuccessfulSends = 0;
        Response finalResponse = new Response(ResponseStatus.SUCCESS, null, null);
        for (Location location: chunkServerLocations) {
            String chunkServerSocketAddress = location.getChunkserverUrl();
            log.info("Sending data to chunkserver={}, writeRequest={}",chunkServerSocketAddress, clientChunkserverWriteRequest);
            Response response = sendRequestToChunkServer(chunkServerSocketAddress, clientChunkserverWriteRequest, RequestType.WRITETOCACHE);
            log.info("Response from {}:{}", chunkServerSocketAddress, response);
            if(response.getResponseStatus() == ResponseStatus.SUCCESS)
                noOfSuccessfulSends++;
        }
        if(!(noOfSuccessfulSends == chunkServerLocations.size())){
            finalResponse.setResponseStatus(ResponseStatus.ERROR);
        }
        return finalResponse;
    }

    private Response sendRequestToChunkServer(String chunkServerSocketAddress, ClientRequest clientRequest, RequestType requestType) {
        log.info("Connecting and Sending request to chunkserver={}, requestType={}, clientRequest={}", chunkServerSocketAddress,requestType, clientRequest);
        Response response = new Response();
        try{
            String chunkServerHost = chunkServerSocketAddress.split(":")[0];
            int chunkServerPort = Integer.parseInt(chunkServerSocketAddress.split(":")[1]);
            Socket socket = new Socket(chunkServerHost, chunkServerPort);
            log.info("Connection Established with chunkserver={}",chunkServerSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream.writeObject(JsonHandler.convertObjectToString(Source.CLIENT));
            objectOutputStream.writeObject(JsonHandler.convertObjectToString(requestType));
            objectOutputStream.writeObject(JsonHandler.convertObjectToString(clientRequest));
            String responseString = (String) objectInputStream.readObject();
            log.info("Response={}", responseString);
            response = JsonHandler.convertStringToObject(responseString, Response.class);
            socket.close();
            log.info("Connection with CS={} closed.", chunkServerSocketAddress);
        } catch (Exception e){
            log.error("Error:",e);
        }
        return response;
    }

    private ArrayList<Location> getChunkserverArray(HashMap<String, Location> locationHashMap) {
        ArrayList<Location> locations = new ArrayList<>();
        locationHashMap.forEach((locationUrl, location) -> locations.add(location));
        return locations;
    }

    private ArrayList<Location> getSecondaryCSLocations(HashMap<String, Location> locationHashMap, String primaryCS) {
        log.info("Inside getSecondaryCSLocations");
        ArrayList<Location> locations = new ArrayList<>();
        locationHashMap.remove(primaryCS);
        locationHashMap.forEach((locationUrl, location) -> locations.add(location));
        return locations;
    }
}
