package com.gopaddle.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Readjson {

	/**
	 * @param args
	 */
	public static String readFileAsString(String path)
	{
		try {
			return readInputStreamAsString(new FileInputStream(path));
		} 
		catch (FileNotFoundException notFound) {
			System.out.println("Invalid file name : " + notFound.getMessage());
		}
		return null;
	}

		
		public static String readInputStreamAsString(InputStream stream)
		{
		  try 
		  {
			   BufferedReader read = new BufferedReader(new InputStreamReader(stream));
			   StringBuilder build = new StringBuilder();
			   String line = null;
			   while ( ( line = read.readLine() ) != null ) {
				   build.append(line);
			   }
			   try  { 
			    read.close(); 
			   }catch(IOException ignore){ 
			   }
			   return build.toString();
			  }
			  catch(IOException ioe) {
				  System.out.println("IO error : " + ioe.getLocalizedMessage());
			  }
		  	  catch(NullPointerException nullEx){
		  		System.out.println("Invalid data : " + nullEx.getMessage());
			  }
			  catch(Exception ex){
				  System.out.println("Unexpected Exception \n try this different way !!!" + ex.getMessage() );
		 	  }
			  return null;
		 }
		
		public static void stringWriteAsFile(String content, String fileName) 
		{
			try
			{
				int val = 0;
				byte bytes[] = new byte[1024];
				FileOutputStream opStream = new FileOutputStream(fileName);
				ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
				while(( val = inputStream.read(bytes)) != -1 ) {
					opStream.write(bytes, 0, val);
				}
				inputStream.close();
				opStream.close();
			}
			catch(FileNotFoundException notFound)
			{
				System.out.println("Invalid file name : " + notFound.getMessage());
			}
			catch(IOException ioe){
				System.out.println("Problem with file writing : " + ioe.getLocalizedMessage());
			}
	
		}
	}


