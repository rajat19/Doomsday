package com.paradox.geeks.doomsday.rules;

import com.paradox.geeks.doomsday.messages.GorOriginalResponse;
import com.paradox.geeks.doomsday.messages.GorReplayedResponse;
import com.paradox.geeks.doomsday.messages.GorRequest;

public interface Rule {
    void modifyRequestForReplay(GorRequest request);
    boolean blockReplay(GorRequest request);
    void handleOriginalResponse(GorRequest request, GorOriginalResponse response);
    void handleReplayResponse(GorRequest request, GorOriginalResponse originalResponse, GorReplayedResponse replayedResponse);
}
