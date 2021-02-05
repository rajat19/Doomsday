package com.paradox.geeks.doomsday;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.paradox.geeks.doomsday.management.PropertiesConstants;
import com.paradox.geeks.doomsday.management.PropertiesManager;
import com.paradox.geeks.doomsday.messages.GorMessage;
import com.paradox.geeks.doomsday.messages.GorOriginalResponse;
import com.paradox.geeks.doomsday.messages.GorReplayedResponse;
import com.paradox.geeks.doomsday.messages.GorRequest;
import com.paradox.geeks.doomsday.rules.Rule;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Doomsday {
    private final Map<String, GorRequest> requestMap;
    private final Map<String, GorOriginalResponse> originalResponseMap;
    private final Map<String, GorReplayedResponse> replayedResponseMap;
    private final String dirname;
    private final Rule[] rules;
    private PropertiesManager propertiesManager = PropertiesManager.getInstance();
    private final String[] ignoredKeys;

    public Doomsday(String dirname, Rule... rules) {
        this.dirname = dirname;
        replayedResponseMap = new HashMap<>();
        originalResponseMap = new HashMap<>();
        requestMap = new HashMap<>();
        this.rules = rules;
        ignoredKeys = propertiesManager.getProperty(PropertiesConstants.IGNORED_KEYS).split(",");
    }

    public void handleLine(PrintStream out, PrintWriter results, GorMessage statement) {
        if (statement instanceof GorRequest) {
            requestMap.put(statement.getId(), (GorRequest) statement);
            handleRequest(out, (GorRequest) statement);
        }
        else if (statement instanceof GorOriginalResponse) {
            out.println(statement.asLine());
            if (replayedResponseMap.containsKey(statement.getId())) {
                handleOriginalResponse(requestMap.get(statement.getId()), (GorOriginalResponse) statement);
                replayedResponseMap.remove(statement.getId());
            } else {
                originalResponseMap.put(statement.getId(), (GorOriginalResponse) statement);
            }
        }
        else if (statement instanceof GorReplayedResponse) {
            out.println(statement.asLine());
            if (originalResponseMap.containsKey(statement.getId())) {
                handleReplayedResponse(results, (GorReplayedResponse) statement, originalResponseMap.get(statement.getId()), requestMap);
                originalResponseMap.remove(statement.getId());
            } else {
                replayedResponseMap.put(statement.getId(), (GorReplayedResponse) statement);
            }
        } else {
            throw new IllegalStateException("Unknown statement type");
        }
    }

    private void handleRequest(PrintStream requests, GorRequest statement) {
        for (Rule rule: rules) {
            if (rule.blockReplay(statement)) return;
        }
        for (Rule rule: rules) {
            rule.modifyRequestForReplay(statement);
        }
        requests.println(statement.asLine());
    }

    private void handleOriginalResponse(GorRequest request, GorOriginalResponse statement) {
        for (Rule rule: rules) {
            rule.handleOriginalResponse(request, statement);
        }
    }

    private void handleReplayedResponse(PrintWriter results, GorReplayedResponse statement, GorOriginalResponse
            originalResponse, Map<String, GorRequest> requests) {
        GorRequest request = requests.get(statement.getId());
        for (Rule rewriteRule : rules) {
            rewriteRule.handleReplayResponse(request, originalResponse, statement);
        }
        requests.remove(statement.getId());
        compare(request, originalResponse, statement, results);
    }

    private void compare(GorRequest request, GorOriginalResponse orig, GorReplayedResponse replay, PrintWriter results) {
        boolean differs = false;
        results.println(replay.getId());
        int original_status = orig.getStatus();
        int replay_status = replay.getStatus();
        if(replay_status == 904) return;

        if (orig.getStatus() != replay.getStatus()) {
            differs = true;
        }
        try{
            if (!Arrays.equals(orig.getBody(), replay.getBody())) {
                String leftJson = new String(orig.getBody(), StandardCharsets.UTF_8);
                String rightJson = new String(replay.getBody(), StandardCharsets.UTF_8);

                ObjectMapper mapper = new ObjectMapper();
                TypeReference<HashMap<String, Object>> type = new TypeReference<HashMap<String, Object>>() {};

                Map<String, Object> leftMap = mapper.readValue(leftJson, type);
                Map<String, Object> rightMap = mapper.readValue(rightJson, type);

                Map<String, Object> leftFlatMap = FlatMapUtil.flatten(leftMap);
                Map<String, Object> rightFlatMap = FlatMapUtil.flatten(rightMap);

                MapDifference<String, Object> difference = Maps.difference(leftFlatMap, rightFlatMap);

                StringBuilder keysOnLeft = new StringBuilder();
                difference.entriesOnlyOnLeft().forEach((key, value) -> buildKeysList(key, value, keysOnLeft));
                if(!keysOnLeft.toString().isEmpty()){
                    differs = true;
                    FileManager.writeUsingFileWriter(dirname, "Entries only in Original Response\n--------------------------");
                    FileManager.writeUsingFileWriter(dirname,replay.getId()+"--" + keysOnLeft.toString());
                }

                StringBuilder keysOnRight = new StringBuilder();
                difference.entriesOnlyOnRight().forEach((key, value) -> buildKeysList(key, value, keysOnRight));
                if(!keysOnRight.toString().isEmpty()){
                    differs = true;
                    FileManager.writeUsingFileWriter(dirname,"\n\nEntries only in Replayed Response\n--------------------------");
                    FileManager.writeUsingFileWriter(dirname,replay.getId()+"--" + keysOnRight.toString());
                }

                StringBuilder keysOnBoth = new StringBuilder();
                difference.entriesDiffering().forEach((key, value) -> buildKeysList(key, value, keysOnBoth));
                if(!keysOnBoth.toString().isEmpty()){
                    differs = true;
                    FileManager.writeUsingFileWriter(dirname, "\n\nEntries differing in both Response\n--------------------------");
                    FileManager.writeUsingFileWriter(dirname,replay.getId()+"--" + keysOnBoth.toString());
                    FileManager.writeUsingFileWriter(dirname,"UserId>>>"+request.getHeader("X-USER-ID"));
                }
            }
        }
        catch(Exception ignored){}
        if (differs) {
            FileManager.writeToFile(dirname, request, orig, replay, results);
        }
    }

    private void buildKeysList(String data, Object value, StringBuilder keySet) {
        try {
            for(String key : ignoredKeys){
                if(data!=null && ("---"+data).contains(key)){
                    return;
                }
            }
            keySet.append(data)
                    .append("------->[")
                    .append(value)
                    .append("]------")
                    .append(", ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
