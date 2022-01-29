package com.gfs.master.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * created by nikunjagarwal on 22-01-2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Location {
    private String chunkserverUrl;
    private Date lastUpdated;
    private int versionNo;
}
