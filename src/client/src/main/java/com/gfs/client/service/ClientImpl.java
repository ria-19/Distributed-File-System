package com.gfs.client.service;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gfs.client.model.*;
import com.gfs.client.utils.JsonHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

@Component
@Slf4j
public class ClientImpl implements CommandLineRunner {

    @Value("${masterServerUrl}")
    private String masterServerUrl;

    @Value("${masterServerPort}")
    private int masterServerPort;


    @Override
    public void run(String... args) throws Exception {

         //TODO : Make requests to the master then to chunkserver based on REST APIs
    }

    private void fetchChunkDataFromChunkServer(String chunkHandle, Location location){
        try {
            String chunkServerHost = location.getChunkserverUrl().split(":")[0];
            int chunkServerPort = Integer.parseInt(location.getChunkserverUrl().split(":")[1]);
            Socket socket = new Socket(chunkServerHost, chunkServerPort);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            Response response = sendMessageInSequenceToServersAndFetchResponse(objectInputStream, objectOutputStream, Source.CLIENT, RequestType.READCHUNK, chunkHandle);
            socket.close();
            log.info("Response={}", response);
        } catch (Exception e) {
            log.info("Error : {}",e);
        }
    }


    private boolean sendDataToChunkServersForCache(ClientChunkserverWriteRequest clientChunkserverWriteRequest, ArrayList<Location> chunkServerLocations){
        int noOfSuccessfulSends = 0;
        for (Location location: chunkServerLocations) {
            String chunkServerSocketAddress = location.getChunkserverUrl();
            String chunkServerHost = chunkServerSocketAddress.split(":")[0];
            int chunkServerPort = Integer.parseInt(chunkServerSocketAddress.split(":")[1]);
            try{
                Socket socket = new Socket(chunkServerHost, chunkServerPort);
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                Response response = sendMessageInSequenceToServersAndFetchResponse(objectInputStream, objectOutputStream, Source.CLIENT, RequestType.WRITETOCACHE, clientChunkserverWriteRequest);
                if(response.getResponseStatus() == ResponseStatus.SUCCESS)
                    noOfSuccessfulSends++;
                socket.close();
            } catch (Exception e){
                log.error("Error:{}", e);
            }
        }
        if(noOfSuccessfulSends == chunkServerLocations.size())
            return true;
        return false;
    }

    private void sendAckToPrimaryToWriteData(String chunkHandle, String primaryChunkserverSocketString){
        String chunkServerHost = primaryChunkserverSocketString.split(":")[0];
        int chunkServerPort = Integer.parseInt(primaryChunkserverSocketString.split(":")[1]);
        try{
            Socket socket = new Socket(chunkServerHost, chunkServerPort);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            Response response = sendMessageInSequenceToServersAndFetchResponse(objectInputStream, objectOutputStream, Source.CLIENT, RequestType.WRITETOFILEFROMCACHE, chunkHandle);
            socket.close();
            log.info("Response={}", response);
        } catch (Exception e){
            log.error("Error:{}",e);
        }
    }

    private Response sendMessageInSequenceToServersAndFetchResponse(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream, Source source, RequestType requestType, Object data) throws IOException, ClassNotFoundException {
        objectOutputStream.writeObject(JsonHandler.convertObjectToString(source));
        objectOutputStream.writeObject(JsonHandler.convertObjectToString(requestType));
        objectOutputStream.writeObject(data);
        String responseString = (String) objectInputStream.readObject();
        Response response = JsonHandler.convertStringToObject(responseString, Response.class);
        return response;
    }

}
