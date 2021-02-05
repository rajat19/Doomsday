package com.paradox.geeks.doomsday;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.paradox.geeks.doomsday.management.PropertiesConstants;
import com.paradox.geeks.doomsday.management.PropertiesManager;
import com.paradox.geeks.doomsday.messages.GorMessage;
import com.paradox.geeks.doomsday.messages.GorOriginalResponse;
import com.paradox.geeks.doomsday.messages.GorReplayedResponse;
import com.paradox.geeks.doomsday.messages.GorRequest;
import com.paradox.geeks.doomsday.rules.Rule;

import java.io.*;
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
    private String[] ignoredKeys;

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
            handleOriginalResponse(requestMap.get(statement.getId()), (GorOriginalResponse) statement);
        }
        else if (statement instanceof GorReplayedResponse) {
            out.println(statement.asLine());
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
        //writeUsingFileWriter(replay.getId());
        int original_status = orig.getStatus();
        int replay_status = replay.getStatus();
        if(  replay_status == 904)
        {
            // results.println("  ignorance is bliss");
            // writeUsingFileWriter(" ignorance is bliss");
            return;
        }

        if (orig.getStatus() != replay.getStatus()) {
            //    results.println(replay.getId()+"  had differing status");
            //     writeUsingFileWriter(replay.getId()+"  had differing status" + orig.getStatus() + "--" + replay.getStatus());
            differs = true;
        }
        try{
            if (!Arrays.equals(orig.getBody(), replay.getBody())) {
                // results.println("  had differing response bodies");
                // writeUsingFileWriter("  had differing response bodies");
//                differs = true;
                String leftJson = new String(orig.getBody(),"UTF8");
                String rightJson = new String(replay.getBody(),"UTF8");

                ObjectMapper mapper = new ObjectMapper();
                TypeReference<HashMap<String, Object>> type = new TypeReference<HashMap<String, Object>>() {};

                Map<String, Object> leftMap = mapper.readValue(leftJson, type);
                Map<String, Object> rightMap = mapper.readValue(rightJson, type);

                Map<String, Object> leftFlatMap = FlatMapUtil.flatten(leftMap);
                Map<String, Object> rightFlatMap = FlatMapUtil.flatten(rightMap);

                MapDifference<String, Object> difference = Maps.difference(leftFlatMap, rightFlatMap);


                // writeUsingFileWriter("Entries only in Original Response\n--------------------------");
                // difference.entriesOnlyOnLeft().forEach((key, value) -> writeUsingFileWriterWithValues("Original-" +key + ": " + value, request, orig, replay,results));
                StringBuilder keysOnLeft = new StringBuilder();
                difference.entriesOnlyOnLeft().forEach((key, value) -> buildKeysList(key, value, keysOnLeft));
                if(!keysOnLeft.toString().isEmpty()){
                    differs = true;
                    writeUsingFileWriter("Entries only in Original Response\n--------------------------");
                    writeUsingFileWriter(replay.getId()+"--" + keysOnLeft.toString());
                }

                // writeUsingFileWriter("\n\nEntries only in Replayed Response\n--------------------------");
                //    difference.entriesOnlyOnRight().forEach((key, value) -> writeUsingFileWriterWithValues("Replayed-" + key + ": " + value, request, orig, replay,results));

                StringBuilder keysOnRight = new StringBuilder();
                difference.entriesOnlyOnRight().forEach((key, value) -> buildKeysList(key, value, keysOnRight));
                if(!keysOnRight.toString().isEmpty()){
                    differs = true;

                    writeUsingFileWriter("\n\nEntries only in Replayed Response\n--------------------------");

                    writeUsingFileWriter(replay.getId()+"--" + keysOnRight.toString());
                }

                // writeUsingFileWriter("\n\nEntries differing\n--------------------------");
                // difference.entriesDiffering().forEach((key, value) -> writeUsingFileWriterWithValues("Diff-" + key + ": " + value, request, orig, replay,results));

                StringBuilder keysOnBoth = new StringBuilder();
                difference.entriesDiffering().forEach((key, value) -> buildKeysList(key, value, keysOnBoth));
                if(!keysOnBoth.toString().isEmpty()){
                    differs = true;

                    writeUsingFileWriter("\n\nEntries differing in both Response\n--------------------------");

                    writeUsingFileWriter(replay.getId()+"--" + keysOnBoth.toString());

                    writeUsingFileWriter("UserId>>>"+request.getHeader("X-USER-ID"));

                }
            }
        }
        catch(Exception ignored){}
        if (differs)
        {
            writeToFile(request, orig, replay, results);
        }
    }

    private void writeToFile(GorRequest request, GorOriginalResponse orig, GorReplayedResponse replayed, PrintWriter
            results) {
        try {
            String slash = File.separator;
            Files.write(request.getHttpBlock(results), new File(dirname + slash + request.getId() + "_req.txt"));
            Files.write(orig.getHttpBlock(results), new File(dirname + slash + request.getId() + "_orig.txt"));
            Files.write(replayed.getHttpBlock(results), new File(dirname + slash + request.getId() + "_repl.txt"));
        } catch (IOException e) {
            e.printStackTrace(results);
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

    private void writeUsingFileWriter(String data) {
        File file = new File(this.dirname + "/FileWriter.txt");
        FileWriter fr = null;
        try {
            for(String key : ignoredKeys){
                if(data!=null && ("---"+data).contains(key)){
                    return;
                }
            }

            fr = new FileWriter(file,true);
            fr.write(data + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            //close resources
            try {
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
