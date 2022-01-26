package com.gfs.chunkserver.model;

public enum RequestType {
    READ,
    READCHUNK,
    WRITE,
    WRITETOCACHE,
    WRITETOFILEFROMCACHE
}
