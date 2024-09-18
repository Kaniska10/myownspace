package com.batch.process.app.fileprocessor.service.impl;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.batch.process.app.fileprocessor.service.FileprocessorService;

@Service
public class FileprocessorServiceImpl implements FileprocessorService {

	@Override
	public void processFiles() {
		// TODO Auto-generated method stub
		File inputFile = new File("D:\\incoming\\");
		
		File[] listOfFiles = inputFile.listFiles();
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
		if((listOfFiles != null) && (listOfFiles.length > 0))
		{
			System.out.println("Files are there");
			try {
				createTasks(listOfFiles,executor);
			} catch (FileNotFoundException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Files are not there");
			try {
				Thread.sleep(30000l);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

	}
	
	public static void createTasks(File[] listOfFiles, ThreadPoolExecutor executor ) throws InterruptedException, FileNotFoundException{
		
        // Submit tasks to the executor
		int i = 0;
		for (File file : listOfFiles)
		{
        	if (!file.exists()) {
        		break;
        	}
            Runnable task = new ProcessEachTask("Task " + i,2000,5,file);
            executor.execute(task);
			i++;
		}
		
       /* for (int i = 0; i < 10; i++) {

        }*/
	}

}
