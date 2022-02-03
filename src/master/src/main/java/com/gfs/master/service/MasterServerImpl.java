package com.gfs.master.service;

import com.gfs.master.model.Source;
import com.gfs.master.utils.JsonHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * created by nikunjagarwal on 16-01-2022
 * This class creates a server socket and listens on that
 */
@Component
@Slf4j
public class MasterServerImpl implements CommandLineRunner {

    @Value("${chunkservers.threadpoolsize}")
    private int chunkserversThreadPoolSize;
    @Value("${clients.threadpoolsize}")
    private int clientsThreadPoolSize;
    private final int maximumQueueLength;
    private final InetAddress masterServerHost;
    private final int masterServerPort;

    public MasterServerImpl(@Value("${masterserver.host}") String masterServerHost,
                            @Value("${masterserver.queuesize}") int maximumQueueLength,
                            @Value("${masterserver.port}") int masterServerPort) throws IOException {
        this.masterServerHost = InetAddress.getByName(masterServerHost);
        this.masterServerPort = masterServerPort;
        this.maximumQueueLength = maximumQueueLength;
    }

    /**
     * This function creates new socket when a source connects to it. Depending on the
     * type of source, it redirects the incoming request to respective executorservice.
     */
    @Override
    public void run(String... args) throws IOException{
        ServerSocket serverSocket = new ServerSocket(masterServerPort, maximumQueueLength, masterServerHost);
        log.info("Started Master server at : {}", serverSocket);
        ExecutorService executorServiceForChunkServer = Executors.newFixedThreadPool(chunkserversThreadPoolSize);
        ExecutorService executorServiceForClients = Executors.newFixedThreadPool(clientsThreadPoolSize);
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    log.info("Started Connection with Remote Socket Address : " + socket.getRemoteSocketAddress());
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                    log.info("Waiting to check Type of Connection");
                    // Assumption : The first message from the connected source will be of Source type
                    String request = (String) objectInputStream.readObject();
                    log.info("Type of connection : {}", request);
                    Source source = JsonHandler.convertStringToObject(request, Source.class);
                    switch (source) {
                        case CHUNKSERVER:
                            executorServiceForChunkServer.execute(new HandleChunkServerRequestTask(socket, objectInputStream));
                            break;
                        case CLIENT:
                            executorServiceForClients.execute(new HandleClientRequestTask(socket, objectInputStream, objectOutputStream));
                            break;
                        default:
                            log.error("Incorrect source");
                    }
                } catch (Exception e) {
                    log.error("Error :{}", e);
                }
            }
    }

}
