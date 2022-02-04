package com.gfs.master.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @JsonIgnore
    private Date lastUpdated;
    @JsonIgnore
    private int versionNo;
}
