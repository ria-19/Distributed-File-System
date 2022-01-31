package com.gfs.client.model.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientChunkserverReadRequest extends ClientRequest {
    private String chunkHandle;
}
