package com.gfs.chunkserver.utils;


/**
 * created by nikunjagarwal on 20-02-2022
 */
public class RetryUtils {
    private int numOfRetries;
    public RetryUtils(int numOfRetries) {
        this.numOfRetries = numOfRetries;
    }

    public boolean canRetry() {
        return numOfRetries > 0;
    }

    public void retry() {
        numOfRetries--;
    }

    public void stopRetry(){
        numOfRetries = 0;
    }
}
