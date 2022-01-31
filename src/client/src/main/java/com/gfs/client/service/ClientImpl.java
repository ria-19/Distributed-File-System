package com.gfs.client.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ClientImpl implements CommandLineRunner {

    @Autowired
    ChunkserverConnectorServiceImpl chunkserverConnectorService;

    @Autowired
    MasterConnectorServiceImpl masterConnectorService;

    @Override
    public void run(String... args) {
         //TODO : Make requests to the master then to chunkserver based on REST APIs
    }


    public void readChunkData(String filename, int offset) {
        // Call master connector service for metadata
        // Call chunk connector service for data
    }

    public void writeChunkData(String filename, String data) {
        // implement breaking file into chunks
        // Call master connector service for metadata
        // Call chunk connector service for data
    }



}
