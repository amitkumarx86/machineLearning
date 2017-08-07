package org.wsl.narratives.wsd.resources;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class WsdPerlScriptModule {
	static String perlPATH = "/home/amitkumarx86/Documents/projects/narratives/code/Narratives/perlFiles/";
	public String callWsdScript(String sentence) {
		 
		
		String temp="";
			   temp+= "<br><table class='table table-hover' style='width:80%'><tr><th>Lemma</th><th>Sense</th></tr>"; 
			   
			   
			try
			{
				ProcessBuilder pb = new ProcessBuilder("perl", 
						perlPATH+"wsd.pl",
						sentence);
				
				Process process = pb.start();
				BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
				 
		        process.waitFor();
	
		        String temp1 = output.readLine();
		         
		        String[] sen = sentence.split(" ");
		        String[] values = temp1.split(" ");
		        for(int i=0;i<sen.length; i++)
		        	//System.out.println(sen[i]+" "+values[i]);
		        	temp +="<tr><td>"+sen[i] +"</td><td>"+values[i]+"</td></tr>";
			        
		        
		         
	//			if(process.exitValue() == 0)
	//				System.out.println("Command Successful");
	//			else
	//			System.out.println("Command Failure");
				
			}
			catch(Exception e){
				temp += "Exception: "+ e.toString();
	//			System.out.println("Exception: "+ e.toString());
			}
			
		
		temp += "</table>";
		return temp;
		
	}
	public static void main(String[] args){
		WsdPerlScriptModule w = new WsdPerlScriptModule();
		System.out.println(w.callWsdScript("I am going to school"));
	}
}
