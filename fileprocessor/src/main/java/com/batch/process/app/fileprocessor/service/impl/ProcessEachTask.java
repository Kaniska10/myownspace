package com.batch.process.app.fileprocessor.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Scope("prototype")
public class ProcessEachTask implements Runnable {

    private String filePath;
    private int sleepInterval;
    private int retryCount;
    private File file;

    public ProcessEachTask(String filePath, int sleepInterval, int retryCount, File file) {
        this.filePath = filePath;
        this.sleepInterval = sleepInterval;
        this.retryCount = retryCount;
        this.file = file;
    }

    @Override
    public void run() {
        System.out.println(filePath + " is being executed by " + Thread.currentThread().getName());
        
        Scanner myReader;
        StringBuilder data = new StringBuilder();
        
		try {
				myReader = new Scanner(file);
	            
	            while (myReader.hasNextLine()) {
	                String dataLine = myReader.nextLine();
	                data.append(dataLine);
	            }
	            myReader.close();
	            file.delete();

            
            if(isValidJson(data.toString().trim()))
            {
            	System.out.println(filePath + data.toString().trim() + Thread.currentThread().getName());
            }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        try {
            Thread.sleep(sleepInterval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(filePath + " has completed execution.");
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
	
}
