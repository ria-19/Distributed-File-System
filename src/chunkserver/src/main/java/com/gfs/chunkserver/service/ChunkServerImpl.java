package com.gfs.chunkserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * created by nikunjagarwal on 16-01-2022
 */
@Component
@Slf4j
public class ChunkServerImpl implements CommandLineRunner {

    ServerSocket serverSocket;
    @Value("${chunkServerPort}")
    private int chunkServerPort;
    @Value("${masterServerHost}")
    private String masterServerHost;
    @Value("${masterServerPort}")
    private int masterServerPort;
    @Value("${numFileHandlingThreads}")
    private int numFileHandlingThreads;

    public ChunkServerImpl() throws Exception{
        serverSocket = new ServerSocket(chunkServerPort);
    }

    /**
     * It initiates the heartbeat function and creates a server socket for clients and
     * listens on that
     */
    @Override
    public void run(String... args) {
        log.info("Server started");
        ExecutorService executorService= Executors.newFixedThreadPool(numFileHandlingThreads);
        ExecutorService heartbeatExecutorService = Executors.newSingleThreadExecutor();
        heartbeatExecutorService.execute(() -> HeartbeatServiceImpl.startHeartbeatForMaster(masterServerHost, masterServerPort));
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                log.info("Started Connection with Remote Socket Address : " + socket.getRemoteSocketAddress());
                executorService.execute(new HandleRequestTask(socket));
             } catch (Exception e) {
                log.error("Error:", e);
            }
        }
    }
}
