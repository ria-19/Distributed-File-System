package com.gfs.master.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gfs.master.model.ClientChunksMetadata;
import com.gfs.master.model.ServerRequest;
import com.gfs.master.utils.JsonHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

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
            String request = (String)objectInputStream.readObject();
            ServerRequest serverRequest = JsonHandler.convertStringToObject(request, ServerRequest.class);
            log.info("Data : {}", request);
            switch(serverRequest.getSource()){
                case CHUNKSERVER:
                    ClientChunksMetadata clientChunksMetadata = JsonHandler.convertObjectToOtherObject(serverRequest.getRequest(), ClientChunksMetadata.class);
                    HeartbeatServiceImpl.updateHeartBeatOfServer(socket.getRemoteSocketAddress().toString(), clientChunksMetadata);
                    break;
                case CLIENT:
                    // TODO : Send metadata to client
                    break;
                default:
                    log.error("Incorrect source");
            }
            socket.close();
        } catch (Exception e) {
            log.error("error : ", e);
        }
    }
}
