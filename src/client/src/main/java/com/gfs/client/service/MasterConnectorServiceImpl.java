package com.gfs.client.service;

import com.gfs.client.model.RequestType;
import com.gfs.client.model.Response;
import com.gfs.client.model.Source;
import com.gfs.client.model.request.ClientRequest;
import com.gfs.client.utils.JsonHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * created by nikunjagarwal on 30-01-2022
 */
@Service
@Slf4j
public class MasterConnectorServiceImpl {
    @Value("${masterserver.host}")
    private String masterServerHost;

    @Value("${masterserver.port}")
    private int masterServerPort;

    public Response sendRequestToMaster(ClientRequest clientRequest, RequestType requestType) {
        log.info("Establishing connection and sending request to master={}:{}, clientRequest={}, requestType={}", masterServerHost,masterServerPort, clientRequest, requestType);
        Response response = new Response();
        try{
            Socket socket = new Socket(masterServerHost, masterServerPort);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(JsonHandler.convertObjectToString(Source.CLIENT));
            objectOutputStream.writeObject(JsonHandler.convertObjectToString(requestType));
            objectOutputStream.writeObject(JsonHandler.convertObjectToString(clientRequest));
            String responseString = (String) objectInputStream.readObject();
            response = JsonHandler.convertStringToObject(responseString, Response.class);
            socket.close();
            log.info("Response={}", response);
        } catch (Exception e){
            log.error("Error:",e);
        }
        return response;
    }


}
