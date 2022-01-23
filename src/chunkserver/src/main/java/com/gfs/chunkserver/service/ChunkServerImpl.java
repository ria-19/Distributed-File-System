package com.gfs.chunkserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * created by nikunjagarwal on 16-01-2022
 */
@Component
@Slf4j
public class ChunkServerImpl implements CommandLineRunner {

    ServerSocket serverSocket;

    public ChunkServerImpl() throws Exception{
//        serverSocket = new ServerSocket(8020);
    }

    /**
     * It initiates the heartbeat function and creates a server socket for clients and
     * listens on that
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("Server started");
        HeartbeatServiceImpl.startHeartbeatForMaster();
        while (true) {
            try {
//                Socket socket = serverSocket.accept();
                // TODO : Handle incoming data requests from clients
            } catch (Exception e) {
                log.error("Error:", e);
            }
        }
    }
}
