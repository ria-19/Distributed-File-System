package com.gfs.client.service;

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

@Component
@Slf4j
public class ClientImpl implements CommandLineRunner {

    @Value("${masterserver.host}")
    private String masterServerHost;

    @Value("${masterserver.port}")
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
            ClientChunkserverReadRequest clientChunkserverReadRequest = new ClientChunkserverReadRequest(chunkHandle);
            Response response = sendMessageInSequenceToServersAndFetchResponse(objectInputStream, objectOutputStream, RequestType.READCHUNK, clientChunkserverReadRequest);
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
                clientChunkserverWriteRequest.setChunkPath(location.getChunkPath());
                Response response = sendMessageInSequenceToServersAndFetchResponse(objectInputStream, objectOutputStream, RequestType.WRITETOCACHE, clientChunkserverWriteRequest);
                if(response.getResponseStatus() == ResponseStatus.SUCCESS)
                    noOfSuccessfulSends++;
                socket.close();
            } catch (Exception e){
                log.error("Error : {}", e);
            }
        }
        return noOfSuccessfulSends == chunkServerLocations.size();
    }

    private void sendAckToPrimaryToWriteData(ClientChunkserverWriteAckRequest clientChunkserverWriteAckRequest, String primaryChunkserverSocketString){
        String chunkServerHost = primaryChunkserverSocketString.split(":")[0];
        int chunkServerPort = Integer.parseInt(primaryChunkserverSocketString.split(":")[1]);
        try{
            Socket socket = new Socket(chunkServerHost, chunkServerPort);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            Response response = sendMessageInSequenceToServersAndFetchResponse(objectInputStream, objectOutputStream, RequestType.WRITETOFILEFROMCACHE, clientChunkserverWriteAckRequest);
            socket.close();
            log.info("Response={}", response);
        } catch (Exception e){
            log.error("Error:{}",e);
        }
    }

    private Response sendMessageInSequenceToServersAndFetchResponse(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream, RequestType requestType, ClientRequest data) throws IOException, ClassNotFoundException {
        objectOutputStream.writeObject(JsonHandler.convertObjectToString(Source.CLIENT));
        objectOutputStream.writeObject(JsonHandler.convertObjectToString(requestType));
        objectOutputStream.writeObject(data);
        String responseString = (String) objectInputStream.readObject();
        return JsonHandler.convertStringToObject(responseString, Response.class);
    }

}
