package com.gfs.chunkserver.service;

import com.gfs.chunkserver.model.Source;
import com.gfs.chunkserver.utils.JsonHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * created by nikunjagarwal on 27-01-2022
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
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            String sourceString = (String)objectInputStream.readObject();
            log.info("Client Request of Type : {} from {}", sourceString, remoteSocketAddress);
            Source source = JsonHandler.convertStringToObject(sourceString, Source.class);
            switch (source){
                case CHUNKSERVER:
                    break;
                case CLIENT:
                    ClientRequestHandlerImpl clientRequestHandler = new ClientRequestHandlerImpl();
                    clientRequestHandler.handleClientRequest(socket, objectInputStream);
                    break;
                default:
                    log.error("Incorrect type");

            }
        } catch (Exception e){

        }

    }
}
