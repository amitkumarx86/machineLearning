package org.narratives.CoRefAnnotator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.Mention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.ArrayCoreMap;
import edu.stanford.nlp.util.CoreMap;


import org.narratives.InputFormat.CoRefInput;
//import org.narratives.InputFormat.GetInput;



 
public class CoRefAnnotator extends Thread{

	private CoRefInput input,output;
	private FileOutputStream fos = null;
	private FileInputStream fis = null;
	private ObjectInputStream ois = null;
	private ObjectOutputStream oos = null;
	private Annotation document;
	private Properties props;
	private StanfordCoreNLP pipeline;
	ArrayList result = new ArrayList();
	
	// variable set by Amit
	private String path = "";
	private String textInput = ""; // this variable takes plot
	private String plotNo = "";
	private String CorefFileName = "";
	private String PTreeFileName = "";
	
	
	
	
	public String getPath() {
		return path;
	}


	public void setPath(String path) {
		this.path = path;
	}


	public String getCorefFileName() {
		return CorefFileName;
	}


	public void setCorefFileName(String corefFileName) {
		CorefFileName = corefFileName;
	}


	public String getPTreeFileName() {
		return PTreeFileName;
	}


	public void setPTreeFileName(String pTreeFileName) {
		PTreeFileName = pTreeFileName;
	}


	public String getTextInput() {
		return textInput;
	}


	public void setTextInput(String textInput) {
		this.textInput = textInput;
	}


	public void saveFile (CoRefInput input) throws Exception
	{
			fos = new FileOutputStream(path+"coRefOut.ser");
			oos = new ObjectOutputStream(fos);
			oos.writeObject(input);	
			oos.close();
			fos.close();
	}
	
	
	public CoRefInput readFile() throws Exception
	{
		fis = new FileInputStream(path+"coRefOut.ser");
		ois = new ObjectInputStream(fis);
		CoRefInput output = (CoRefInput)ois.readObject();
		return output;
	}
	
	public void annotator(String inputSentence) throws Exception
	{
		document = new Annotation(inputSentence);
	    props = new Properties();
	    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,mention,coref");
	    pipeline = new StanfordCoreNLP(props);
	    pipeline.annotate(document);
	}
	
	public void posTreeProcessor() throws Exception
	{
		ArrayList<Tree> arr = new ArrayList<>();
		String res = "";
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    for(CoreMap sentence : sentences)
	    {
	    	
	    	for (CoreLabel token: sentence.get(TokensAnnotation.class)) {

	    		String word = token.get(TextAnnotation.class);
		        String pos = token.get(PartOfSpeechAnnotation.class);
		        res = res + word + "_"+pos+" ";
		      }
	    	Tree tree = sentence.get(TreeAnnotation.class);
	    	arr.add(tree);
	    }
	    System.out.println(res);
	    System.out.println(arr);
	    input.setPosOutputSentence(res);
	    input.setTreeSentence(arr);
	    
		
	}
	
	
	public void coRefProcessor () throws Exception
	{
		System.out.println(document);
		//System.out.println(document.get(CorefCoreAnnotations.CorefChainAnnotation.class));
			    
	    input.setChainLink(document.get(CorefCoreAnnotations.CorefChainAnnotation.class));
	    input.setSentenceMention(document.get(CoreAnnotations.SentencesAnnotation.class));
		
	}
	
	public String getPlotNo() {
		return plotNo;
	}


	public void setPlotNo(String plotNo) {
		this.plotNo = plotNo;
	}


