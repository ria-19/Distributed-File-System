package com.gfs.master.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.Socket;

/**
 * created by nikunjagarwal on 21-01-2022
 * This class handles all requests from Clients
 */
@Data
@AllArgsConstructor
public class HandleClientRequestTask implements Runnable{

    private Socket socket;
    @Override
    public void run() {
        // TODO : Send metadata to client
    }
}
