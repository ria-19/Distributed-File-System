package com.gfs.master.service;

import lombok.extern.slf4j.Slf4j;
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
public class MasterServerImpl implements CommandLineRunner {

    ServerSocket serverSocket;

    public MasterServerImpl() throws IOException {
        serverSocket = new ServerSocket(8018);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Server started");
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                executorService.execute(new MasterServerHandleRequestTask(socket));
            }
        } catch (Exception e) {
            log.error("Error :{}", e);
            serverSocket.close();
        }

    }
}
