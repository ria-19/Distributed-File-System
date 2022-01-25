package com.gfs.chunkserver.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Service class to handle File Operations
 */
@Service
@AllArgsConstructor
@Slf4j
public class FileHandlingService {

    /**
     * Utility Function to read data from a file
     *
     * @param socket {@link Socket} object to send the read file to
     * @param filepath Path of the file to read from
     * @throws IOException
     */
    public static String readFile(Socket socket, String filepath) throws IOException{
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        Stream<String> stream = Files.lines(Paths.get(filepath));
        stream.forEach(obj -> {
            try {
                objectOutputStream.writeObject(obj);
            } catch (IOException e) {
                log.error("IOException in FileHandlingService :: readFile", e);
            }
            finally {
                try {
                    objectOutputStream.close();
                    socket.close();
                } catch (IOException e) {
                    log.error("Error in closing objectOutputStream at FileHandlingService :: readFile", e);
                }
            }
        });
        return null;
    }

    /**
     * Utility function to write data to a file
     *
     * @param socket {@link Socket} to write acknowledgement to
     * @param filepath Path of the file to append data to
     * @param data {@link String} data to append to file
     */
    public static void writeFile(Socket socket, String filepath, String data) {
//        Opening the file in append mode
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filepath, true));
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            bufferedWriter.write(data);

//            Writing 'true' to the outputStream to show command completion
            objectOutputStream.writeBoolean(true);
            socket.close();
        } catch (IOException e) {
            log.error("IOException in FileHandlingService :: writeFile", e);
        }
    }

    public static String readFile(String filepath){
        return null;
    }

    public static void writeFile(String filepath, String data){

    }
}
