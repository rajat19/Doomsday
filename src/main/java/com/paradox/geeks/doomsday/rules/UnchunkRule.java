package com.paradox.geeks.doomsday.rules;

import com.paradox.geeks.doomsday.messages.GorOriginalResponse;
import com.paradox.geeks.doomsday.messages.GorReplayedResponse;
import com.paradox.geeks.doomsday.messages.GorRequest;
import com.paradox.geeks.doomsday.messages.GorResponse;

import java.util.Arrays;

public class UnchunkRule implements Rule{
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
        unchunkBody(replayedResponse);
    }

    private void unchunkBody(GorResponse response) {
        response.getHeader("Transfer-Encoding").ifPresent(encoding -> {
            if (encoding.equals("chunked")) {
                byte[] body = response.getBody();
                byte[] result = new byte[body.length]; //we'll truncate at the end
                int resultPos = 0;
                String chunkSizeStr = "";
                for (int i = 0; i < body.length; i++) {
                    if (body[i] == '\r') {

                        int chunksize = Integer.parseInt(chunkSizeStr, 16);
                        i += 2; //skip the \r\n (position i at at the first char of the chunk)
                        System.arraycopy(body, i, result, resultPos, chunksize); //write to result
                        resultPos += chunksize;
                        i += chunksize + 1; //position i at the \n after the chunk
                        chunkSizeStr = "";
                    } else {
                        chunkSizeStr += (char)body[i];
                    }
                }
                response.setBody(Arrays.copyOf(result, resultPos));
                response.removeHeader("Transfer-Encoding");
            }
        });
    }
}
