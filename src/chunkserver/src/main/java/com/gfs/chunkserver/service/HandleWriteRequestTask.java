package com.gfs.chunkserver.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ObjectInputStream;
import java.net.Socket;

@Data
@AllArgsConstructor
@Slf4j
public class HandleWriteRequestTask implements Runnable{
    private Socket socket;
    private ObjectInputStream objectInputStream;

    @Override
    public void run() {

    }
}
