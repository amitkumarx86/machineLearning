package org.narratives.CoRefAnnotator;

import org.wsl.narratives.matrix.model.Matrix;
import org.wsl.narratives.matrix.resources.MatrixModificationResource;
import org.wsl.narratives.wsd.resources.WsdPerlScriptModule;
import edu.sussex.nlp.jws.JWS;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
 

@Path("/wsd")
public class WSDResource extends Thread{
	private static ArrayList<String> stopWordsList = null;
	private static final String STOP_WORDS_FILE_NAME = "stopWordsList.txt";
	private static final String SCRIPTS_PATH = "/src/main/webapp/";
	//private static final String STOP_WORDS_FILE = SCRIPTS_PATH + STOP_WORDS_FILE_NAME;
	private static final String STOP_WORDS_FILE = "/home/amitkumarx86/Downloads/stopWordsList.txt";
	private  String path = "";
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public WSDResource(){
		
	}
	
	

	private String phrase="";
	private HashMap<String, String> dataMap = new HashMap<String, String>();
	private ArrayList<String> plot1 = new ArrayList<String>();
	private ArrayList<String> plot2 = new ArrayList<String>();
	public double result = 0;
	
    public ArrayList<String> getPlot1() {
		return plot1;
	}

	public void setPlot1(ArrayList<String> plot1) {
		this.plot1 = plot1;
	}

	public ArrayList<String> getPlot2() {
		return plot2;
	}

	public void setPlot2(ArrayList<String> plot2) {
		this.plot2 = plot2;
	}

	public HashMap<String, String> getDataMap() {
		return dataMap;
	}

	public void setDataMap(HashMap<String, String> dataMap) {
		this.dataMap = dataMap;
	}

