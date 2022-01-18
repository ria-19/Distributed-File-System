package com.gfs.chunkserver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * created by nikunjagarwal on 16-01-2022
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chunk {
   private String chunkHandle;
   private String data;
}