	public void writePTree(String fileName) throws Exception
	{
		FileOutputStream fos = new FileOutputStream(path+fileName);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(input.getTreeSentence());
		fos.close();
		oos.close();
		FileInputStream fis = new FileInputStream(path+fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		ArrayList<Tree> treeSentence = (ArrayList<Tree>)ois.readObject();
		//CoRefInput output = (CoRefInput)ois.readObject();
		System.out.println("PTreee wrtiting");
		for (Tree cc : treeSentence) {
	        System.out.println("\t" + cc);
	      }
		fis.close();
		ois.close();
	}
	
	public void writeCoRef(String fileName) throws Exception
	{
		FileOutputStream fos = new FileOutputStream(path+fileName);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(input.getChainLink());	
		fos.close();
		oos.close();
		FileInputStream fis = new FileInputStream(path+fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		Map<Integer,CorefChain> chainLink = (Map<Integer,CorefChain>)ois.readObject();
		//CoRefInput output = (CoRefInput)ois.readObject();
		System.out.println("CoRefwrittimnhfgjs");
		for (CorefChain cc : chainLink.values()) {
	        System.out.println("\t" + cc);
	      }
		fis.close();
		ois.close();
	}
	public void writeFlagFile(String fileName){
		FileWriter fw;
		try {
			fw = new FileWriter(path+fileName);
			fw.write(path+this.getCorefFileName());
			fw.write(path+this.getPTreeFileName());
		 
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		
	}

	
	public void run(){
		System.out.println(this.getName()+" created for coref annotator");
		callCorefAndPtree(this.textInput, this.getCorefFileName(), this.getPTreeFileName(), this.getPlotNo());
		
	}
	private String callCorefAndPtree(String inputText, String coreFile, String pTreeFile, String pltNo){
		// TODO Auto-generated method stub
		//System.out.println("Hello");
		System.out.println(inputText);
		
		String inputSentence = inputText;
		ArrayList arr = new ArrayList();
		try{
			/*
			input = new CoRefInput();
			
			//String inputSentence = "Barrack Obama is president of US. He got elected in 2008. He ran for 8 years.";
			input.setInputSentence(inputSentence);
			System.out.println("started nlp module");
			//System.out.println(inputSentence);
			annotator(inputSentence);
			coRefProcessor();
			posTreeProcessor();
			//saveFile(input);
			//output = readFile();
			//Checking and Printing
			String res = "";
		
			// writing files
			writeCoRef(coreFile);
			writePTree(pTreeFile);
			*/
			
//			Extractor ex = new Extractor();
//			ex.getEntityMap("coRefChain.ser", "pTree.ser", path);
			
			
			
			/*
			for (CorefChain cc : output.getChainLink().values()) {
		        //System.out.println("\t" + cc);
		        res = res + cc + " ";
		      }
			arr.add(res);
		      for (CoreMap sentence : output.getSentenceMention()) {
		        //System.out.println("---");
		        //System.out.println("mentions");
		        for (Mention m : sentence.get(CorefCoreAnnotations.CorefMentionsAnnotation.class)) {
		          //System.out.println("\t" + m);
		          arr.add(m);
		         }
		      }
		      
		      result.add(input.getChainLink());
		      result.add(input.getSentenceMention());
		      result.add(input.getPosOutputSentence());
		      result.add(input.getTreeSentence());
		      
		      System.out.println(result);
		      */
		      // writing flag file
		      //writeFlagFile("plot"+this.getPlotNo()+".flag");
		      
		      // call phraseExtractor
		      Extractor ext = new Extractor();
		      ext.setPlotNo("plot"+pltNo);
		      ext.setPath(path);
		      ext.getEntityMap(this.getCorefFileName(),this.getPTreeFileName(),path);
		      
		}catch(Exception ex){
			
			ex.printStackTrace();
			
		}
		// call phrase extraction module
		
		return result.toString();
		
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String testing(){
		return "asfd";
		//System.out.print("get it");
	}
	public static void main(String[] args) {
		CoRefAnnotator pl1 = new CoRefAnnotator();
		
		pl1.setTextInput("Barak obama is the president of US. He ruled for 8 years.");
		pl1.setCorefFileName("coRefChain1.ser");
		pl1.setPTreeFileName("pTree1.ser");
		pl1.start();
	}
}
    
