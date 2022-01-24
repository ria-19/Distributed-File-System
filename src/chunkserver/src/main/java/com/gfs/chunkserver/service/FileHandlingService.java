package com.gfs.chunkserver.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class FileHandlingService {
    private Socket socket;
    private String chunkHandle;

    public void readFile() throws IOException{
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        Stream<String> stream = Files.lines(Paths.get(chunkHandle));
        stream.forEach(obj -> {
            try {
                objectOutputStream.writeObject(obj);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    public void writeFile() {

    }
}
