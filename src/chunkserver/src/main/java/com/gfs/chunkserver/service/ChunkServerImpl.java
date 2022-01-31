package com.gfs.chunkserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
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


    @Value("${numFileHandlingThreads}")
    private int numFileHandlingThreads;
    @Value("${chunkserver.host}")
    private String chunkServerHostString;
    @Value("${chunkserver.port}")
    private int chunkServerPort;
    @Value("${chunkserver.queuesize}")
    private int maximumQueueLength;

    /**
     * It initiates the heartbeat function and creates a server socket for clients and
     * listens on that
     */
    @Override
    public void run(String... args) throws IOException{
        InetAddress chunkserverHost = InetAddress.getByName(chunkServerHostString);
        ServerSocket serverSocket = new ServerSocket(chunkServerPort, maximumQueueLength ,chunkserverHost);
        log.info("Server started. Chunkserver is listening at : {}", serverSocket.getLocalSocketAddress());
        ExecutorService heartbeatExecutorService = Executors.newSingleThreadExecutor();
        heartbeatExecutorService.execute(HeartbeatServiceImpl::startHeartbeatForMaster);
        ExecutorService executorService= Executors.newFixedThreadPool(numFileHandlingThreads);

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
