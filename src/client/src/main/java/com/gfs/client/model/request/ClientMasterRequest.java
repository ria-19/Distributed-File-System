package com.gfs.client.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientMasterRequest extends ClientRequest{
    private String filename;
    private Integer offset;
}
