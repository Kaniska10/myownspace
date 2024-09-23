package com.infosys.infytel.controller;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.infytel.dto.MoviesResponseDTO;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileLoader {

    public static void main(String[] args) throws IOException {
        //File file = new File("D:\\movies.json");
        FileReader file = new FileReader("D:\\movies.txt");
        JsonFactory jsonFactory = new JsonFactory();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        List<JsonNode> chunk = new ArrayList<>();
        MoviesResponseDTO moviesResponseDTO = new MoviesResponseDTO();
        int chunkSize = 4;

        try (JsonParser jsonParser = jsonFactory.createParser(file)) {
            // Move to the start of the array
            while (jsonParser.nextToken() != JsonToken.START_ARRAY) {
                jsonParser.nextToken();
            }

            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                JsonNode node = objectMapper.readTree(jsonParser);
                chunk.add(node);

                if (chunk.size() == chunkSize) {
                    moviesResponseDTO.setMovies(chunk);
                    processChunk(objectMapper.writeValueAsString(moviesResponseDTO));
                    chunk.clear();
                    moviesResponseDTO.clear();
                }
            }

            // Process any remaining elements
            if (!chunk.isEmpty()) {
                moviesResponseDTO.setMovies(chunk);
                processChunk(objectMapper.writeValueAsString(moviesResponseDTO));
            }
        }
    }

    private static void processChunk(String chunk) {
        // Process the chunk of JSON objects
        createFile(chunk);
        //System.out.println("Processing chunk: " + chunk);

    }

    public static void createFile(String chunk)
    {
        try {
            File myFile = new File("D:\\incoming\\movies_"+(new SecureRandom()
                    .nextInt(900000) + 100000)+"_"
                    + LocalDateTime.now().format(DateTimeFormatter
                    .ofPattern("yyMMddHHmmssSSS"))+".txt");
            if (myFile.createNewFile()) {
                System.out.println("File created: " + myFile.getName());
                writeToFile(myFile,chunk);
                StringBuilder data = new StringBuilder();
                //isValidJson(chunk);
                try {
                    Scanner myReader = new Scanner(myFile);
                    while (myReader.hasNextLine()) {
                        String dataLine = myReader.nextLine();
                        data.append(dataLine);
                    }
                    myReader.close();
                    System.out.println(data.toString().trim());
                    System.out.println("Valid json: " + isValidJson(data.toString().trim()));
                } catch (FileNotFoundException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                }


            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static boolean isValidJson(String jsonString) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jsonString);
            return true; // JSON is valid
        } catch (JsonProcessingException e) {
            return false; // JSON is invalid
        }
    }
    public static void writeToFile(File myFile,String chunk) {

        try{
            FileWriter myWriter = new FileWriter(myFile);
            myWriter.write(chunk.trim());
            myWriter.close();
        }catch(IOException ioException) {
            System.out.println("An error occurred.");
            ioException.printStackTrace();
        }
    }

    public static String getRandomNum()
    {
        SecureRandom secureRandom = new SecureRandom();
        int randomNumber = (secureRandom.nextInt(900000) + 100000); // Generates a number between 100000 and 999999
        return Integer.toString(randomNumber);
    }

}
