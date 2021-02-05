package com.paradox.geeks.doomsday;

import com.paradox.geeks.doomsday.management.PropertiesConstants;
import com.paradox.geeks.doomsday.management.PropertiesManager;
import com.paradox.geeks.doomsday.messages.GorMessage;
import com.paradox.geeks.doomsday.rules.IgnoreDateDifferenceRule;
import com.paradox.geeks.doomsday.rules.IgnoreStaticRule;

import java.io.*;
import java.util.Scanner;

public class Application {
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        PropertiesManager propertiesManager = PropertiesManager.getInstance();

        String outputDirname = propertiesManager.getProperty(PropertiesConstants.OUTPUT_DIRNAME);
        if (args.length > 0) {
            outputDirname = args[0];
        }

        FileManager.createDirectory(outputDirname);
        PrintWriter results = new PrintWriter(outputDirname + File.separator + "log", "UTF-8");
        Doomsday doomsday = new Doomsday(
                outputDirname,
                new IgnoreStaticRule(),
                new IgnoreDateDifferenceRule()
        );

        Scanner sc = new Scanner(System.in);
        PrintStream out = System.out;

        while (sc.hasNext()) {
            try {
                GorMessage statement = GorMessage.make(sc.nextLine(), results);
                doomsday.handleLine(out, results, statement);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                results.flush();
            }
        }
    }
}
