package com.gfs.client.model.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientChunkserverWriteRequest extends ClientRequest {
    private String chunkHandle;
    private String data;
}
