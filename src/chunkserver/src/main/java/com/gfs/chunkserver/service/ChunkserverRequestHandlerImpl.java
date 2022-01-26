package com.gfs.chunkserver.service;

import com.gfs.chunkserver.model.ClientWriteRequest;
import com.gfs.chunkserver.model.RequestType;
import com.gfs.chunkserver.utils.JsonHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * created by nikunjagarwal on 27-01-2022
 */
@Slf4j
@Service
public class ChunkserverRequestHandlerImpl {

    public void handleClientRequest(Socket socket, ObjectInputStream objectInputStream) {
        try {
            String remoteSocketAddress = socket.getRemoteSocketAddress().toString();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            String request = (String) objectInputStream.readObject();
            log.info("Client Request of Type : {} from {}", request, remoteSocketAddress);
            RequestType requestType = JsonHandler.convertStringToObject(request, RequestType.class);

            switch (requestType) {
                case WRITE:
                    writeFile(objectOutputStream, objectInputStream);
                    break;
                default:
                    log.error("Wrong Request Type at HandleClientRequestTask");
                    break;
            }
        } catch (Exception e) {
            log.error("Error : {}", e);
        }
    }

    public void writeFile(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) {
        try{
            String clientRequestString = (String) objectInputStream.readObject();
            ClientWriteRequest clientWriteRequest = JsonHandler.convertStringToObject(clientRequestString, ClientWriteRequest.class);
            FileHandlingService.writeFile(clientWriteRequest.getChunkHandle(), clientWriteRequest.getData());
            objectOutputStream.writeObject("Success");
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error in HandleClientRequestTask:", e);
        }
    }
}
