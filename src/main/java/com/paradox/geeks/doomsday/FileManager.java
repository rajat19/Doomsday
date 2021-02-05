package com.paradox.geeks.doomsday;

import com.google.common.io.Files;
import com.paradox.geeks.doomsday.management.PropertiesConstants;
import com.paradox.geeks.doomsday.management.PropertiesManager;
import com.paradox.geeks.doomsday.messages.GorOriginalResponse;
import com.paradox.geeks.doomsday.messages.GorReplayedResponse;
import com.paradox.geeks.doomsday.messages.GorRequest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileManager {
    public static void createDirectory(String dirname) {
        new File(dirname).mkdirs();
    }

    public static void writeToFile(String dirname, GorRequest request, GorOriginalResponse orig, GorReplayedResponse replayed, PrintWriter
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

    public static void writeUsingFileWriter(String dirname, String data) {
        PropertiesManager propertiesManager = PropertiesManager.getInstance();
        String fileWriterFilename = propertiesManager.getProperty(PropertiesConstants.FILE_WRITER_FILE_NAME);
        String[] ignoredKeys = propertiesManager.getProperty(PropertiesConstants.IGNORED_KEYS).split(",");

        File file = new File(dirname + File.separator + fileWriterFilename);
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
