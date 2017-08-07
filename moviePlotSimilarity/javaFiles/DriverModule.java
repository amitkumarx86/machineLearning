package org.narratives.CoRefAnnotator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.wsl.narratives.wsd.resources.WsdPerlScriptModule;


@Path("/corefannotator")
public class DriverModule {
	
	private  String path = "/home/amitkumarx86/Documents/projects/narratives/code/Narratives/files/sets/Set12/";
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	public String processing(@FormParam("coRefText1")String inputText1,@FormParam("coRefText2")String inputText2, @FormParam("coRefText3")String inputText3) throws Exception{
		
		// get the start time
		long startTime = System.nanoTime();
		// reinitialize phrase list file
		System.out.println(inputText1);
		FileWriter fw = new FileWriter(path+"plotPhrase.list");
		fw.close();		
		CoRefAnnotator pl1 = new CoRefAnnotator();
		CoRefAnnotator pl2 = new CoRefAnnotator();
		CoRefAnnotator pl3 = new CoRefAnnotator();
		
		
		pl1.setTextInput(inputText1);
		pl1.setPath(path);
		pl1.setPlotNo("1");
		pl1.setCorefFileName("coRefChain1.ser");
		pl1.setPTreeFileName("pTree1.ser");
		
		
		pl2.setTextInput(inputText2);
		pl2.setPath(path);
		pl2.setPlotNo("2");
		pl2.setCorefFileName("coRefChain2.ser");
		pl2.setPTreeFileName("pTree2.ser");
		
		
		pl3.setTextInput(inputText3);
		pl3.setPath(path);
		pl3.setPlotNo("3");
		pl3.setCorefFileName("coRefChain3.ser");
		pl3.setPTreeFileName("pTree3.ser");
		
		pl1.start();
		pl2.start();
		pl3.start();
		
		pl1.join();
		pl2.join();
		pl3.join();
		
		long endTime = System.nanoTime();
		System.out.println("Took "+((endTime - startTime)/Math.pow(10,9)) + " sec for nlp && phrase extraction module"); 
		// call wsd module and matrix module
		
	
 		startTime = System.nanoTime();
 		
 		// read plotphrase list file and get the plot
 		ArrayList<String> temp2 = readPlotFile(path+"plotPhrase.list");
 		Iterator<String> t = temp2.iterator();
 		System.out.println(temp2);
 		ArrayList<String> plot1Phrase = null;
		while(t.hasNext()){
 			String temp = t.next();
 			if(temp.indexOf("plot1")>=0){
 				plot1Phrase = readPlotFile(temp);
 				
 				break;
			}
 		}
		
		t = temp2.iterator();
		ArrayList<String> plot2Phrase = null;
		while(t.hasNext()){
 			String temp = t.next();
 			if(temp.indexOf("plot2")>=0){
 				plot2Phrase = readPlotFile(temp);
 				
 				break;
 			}
 		}
		
		t = temp2.iterator();
		ArrayList<String> plot3Phrase = null;
		while(t.hasNext()){
 			String temp = t.next();
 			if(temp.indexOf("plot3")>=0){
 				plot3Phrase = readPlotFile(temp);
 				
 				break;
 			}
 		}
		
		System.out.println(plot1Phrase);
		System.out.println(plot2Phrase);
		System.out.println(plot3Phrase);
		
		
		/****************************************************************
		 * @author amitkumarx86
		 * inner class to call wsd in parallel
		 ****************************************************************/
		// get wsd for each plot
		ArrayList<String> wsdPlot1 = new ArrayList<String>();
		ArrayList<String> wsdPlot2 = new ArrayList<String>();
		ArrayList<String> wsdPlot3 = new ArrayList<String>();
		
		
		class GetWsd extends Thread{
			ArrayList<String> wsdPlotPhrase = new ArrayList<String>();
			ArrayList<String> plotPhrase = new ArrayList<String>();
			
			public void run(){
				System.out.println(this.getName()+" created for wsd");
				WsdPerlScriptModule wsd = new WsdPerlScriptModule();
				for(String phrase: plotPhrase){
					wsdPlotPhrase.add(wsd.callWsdScript(phrase).get("wsd"));
				}
			}
		}
		
		GetWsd g1=new GetWsd();
		g1.plotPhrase=plot1Phrase;
		g1.start();
		wsdPlot1 = g1.wsdPlotPhrase;
		
		GetWsd g2=new GetWsd();
		g2.plotPhrase=plot2Phrase;
		g2.start();
		wsdPlot2 = g2.wsdPlotPhrase;
		
		GetWsd g3=new GetWsd();
		g3.plotPhrase=plot3Phrase;
		g3.start();
		wsdPlot3 = g3.wsdPlotPhrase;
		
		g1.join();
		g2.join();
		g3.join();
		
		System.out.println(wsdPlot1);
		System.out.println(wsdPlot2);
		System.out.println(wsdPlot3);
		
		FileWriter f2 = new FileWriter(path+"WSD.log");
		for(String s : wsdPlot1) f2.write(s+"\n");
		f2.write("----------------------------------------\n");
		for(String s : wsdPlot2) f2.write(s+"\n");
		f2.write("---------------------------------------\n");
		
		//for(String s : wsdPlot3) f2.write(s+"\n");
		//f2.write("----------------------------------------");
		f2.write("\n........................WSD module is finished...........................\n");
		//f2.close();
		
		endTime = System.nanoTime();
		System.out.println("Took "+((endTime - startTime)/Math.pow(10,9)) + " sec for wsd module"); 
		/****************************************************************
		 * @author amitkumarx86
		 * inner class to call wsd for lin and matrix computation in parallel
		 ****************************************************************/
		
		startTime = System.nanoTime();
		
		WSDResource wsd1 = new WSDResource();
		WSDResource wsd2 = new WSDResource();
		WSDResource wsd3 = new WSDResource();
		
		wsd1.setPath(path);
		wsd1.setPlot1(wsdPlot1);
		wsd1.setPlot2(wsdPlot2);		

		wsd2.setPath(path);
		wsd2.setPlot1(wsdPlot1);
		wsd2.setPlot2(wsdPlot3);		

		wsd3.setPath(path);
		wsd3.setPlot1(wsdPlot2);
		wsd3.setPlot2(wsdPlot3);		
		
		wsd1.start();
		wsd2.start();
		wsd3.start();
		
		wsd1.join();
		wsd2.join();
		wsd3.join();
		
		// total processing time taken
		endTime = System.nanoTime();
		f2.write("Result  1 & 2 "+wsd1.result+"\n");
		f2.write("Result  1 & 3 "+wsd2.result+"\n");
		f2.write("Result  2 & 3 "+wsd3.result+"\n");
		f2.close();
		
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		System.out.println("Took "+((endTime - startTime)/Math.pow(10,9)) + " sec for score calculation."); 
		System.out.println("1  & 2 "+wsd1.result+" \n1 & 3 "+wsd2.result+" \n2 & 3 "+wsd3.result);
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		// create html result
		String result = "<hr><h3>Score between plot1 & plot2</h3>"+wsd1.result+"</br>";
		result += "<h3>Score between plot1 & plot3</h3>"+wsd2.result+"</br>";
		result += "<h3>Score between plot2 & plot3</h3>"+wsd3.result+"</br>"; 
		//System.out.println(result);
		return result;
	}
	
	public static ArrayList<String> readPlotFile(String fileName){
		ArrayList<String> result = new ArrayList<String>();
		File fin = new File(fileName);
		FileInputStream fis;
		try {
			fis = new FileInputStream(fin);
			//Construct BufferedReader from InputStreamReader
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		 
			String line = null;
			try {
				while ((line = br.readLine()) != null) {
					//System.out.println(line);
					result.add(line);
				}
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String testing(){
		System.out.print("get it");
		return "Working..";
	}
	
	public static void main(String[] args) throws Exception {
		DriverModule dr = new DriverModule();
		dr.processing("","","");
	}
}
