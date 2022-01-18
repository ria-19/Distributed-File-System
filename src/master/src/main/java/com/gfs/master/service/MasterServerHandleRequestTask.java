package com.gfs.master.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gfs.master.model.Chunk;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * created by nikunjagarwal on 16-01-2022
 */
@Data
@AllArgsConstructor
@Slf4j
public class MasterServerHandleRequestTask implements Runnable{
    private Socket socket;

    @Override
    public void run() {
        try {
            log.info("Remote Socket Address : " + socket.getRemoteSocketAddress());
            InputStream inputStream = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            String data = (String)objectInputStream.readObject();
            log.info("Data : {}", data);
            if(data.equals("chunkserver")){
                HeartbeatServiceImpl.updateHeartBeatOfServer(socket.getRemoteSocketAddress().toString());
            } else {
                // TODO : Send metadata to client
            }
            socket.close();
        } catch (Exception e) {
            log.error("error : ", e);
        }
    }
}
