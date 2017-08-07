package org.narratives.CoRefAnnotator;

	import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
	import java.util.ArrayList;
	import java.util.HashMap;
	import java.util.Iterator;
	import java.util.List;
	import java.util.Map;
	import java.util.Set;
	import java.util.TreeSet;
	
	import edu.stanford.nlp.coref.data.CorefChain;
	import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
//import edu.stanford.nlp.ling.Sentence;
//import edu.stanford.nlp.pipeline.CoreNLPProtos.Sentence;
	import edu.stanford.nlp.ling.SentenceUtils;
	import edu.stanford.nlp.trees.Tree;
	
	
	public class Extractor {
	
		
		private ArrayList<HashMap<String, Set<String>>> verbsExtracted = new ArrayList<HashMap<String, Set<String>>>();
		private ArrayList<HashMap<String, Set<String>>> phraseExtracted = new ArrayList<HashMap<String, Set<String>>>();
		public static String PTREE_SER_FILES = null; 
		public static String COREF_CHAINS_SER_FILES = null;
		public  String path="";
		
		private String plotNo = "";
		
		
		public  String getPath() {
			return path;
		}

		public  void setPath(String path) {
			this.path = path;
		}

		public String getPlotNo() {
			return plotNo;
		}

		public void setPlotNo(String plotNo) {
			this.plotNo = plotNo;
		}

		
	
	public  ArrayList<HashMap<String, Set<String>>> getEntityMap(String COREF_CHAINS_SER_FILES, String PTREE_SER_FILES, String path) {
			//System.out.println(text);
			System.out.println("---- nlp starts ---- ");
			Extractor instance = new Extractor();
			
			HashMap<String, Set<String>> actorMap = new HashMap<String, Set<String>>();
			Map<Integer, CorefChain> ccMap = instance.getCorefChain(COREF_CHAINS_SER_FILES, path);
			HashMap<String, Set<String>> map = new HashMap<String, Set<String>>();
			ArrayList<Tree> pTree = instance.getParseTree(PTREE_SER_FILES, path);
			
			int totalMentions = 0;
			int totalExtracted = 0;
			int entity_idx = 1;
			for (CorefChain cc : ccMap.values()) {
				//System.out.println("\t Rep mention : " + cc.getRepresentativeMention());
				Set<String> verbSet = new TreeSet<String>();
				Set<String> phraseSet = new TreeSet<String>();
				Iterator<Set<CorefMention>> mentionSet = cc.getMentionMap().values().iterator();
				while (mentionSet.hasNext()) {
					Set<CorefMention> mentn = mentionSet.next();
					for (CorefMention mtn : mentn) {
						++totalMentions;
						Tree sent = pTree.get(mtn.sentNum - 1);
						sent.setSpans();
						Iterator<Tree> lvs = sent.getLeaves().iterator();
						while (lvs.hasNext()) {
							Tree l = lvs.next();
							if (l.getSpan().elems()[0] == (mtn.startIndex - 1)) {
								int i = 1;
								 
								while (true) {
									Tree t = l.ancestor(i++, sent);
									if (t.label().value().equals("NP")) {
										 //System.out.println("**** Found NP "+t);
										// ****");
										List<Tree> sibs = t.siblings(sent);
										// System.out.println("Siblings length
										// :- "
										// + sibs.size());
										for (Tree sib : sibs) {
											if (sib.label().value().equals("VP") || sib.label().value().equals("S")) {
												totalExtracted++;
												String phrase = SentenceUtils.listToString(sib.yield());
												//System.out.println("Associated Phrase : " + phrase);
												//System.out.println(sib);
												verbSet.addAll(instance.printVerbs(sib));
												phraseSet.add(phrase);
				
											}
										}
									}
									// Print co-references part of VP
									if (t.label().value().equals("VP")) {
										//System.out.println("VP is t: "+t);
										totalExtracted++;
										String phrase = SentenceUtils.listToString(t.yield());
										//System.out.println("Associated Phrase : " + phrase);
										//System.out.println(t);
										//System.out.println(instance.printVerbs(t));
										verbSet.addAll(instance.printVerbs(t));
										phraseSet.add(phrase);
										break;
									}
									// Terminating condition for the loop
									if (t.label().value().equals("ROOT")) {
											break;
										}
									}
								}
							}
						}
					}
				
					String entityRepMention = cc.getRepresentativeMention().toString();
					map.put(entityRepMention, verbSet);
					actorMap.put(entityRepMention , phraseSet);
		
			}
	
		//System.out.println("Total Mentions," + totalMentions+",Total extracted," + totalExtracted);
		//System.out.println("Total extracted : " + totalExtracted);
		
		
		//System.out.println("***** Verb Map ***** : \n" + map);
		instance.verbsExtracted.add(map);
		instance.phraseExtracted.add(actorMap); 
		
		writePhraseFile(actorMap,this.getPlotNo(), path);		
		System.out.println("---- nlp ENDS ----");		
		return instance.phraseExtracted;
	}
	
	public static void writePhraseFile(HashMap<String, Set<String>> actorMap, String fileName, String path){
		FileWriter fw,fw1;
		try {
			fw = new FileWriter(path+fileName+".phrase");			
			// this one for list of phrase files
			fw1 = new FileWriter(path+"plotPhrase.list",true);
			fw1.write(path+fileName+".phrase"+"\n");			
			for(Set<String> s : actorMap.values()){
				Iterator<String> s1 = s.iterator();
				while(s1.hasNext()) fw.write(s1.next()+"\n");
			}
			fw1.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 
		
	}
	private Map<Integer, CorefChain> getCorefChain(String fileName, String path) {
		Map<Integer, CorefChain> map = null;

		try {
			FileInputStream fis = new FileInputStream(path + fileName);
			ObjectInputStream ois = new ObjectInputStream(fis);
			map = (Map<Integer, CorefChain>) ois.readObject();
			ois.close();
			fis.close();
			return map;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
		c.printStackTrace();
		return null;
		}
	}
	
	private ArrayList<Tree> getParseTree(String fileName, String path) {
		ArrayList<Tree> pTree = null;
	
		try {
			FileInputStream fis = new FileInputStream(path + fileName);
			ObjectInputStream ois = new ObjectInputStream(fis);
			pTree = (ArrayList<Tree>) ois.readObject();
			ois.close();
			fis.close();
			return pTree;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
			return null;
		}
	}
	
	private void extractNPVP(Tree t) {
		System.out.println("**** Extracting phrases *****");
		t.setSpans();
		List<Tree> tr = t.subTreeList();
		for (Tree f : tr) {
			if (f.label().value().equals("S")) {
				System.out.println("Tree ::: " + " Sentence ::: " + SentenceUtils.listToString(f.yield()));
				for (Tree ps : f.children()) {
					System.out.println("Tree ::: " + ps.label().value() + " :::5 " + SentenceUtils.listToString(ps.yield()));
					System.out.println("Span : " + ps.getSpan() + " --- " + ps.getSpan().elems()[0] + " --- "
							+ ps.getSpan().elems()[1]);
				}
			}
		}
		System.out.println("*****************************");
		// }
	}
	
	private List<String> printVerbs(Tree t) {
		List<String> ret = new ArrayList<String>();
		if (t.isLeaf()) {
			return null;
		}
		List<Tree> children = t.getChildrenAsList();
		for (Tree c : children) {
			//System.out.println("DFSSF"+c);
			if (c.label().value().startsWith("VB") || c.label().value().startsWith("RB")) {
					List<Tree> leafs = c.getLeaves();
					for (Tree nd : leafs) {
						ret.add(nd.label().value());
						//System.out.println("If wala :"+nd.label().value());
					}
					//System.out.println("SHRADDHA IF ");
				} else {
					List<String> st = printVerbs(c);
					if (st != null) {
						ret.addAll(st);

						//System.out.println("else wala :"+ret.addAll(st));
					}

					//System.out.println("SHRADDHA ELSE  ");
				}
			//System.out.println("LIS IS "+ret);
			}
			return ret;
		}
	
	public static void main(String []args){
		Extractor e = new Extractor();
		File fin = new File(e.path+"plot1.flag");
		FileInputStream fis;
		try {
			fis = new FileInputStream(fin);
			//Construct BufferedReader from InputStreamReader
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		 
			String line = null;
			try {
				COREF_CHAINS_SER_FILES  = br.readLine();
				PTREE_SER_FILES = br.readLine();
				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
				br.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		 
			
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		 
		
		System.out.println(COREF_CHAINS_SER_FILES+" "+PTREE_SER_FILES);
		Extractor ext = new Extractor();
		ext.getEntityMap(COREF_CHAINS_SER_FILES,PTREE_SER_FILES,e.path);
		
	}
	}
