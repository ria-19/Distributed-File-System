package com.gfs.chunkserver.utils;

import com.gfs.chunkserver.service.HeartbeatServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service class to handle File Operations
 */
@Slf4j
@Service
public class FileHandlingService {

    private static String baseFileDirectoryPath;

    public FileHandlingService(@Value("${chunkserver.basePath}") String baseFileDirectoryPath) {
        FileHandlingService.baseFileDirectoryPath = baseFileDirectoryPath;
    }


    /**
     * Utility Function to read data from a file
     *
     * @param filepath Path of the file to read from
     * @throws IOException
     */
    public static String readFile(String filepath) throws IOException {
        log.info("Reading from file : {} ", Paths.get(baseFileDirectoryPath, filepath));
        Stream<String> lines = Files.lines(Paths.get(baseFileDirectoryPath, filepath));
        return lines.collect(Collectors.joining("\n"));
    }

    /**
     * Utility function to write data to a file
     *
     * @param filepath Path of the file to append data to
     * @param data {@link String} data to append to file
     */
    public static void writeFile(String filepath, String data) {
        String finalFilePath = Paths.get(baseFileDirectoryPath, filepath).toString();
        File file = new File(finalFilePath);
        try{
            file.createNewFile();
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(finalFilePath, true))) {
                bufferedWriter.write(data);
            } catch (IOException e) {
                log.error("IOException in FileHandlingService :: writeFile", e);
            }
        } catch (Exception e) {
            log.error("Error", e);
        }

    }
}
