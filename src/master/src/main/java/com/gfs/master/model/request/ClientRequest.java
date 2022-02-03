package com.gfs.master.model.request;

import lombok.Data;

/**
 * created by nikunjagarwal on 21-01-2022
 */
@Data
public class ClientRequest extends ServerRequest {
    private String filename;
    private Integer offset;
}
