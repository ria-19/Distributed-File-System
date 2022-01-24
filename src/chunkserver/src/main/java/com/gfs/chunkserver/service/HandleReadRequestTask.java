package com.gfs.chunkserver.service;

import com.gfs.chunkserver.model.ReadRequest;
import com.gfs.chunkserver.model.RequestType;
import com.gfs.chunkserver.utils.JsonHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

@Data
@AllArgsConstructor
@Slf4j
public class HandleReadRequestTask implements Runnable {
    private Socket socket;
    private ObjectInputStream objectInputStream;


    @Override
    public void run() {
        try {
            String remoteSocketAddress = socket.getRemoteSocketAddress().toString();
            String clientRequestString = (String) objectInputStream.readObject();
            log.info("File read request from {} for {}", remoteSocketAddress, clientRequestString);
            ReadRequest readRequest = JsonHandler.convertStringToObject(clientRequestString, ReadRequest.class);
            var fileHandlingService = new FileHandlingService(socket, readRequest.getChunkHandle());
            fileHandlingService.readFile();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
