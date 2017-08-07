package org.narratives.CoRefAnnotator;

import edu.sussex.nlp.jws.JWS;

public class CallWSD {
	private JWS ws;
	//private static String WORDNET_PATH = "src/main/webapp";
	private static String WORDNET_PATH = "/home/amitkumarx86/Downloads/";
	public CallWSD(){
		ws = new JWS(WORDNET_PATH,"3.0");
		setWs(ws);
	}
	
	public JWS getWs() {
		return ws;
	}

	public void setWs(JWS ws) {
		this.ws = ws;
	}
	
}
