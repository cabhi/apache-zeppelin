package com.wizni;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class DataGenerator {
    public static void main(String[] args) {
	if(args.length != 2) {
	    System.err.println("Usage: java -cp data-generator-1.0-SNAPSHOT-jar-with-dependencies.jar"
	    	+ " com.wizni.DataGenerator <filename> <frequency>");
	    System.err.println("filename: file where data would be appended");
	    System.err.println("frequency: time interval after which new line will be appended");
	    System.exit(1);
	}
	    
	
	String file = args[0];
	try (Writer writer = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream(file, true), "utf-8"))) {
	   while(true) {
	     Thread.sleep(Integer.parseInt(args[1]));
	     writer.write(DataGeneratorHelper.getNewEntry());
	     writer.write("\n");
	     writer.flush();
	   }
	} catch (Exception ex) {
	    System.out.println(ex);
	}
    }

}
