package org.wsl.narratives.wsd.resources;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class LINPerlScriptModule {
	static String perlPATH = "/home/amitkumarx86/Documents/projects/narratives/code/Narratives/perlFiles/";
	public String callLINScript(String word1, String word2) {
		 
		String temp="";
		try
		{

			String params = word1+" n 1 "+word2+" n 1";
			ProcessBuilder pb = new ProcessBuilder("perl", 
					perlPATH+"lin.pl",
					params);
			
 
			Process process = pb.start();
	        process.waitFor();
	        BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        String temp1="";
	         
	        while((temp1 = output.readLine()) != null){
	            temp=temp+temp1;
	             
	        }
			 

//			if(process.exitValue() == 0)
//				System.out.println("Command Successful");
//			else
//			System.out.println("Command Failure");
			
		}
		catch(Exception e){
			temp += "Exception: "+ e.toString();
//			System.out.println("Exception: "+ e.toString());
		}
		return temp;
	}
	public static void main(String[] args){
		LINPerlScriptModule w = new LINPerlScriptModule();
		System.out.println(w.callLINScript("cat","bat"));
	}
}
