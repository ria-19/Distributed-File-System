package com.gfs.chunkserver.service;

import com.gfs.chunkserver.model.Source;
import com.gfs.chunkserver.utils.JsonHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * created by nikunjagarwal on 27-01-2022
 * This class handles request from different sources and redirects to the respective service
 */
@Data
@AllArgsConstructor
@Slf4j
public class HandleRequestTask implements Runnable{
    private Socket socket;

    @Override
    public void run() {
        try{
            String remoteSocketAddress = socket.getRemoteSocketAddress().toString();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            String sourceString = (String)objectInputStream.readObject();
            log.info("Client Request of Type : {} from {}", sourceString, remoteSocketAddress);
            Source source = JsonHandler.convertStringToObject(sourceString, Source.class);
            switch (source){
                case CHUNKSERVER:
                    ChunkserverRequestHandlerImpl chunkserverRequestHandler = new ChunkserverRequestHandlerImpl();
                    chunkserverRequestHandler.handleChunkserverRequest(socket, objectInputStream, objectOutputStream);
                    break;
                case CLIENT:
                    ClientRequestHandlerImpl clientRequestHandler = new ClientRequestHandlerImpl();
                    clientRequestHandler.handleClientRequest(socket, objectInputStream, objectOutputStream);
                    break;
                default:
                    log.error("Incorrect type");

            }
        } catch (Exception e){
            log.error("Error:{}",e);
        }

    }
}
