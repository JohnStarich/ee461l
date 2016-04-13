package com.johnstarich.moviematcher.app;

/**
 * Created by johnstarich on 4/7/16.
 */
public class StaticFile {
    public final String content;
    public final String contentType;

    public StaticFile(String content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }
}
