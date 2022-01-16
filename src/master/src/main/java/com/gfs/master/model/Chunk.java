package com.gfs.master.model;

import lombok.Data;

import java.io.Serializable;

/**
 * created by nikunjagarwal on 16-01-2022
 */
@Data
public class Chunk implements Serializable {
    private static final long serialVersionUID = 5950169519310163575L;
    private String chunkHandle;
    private String data;
}
