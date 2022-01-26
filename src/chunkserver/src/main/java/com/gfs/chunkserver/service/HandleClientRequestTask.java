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
import java.io.ObjectOutputStream;
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
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            String request = (String)objectInputStream.readObject();
            log.info("Client Request of Type : {} from {}", request, remoteSocketAddress);
            RequestType requestType = JsonHandler.convertStringToObject(request, RequestType.class);

            switch (requestType) {
                case READ:
                    readFile(objectOutputStream, objectInputStream);
                    break;
                case WRITE:
                    writeFile(objectOutputStream, objectInputStream);
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

    public void readFile(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) {
        try{
            String clientRequestString = (String) objectInputStream.readObject();
            log.info("Filename to read from : {}" ,clientRequestString);
            ClientReadRequest clientReadRequest = JsonHandler.convertStringToObject(clientRequestString, ClientReadRequest.class);
            String filedata = FileHandlingService.readFile(clientReadRequest.getChunkHandle());
            objectOutputStream.writeObject(filedata);
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error in HandleClientRequestTask:", e);
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