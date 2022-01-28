package com.gfs.chunkserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * created by nikunjagarwal on 23-01-2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    private String chunkserverUrl;
    private String chunkPath;
    private int versionNo;
}
