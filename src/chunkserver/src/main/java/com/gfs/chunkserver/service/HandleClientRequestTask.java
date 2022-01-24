package com.gfs.chunkserver.service;

import com.gfs.chunkserver.model.ClientReadRequest;
import com.gfs.chunkserver.model.ClientWriteRequest;
import com.gfs.chunkserver.model.RequestType;
import com.gfs.chunkserver.utils.JsonHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

@Data
@AllArgsConstructor
@Slf4j
public class HandleClientRequestTask implements Runnable {
    private Socket socket;

    @Override
    public void run() {
        try {
            String remoteSocketAddress = socket.getRemoteSocketAddress().toString();
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            String request = (String)objectInputStream.readObject();
            log.info("Client Request of Type : {} from {}", request, remoteSocketAddress);
            RequestType requestType = JsonHandler.convertStringToObject(request, RequestType.class);

            String clientRequestString = (String) objectInputStream.readObject();

            switch (requestType) {
                case READ:
                    ClientReadRequest clientReadRequest = JsonHandler.convertStringToObject(clientRequestString, ClientReadRequest.class);
                    FileHandlingService.readFile(socket, clientReadRequest.getChunkHandle());
                    break;
                case WRITE:
                    ClientWriteRequest clientWriteRequest = JsonHandler.convertStringToObject(clientRequestString, ClientWriteRequest.class);
                    FileHandlingService.writeFile(socket, clientWriteRequest.getChunkHandle(), clientWriteRequest.getData());
                    break;
                default:
                    log.error("Wrong Request Type at HandleClientRequestTask");
                    break;
            }
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error in HandleClientRequestTask:", e);
        }
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                log.error("Error in closing socket in HandleClientRequestTask");
            }
        }
    }
}