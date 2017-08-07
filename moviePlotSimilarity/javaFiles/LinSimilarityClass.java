package org.narratives.CoRefAnnotator;



import edu.sussex.nlp.jws.*;

public class LinSimilarityClass {
	public  double calculateLin(JWS wordnet, String word1, int synset1, String word2, int synset2, String pos){
		Lin linObj = wordnet.getLin();
		 
		return linObj.lin(word1, synset1, word2, synset2, pos);
	}

public static void main(String[] args) throws Exception {
	
	JWS ws = new JWS("/home/amitkumarx86/Downloads","3.0");
	double[][] matchMatrix = new double[0][1];
	String[] array = new String[] { "a", "b", "c" };
	String temp=String.join("   k", array);
	System.out.println(" "+temp);
	LinSimilarityClass l = new LinSimilarityClass();
//	System.out.println("  "+l.calculateLin(ws, "school", 4, "extract", 2, "n"));
//	System.out.println("  "+l.calculateLin(ws, "trip", 1, "extract", 2, "n"));
//	System.out.println("  "+l.calculateLin(ws, "school", 4, "family", 6, "n"));
//	
	System.out.println("  "+l.calculateLin(ws, "trip", 1, "family", 6, "n"));
	
//	System.out.println("  "+l.calculateLin(ws, "war", 1, "fight", 1, "v"));
		

}
}
