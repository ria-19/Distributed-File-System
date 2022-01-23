package com.gfs.master.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

/**
 * created by nikunjagarwal on 22-01-2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class File {
    private String filename;
    // <offset, chunkhandle>
    HashMap<Integer, Integer> offsetChunkHandleMap;
}
