package com.gfs.chunkserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * created by nikunjagarwal on 17-01-2022
 */
@Component
@EnableScheduling
@Slf4j
public class HeartbeatServiceImpl {

    @Scheduled(fixedDelay = 1000)
    public void sendHearbeatToMaster() throws IOException {
        Socket socket = new Socket("127.0.0.1",8018);
        log.info("Connected to server : {}", socket);
        OutputStream outputStream = socket.getOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject("chunkserver");
        outputStream.close();
        socket.close();
    }
}
