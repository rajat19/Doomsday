package com.paradox.geeks.doomsday.rules;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.paradox.geeks.doomsday.messages.GorOriginalResponse;
import com.paradox.geeks.doomsday.messages.GorReplayedResponse;
import com.paradox.geeks.doomsday.messages.GorRequest;

public class StoreAuthRule implements Rule{

    private final Cache<String, String> authentications;

    public StoreAuthRule() {
        authentications = CacheBuilder.newBuilder().maximumSize(1000).build();
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
        if (request.getPath().equals("/v2.1/authenticate")) {
            replayedResponse.getHeader("Authorization").ifPresent(authHeader ->
                    originalResponse.getHeader("Authorization").ifPresent(origHeader ->
                            authentications.put(origHeader, authHeader)
                    )
            );
        }
    }
}
