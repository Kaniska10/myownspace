package com.batch.process.app.fileprocessor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.batch.process.app.fileprocessor.service.FileprocessorService;
import com.batch.process.app.fileprocessor.service.impl.FileprocessorServiceImpl;

@SpringBootApplication
public class FileprocessorApplication implements CommandLineRunner {

	public static void main(String[] args) {

		SpringApplication.run(FileprocessorApplication.class, args);
		//SpringApplication.run(FileprocessorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		FileprocessorService fileprocessorService = new FileprocessorServiceImpl();
		fileprocessorService.processFiles();
		
	}
}
