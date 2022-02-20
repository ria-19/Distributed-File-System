package com.gfs.chunkserver.exception;

/**
 * created by nikunjagarwal on 20-02-2022
 */
public class ConnectionException extends Exception{
    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
