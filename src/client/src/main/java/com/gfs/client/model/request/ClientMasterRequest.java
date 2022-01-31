package com.gfs.client.model.request;

import lombok.Data;

@Data
public class ClientMasterRequest extends ClientRequest{
    private String filename;
    private Integer offset;
}
