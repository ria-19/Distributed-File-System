package com.gfs.chunkserver.service;

import com.gfs.chunkserver.model.RequestType;
import com.gfs.chunkserver.utils.JsonHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.ObjectInputStream;
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
        ExecutorService executorServiceForReads = Executors.newFixedThreadPool(4);
        ExecutorService executorServiceForWrites = Executors.newFixedThreadPool(1);
        HeartbeatServiceImpl.startHeartbeatForMaster();
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                log.info("Started Connection with Remote Socket Address : " + socket.getRemoteSocketAddress());
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

                String request = (String)objectInputStream.readObject();
                RequestType requestType = JsonHandler.convertStringToObject(request, RequestType.class);

                switch (requestType) {
                    case READ:
                        executorServiceForReads.execute(new HandleReadRequestTask(socket, objectInputStream));
                        break;
                    case WRITE:
                        executorServiceForWrites.execute(new HandleWriteRequestTask(socket, objectInputStream));
                }

                // TODO : Handle incoming data requests from clients
            } catch (Exception e) {
                log.error("Error:", e);
            }
        }
    }
}
