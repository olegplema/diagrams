package org.plema.services;

import java.io.*;

public class FileService {
    public static void writeFile(String filePath, String content) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
