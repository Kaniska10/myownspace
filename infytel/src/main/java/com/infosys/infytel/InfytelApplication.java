package com.infosys.infytel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class InfytelApplication {

	public static void main(String[] args) {
		SpringApplication.run(InfytelApplication.class, args);
	}

	//Method to print prime number between 1 to 100
	public void printPrimeNumber() {
		int i, j, flag;

		// Print display message
		System.out.println("Prime numbers between 1 and 100 are: ");
		// Traverse each number from 1 to 100
		for (i = 1; i <= 100; i++) {
			// Skip 0 and 1 as they are not prime numbers
			if (i == 1 || i == 0)
				continue;
			// flag variable to tell if i is prime or not
			flag = 1;
			// Traverse each number from 2 to (i/2)
			for (j = 2; j <= i / 2; ++j) {
				// If i is divisible by any number other than 1 and self
				// then i is not prime number
				if (i % j == 0) {
					flag = 0;
					break;
				}
			}
			// flag = 1 means i is prime
			// and flag = 0 means i is not prime
			if (flag == 1)
				System.out.println(i);
		}
	}

	//Method to convert a HashMap of Map<String, String> to a json string
	public String convertMapToJsonString() {
		Map<String, String> map = new HashMap<>();
		map.put("name", "John");
		map.put("age", "30");
		map.put("city", "New York");
		map.put("country", "USA");
		ObjectMapper objectMapper = new ObjectMapper();
		String jsonString = null;
		try {
			jsonString = objectMapper.writeValueAsString(map);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonString;
	}



}
