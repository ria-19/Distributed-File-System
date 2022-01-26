package com.gfs.client.service;

import com.gfs.client.model.RequestType;
import com.gfs.client.model.WriteRequest;
import com.gfs.client.utils.JsonHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Component
@Slf4j
public class ClientImpl implements CommandLineRunner {

    Socket socket;

    public ClientImpl(@Value("${client.port}") int port) throws IOException {
        socket = new Socket("127.0.0.1", port);
        log.info("Connected to chunk server : {}", socket);
    }

    @Override
    public void run(String... args) throws Exception {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
//        TODO :
//            Make requests to the master then to chunkserver based on REST APIs
        objectOutputStream.writeObject(JsonHandler.convertObjectToString(RequestType.WRITE));
        objectOutputStream.writeObject(JsonHandler.convertObjectToString(new WriteRequest("first", "lo ye likh dena \n iske saath")));
        log.info("Waiting for reply");
        var obj = objectInputStream.readObject();
        log.info(obj.toString());
    }
}
