package com.paradox.geeks.doomsday.rules;

import com.paradox.geeks.doomsday.messages.GorOriginalResponse;
import com.paradox.geeks.doomsday.messages.GorReplayedResponse;
import com.paradox.geeks.doomsday.messages.GorRequest;

public class IgnoreStaticRule implements Rule{
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

    }
}