	public String getPhrase() {
		return phrase;
	}

	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}
	public void run(){
		System.out.println(this.getName()+" for wsd is created for lin and matrix comp...");
		
//		System.out.println(this.getPlot1()+" "+this.getPlot2());
//		ArrayList<String> plot1 = new ArrayList<String>();
//		ArrayList<String> plot2 = new ArrayList<String>();
//		plot1.add("father is saving the child and fights with demon");
//		plot1.add("father is saving the child and fights with demon");
//		plot2.add("hero is fights with villain and gets her girlfriend");
//		plot2.add("hero is fights with villain and gets her girlfriend");
//		this.result = callWsdHandler(plot1, plot2);
		
		
		try {
			try {
				stopWordsList = getStopWordList();
			} catch (Exception e) {
				e.printStackTrace();
			}
			//System.out.println("GETTING IMPORTANT PHRASES ");
			ArrayList<String> wsdPlot1=getImpPhrase(this.getPlot1());
			ArrayList<String> wsdPlot2=getImpPhrase(this.getPlot2());
			
			this.result = callWsdHandler(wsdPlot1, wsdPlot2,this.getName(),this.getPath());
			//this.result = callWsdHandler(this.getPlot1(), this.getPlot2(),this.getName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@SuppressWarnings("null")
	private ArrayList<String> getImpPhrase(ArrayList<String> plot22) {
		// TODO Auto-generated method stub
		ArrayList<String> to_return=new ArrayList<String>();
		for (String wsd1 : plot22)
		{
			String words[] = null;
			String temp=null;
			if (wsd1!= null) {
				//System.out.println("wsd11 :"+wsd11);
				words = getValidTokens(wsd1.split(" "));
				//disPhrases.put(ph1, disTokens);
			}
		
			
			if(words.length>0){
				temp=String.join(" ", words);
				//System.out.println("SHRA "+temp);
				if(temp!=null)
					to_return.add(temp);
				//System.out.println("SHRA2");
			}
			
		}
		//System.out.println("shraddha :"+to_return);
		return to_return;
	}

	// post method
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED) 
	@Produces(MediaType.TEXT_HTML)
	public String getWSD(@FormParam("phrase1") String phrase1, @FormParam("phrase2") String phrase2){
		//wsdHandler(phrase1, phrase2);
		return "Done";
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String getHelloWorld(){
		return "You get it!";
	}
	public static String cleanse(String term){
		String result = term.replaceAll("[-+.^:,.!]","");
		return result;
	}
	public static HashMap<String, String> callWSD(String phrase){
		WsdPerlScriptModule wsd = new WsdPerlScriptModule();
		HashMap<String, String> dataMap1 = wsd.callWsdScript(phrase);
		return dataMap1;
	}
	public static  double[][] wsdHandler(String wsd1, String wsd2, LinSimilarityClass lin, JWS ws, FileWriter f) throws IOException  {
		String[] phrase1Data = wsd1.split(" ");
		String[] phrase2Data = wsd2.split(" ");
		double[][] matchMatrix = new double[phrase1Data.length][phrase2Data.length];
		matchMatrix = new double[phrase1Data.length][phrase2Data.length];
		
		for(int i=0;i<phrase1Data.length;i++){
			if(phrase1Data[i] != " "){
				String word1[] = phrase1Data[i].split("#");
				for(int j=0;j<phrase2Data.length;j++){
					if(phrase2Data[j] != " " ){
						String word2[] = phrase2Data[j].split("#");
						//System.out.println(word1[1] +" "+ word2[1]);
						if(word1[1].equals(word2[1]) && word1.length == 3 && word2.length == 3){
//							System.out.println(word1[0]+" "+word1[1]+" "+word1[2]+" "+word2[0]+" "+word2[1]+" "+word2[2]);
							try{
								if(word1[0].equals(word2[0])){
									matchMatrix[i][j] = 1.0;
								}
								else if((word1[1].equals("v") || word2[1].equals("v")) || (word1[1].equals("n") || word2[1].equals("n"))){								
									double temp = 0;
									//System.out.println(this.getWs()+" "+word1[0]+" "+ Integer.parseInt(word1[2])+" "+ word2[0]+" "+Integer.parseInt(word2[2])+" "+ word1[1]);
									temp = lin.calculateLin(ws,word1[0], Integer.parseInt(word1[2]), word2[0],Integer.parseInt(word2[2]), word1[1]);
									matchMatrix[i][j] = Math.abs(temp);
								}
							}
							catch(Exception e) {
								System.out.println(e.toString());
							}
						}
					}
				}
			}
		}// for loop closes
		 
		if(matchMatrix.length>0){
		f.write("\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n");
    	//f.write("wsd1 :"+phrase1Data+"\nwsd2: "+phrase2Data+"\n");
    	try {
			//f.write("\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n");
	    	//f.write("wsd1 :"+wsd1+"\nwsd2: "+wsd2+"\n");
    		for(int j1=0;j1<matchMatrix[0].length;j1++)
    			f.write(" "+phrase2Data[j1]+" ");
    		
    		f.write("\n");
	    	for(int i1=0;i1<matchMatrix.length;i1++){
	    		f.write(phrase1Data[i1]+" ");
	    		for(int j1=0;j1<matchMatrix[0].length;j1++){
	    			f.write(" "+matchMatrix[i1][j1]);
	    		}
	    		f.write("\n");
	    	}
	    	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		return matchMatrix;
	}// function closes
    
	// this function is calling function to get wsd 
	public static double callWsdHandler(ArrayList<String> wsdPlot1, ArrayList<String> wsdPlot2, String threadName , String path) throws IOException{
		//JWS ws = new JWS(WORDNET_PATH,"3.0");
		FileWriter f=null;
		try {
			f = new FileWriter(path+""+threadName+".log");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(threadName+" "+wsdPlot1);
		//System.out.println(threadName+" "+wsdPlot2);
		
		CallWSD wsdR = new CallWSD();
		JWS ws1 = wsdR.getWs(); 
		LinSimilarityClass lin = new LinSimilarityClass();
		
		double[][] mainMatrix = new double[wsdPlot1.size()][wsdPlot2.size()];
		//System.out.println(wsdPlot1.size()+" "+wsdPlot2.size());
		int i=0, j=0;
		double value =0;
		for (String wsd1 : wsdPlot1)
		{
		    j=0;
		    for (String wsd2 : wsdPlot2)
			{
			    // call wsd and get the final matrix
		    	//System.out.println(wsd2+"jhjg");
			    double[][] matchMatrix = wsdHandler(wsd1, wsd2, lin, ws1, f);
			    
			    //printMatrix(matchMatrix);
			    if(matchMatrix.length > 0 )
			    	value = callMatrixComp(matchMatrix);//callMatrixComputation(matchMatrix);
			    else
			    	value = 0;
			    
			    f.write("small matrix value calculation : "+value+"\n");
		    	f.write("\n");
			    mainMatrix[i][j++]=value;
			    //System.out.print(value+" ");
			}
		    
		    //System.out.println((i+1)+" Level Mathcing is done");
		    i++;  
		}
		
		// here we will get the final score
		if(mainMatrix.length > 0 )
			value = callMatrixComp(mainMatrix);//callMatrixComputation(mainMatrix);
		
		
		try {
			f.write("Main Matrix Calculation...-----------------------------\n");
			for(int i1=0;i1<mainMatrix.length;i1++){
	    		for(int j1=0;j1<mainMatrix[0].length;j1++)	    			
					f.write(" "+mainMatrix[i1][j1]);
				f.write("\n");
	    	}
			f.write("main matrix value calculation : "+value+"\n");
			f.close();
		}
    	catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 		}
//		System.out.println(value);
		
		return value;
	}
	
	public static double  callMatrixComp(double[][] matrix){
		int r = matrix.length;
		int c = matrix[0].length;
		double sum =0;
		for(int i=0 ; i < r ; i++){
			for(int j=0; j < c ; j++){
				sum += matrix[i][j];
			}
		}
		if(r != 0 && c != 0)
			return sum/(r*c);
		else 
			return sum;
	}
	
	
	// this function is calling matrix module
	public static double callMatrixComputation(double[][] matchMatrix){
		MatrixModificationResource m = new MatrixModificationResource();
		Matrix matrixObject = new Matrix();
		
		matrixObject.set_row_size(matchMatrix.length);
		
		matrixObject.set_col_size(matchMatrix[0].length);
		matrixObject.setMatrix(matchMatrix);
		
		double sum = 0;
		try {
			sum = m.deleterow_and_col_method1(matrixObject);
			//System.out.println("SUM IS "+sum);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sum;
	}
	public static void printMatrix(double[][] matrix){
		for(int i=0;i<matrix.length;i++){			 
				for(int j=0;j<matrix[0].length;j++){
					System.out.print(matrix[i][j]+" ");
				}
				System.out.println();
		}
	}
	
	
	
	/**function added to ignre stop words
	 * 
	 */
	private static String[] getValidTokens(String[] split) {
		ArrayList<String> validTokens = new ArrayList<String>();
		for (String tk : split) {
			//System.out.println(" here it is :"+tk);
			
			if (stopWordsList.contains(getWordFromSenseString(tk))) {
			//System.out.println("shraddha");
				continue; // Ignore stop words.
			}
			if (tk.contains("#v#") || tk.contains("#n#")) {
				validTokens.add(tk);
			}
		}
		String[] dsf = new String[validTokens.size()];
		return validTokens.toArray(dsf);
	}
	private static String getWordFromSenseString(String sense) {
		int endIdx = -1;
		if ((endIdx = sense.indexOf("#")) != -1) {
			return sense.substring(0, endIdx);
		}
		return sense;
	}
	private static ArrayList<String> getStopWordList() throws Exception {
		ArrayList<String> stopWordsList = new ArrayList<String>();
		FileInputStream fis = new FileInputStream(STOP_WORDS_FILE);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));

		String line = null;
		while ((line = br.readLine()) != null) {
			stopWordsList.add(line);
		}

		br.close();
		if (stopWordsList.size() == 0) {
			throw new Exception("Stop words list not found !!!!");
		}
		return stopWordsList;
	}
	private static void printTokens(String[] tokens) {
		for (String tk : tokens) {
			System.out.print(tk + " ");
		}
		System.out.println();

	}
	
	public static void main(String args[]) throws ClassNotFoundException, SQLException, IOException{
		ArrayList<String> plot1 = new ArrayList<String>();
		ArrayList<String> plot2 = new ArrayList<String>();
		
		plot1.add("father#n#4 be#v#1 saving#n#2 the#ND child#n#2 and#ND fight#n#1 with#ND demon#n#1");
		plot1.add("he#n#1 save#v#2 the#ND child#n#2 in_the_end#r#2");
		plot2.add("Ram#n#4 be#v#1 saving#n#2 the#ND child#n#2 and#ND fight#n#1 with#ND demon#n#1");
		plot2.add("he#n#1 can#n#5 not#r#1 save#v#2 the#ND child#n#2 in_the_end#r#2");
		
		
		callWsdHandler(plot1, plot2,"1","");
		
	}

}


