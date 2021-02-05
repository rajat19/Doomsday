package com.paradox.geeks.doomsday.messages;

import java.io.PrintWriter;

public class GorReplayedResponse extends GorResponse{
    public GorReplayedResponse(String line, byte[] gorSpec, String header, PrintWriter results) {
        super(line, gorSpec, header, results);
    }
}
