package com.paradox.geeks.doomsday.rules;

import com.google.common.io.ByteStreams;
import com.paradox.geeks.doomsday.messages.GorOriginalResponse;
import com.paradox.geeks.doomsday.messages.GorReplayedResponse;
import com.paradox.geeks.doomsday.messages.GorRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPInputStream;

public class GunzipRule implements Rule{

    private final PrintWriter result;

    public GunzipRule(PrintWriter result) {
        this.result = result;
    }

    @Override
    public void modifyRequestForReplay(GorRequest request) {

    }

    @Override
    public boolean blockReplay(GorRequest request) {
        return false;
    }

    @Override
    public void handleOriginalResponse(GorRequest request, GorOriginalResponse response) {

    }

    @Override
    public void handleReplayResponse(GorRequest request, GorOriginalResponse originalResponse, GorReplayedResponse replayedResponse) {
        replayedResponse.getHeader("Content-Encoding").ifPresent(encoding -> {
            if (encoding.equals("gzip")) {
                try {
                    replayedResponse.setBody(ByteStreams.toByteArray(
                            new GZIPInputStream(
                                    new ByteArrayInputStream(replayedResponse.getBody()))));
                } catch (IOException e) {
                    e.printStackTrace(result);
                }
                replayedResponse.removeHeader("Content-Encoding");
            }
        });
    }
}
