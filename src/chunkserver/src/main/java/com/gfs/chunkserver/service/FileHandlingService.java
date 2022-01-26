package com.gfs.chunkserver.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
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
     * @param filepath Path of the file to read from
     * @throws IOException
     */
    private final static String basePath = "./src/chunkserver/x";

    public static String readFile(String filepath) throws IOException {
        log.info("Reading from file : {} ", Paths.get(basePath, filepath).toString());
        Stream<String> lines = Files.lines(Paths.get(basePath, filepath));
        return lines.collect(Collectors.joining("\n"));
    }

    /**
     * Utility function to write data to a file
     *
     * @param filepath Path of the file to append data to
     * @param data {@link String} data to append to file
     */
    public static void writeFile(String filepath, String data) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(Paths.get(basePath, filepath).toString(), true))) {
            bufferedWriter.write(data);
        } catch (IOException e) {
            log.error("IOException in FileHandlingService :: writeFile", e);
        }
    }
}
