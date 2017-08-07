package org.narratives.CoRefAnnotator;

import java.io.FileReader;
import java.io.InputStream;

import edu.sussex.nlp.jws.JWS;

public class Testing {
	
	public static void main(String[] args) throws Exception {
		DriverModule d = new DriverModule();
		
		String paths[] = {"/home/amitkumarx86/Documents/projects/narratives/code/Narratives/files/sets/Set12/"
						  };
		
		long start1 = System.nanoTime();
		for(String path : paths){
			long start = System.nanoTime();
			System.out.println("Processing started for "+path);
			d.setPath(path);
			d.processing("", "", "");
			long end = System.nanoTime();
			System.out.println("Finished process in :"+((end - start)/1000000000));
		}
		long end1 = System.nanoTime();
		System.out.println("Finished process in :"+((end1 - start1 )/1000000000));

		
	}
}
