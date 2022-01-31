package com.gfs.client.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * created by nikunjagarwal on 30-01-2022
 */
@Service
public class MasterConnectorServiceImpl {
    @Value("${masterserver.host}")
    private String masterServerHost;

    @Value("${masterserver.port}")
    private int masterServerPort;


}
