package com.paradox.geeks.doomsday;

import java.io.File;

public class FileManager {
    public static void createDirectory(String dirname) {
        new File(dirname).mkdirs();
    }
}
