package org.wsl.narratives.wsd.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/lin")
public class LINResource {
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	public String getWSD(@FormParam("word1") String word1,
			@FormParam("word2") String word2){
		
		LINPerlScriptModule wsd = new LINPerlScriptModule();
		
		return wsd.callLINScript(word1, word2);
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String getHelloWorld(){
		return "You get it!";
	}
}
