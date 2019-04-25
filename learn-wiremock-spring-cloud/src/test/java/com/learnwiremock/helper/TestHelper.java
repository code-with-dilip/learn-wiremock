package com.learnwiremock.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.collect;

public class TestHelper {

    private static String filePath = "src/test/resources/data/";

    public static String readFromPath(String fileName){
        String data=null;
        try (Stream<String> stream = Files.lines(Paths.get(filePath+fileName))) {
            data = stream
                    .map((line) -> line.trim())
                    .collect(Collectors.joining());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        //System.out.println(readTextFromPath("multiple_users_response.json"));
        System.out.println(readFromPath("multiple_users_response.json"));
    }

}
