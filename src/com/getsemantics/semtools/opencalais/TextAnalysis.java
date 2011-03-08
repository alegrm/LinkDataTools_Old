package com.getsemantics.semtools.opencalais;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TextAnalysis {
	
	private final String urlService = "http://api.opencalais.com/enlighten/rest/";
	private static Logger logger = Logger.getLogger(TextAnalysis.class.getPackage().getName());
	
	public Model request(String pText){

		Model				result = null;
		HttpURLConnection 	lConnection = null;
		OutputStreamWriter 	lWriter = null;
		URL 				lUrl= null;
		String 				lParams ="<c:params xmlns:c=\"http://s.opencalais.com/1/pred/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" +
								"<c:processingDirectives c:contentType=\"text/html\" " + 
											"c:outputFormat=\"XML/RDF\" >" +
								"</c:processingDirectives>"+
								"</c:params>";
		
		try {
			lParams = URLEncoder.encode(lParams, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		logger.debug(urlService+"licenseID=jkew4zd4bwabq2qakcwkkek3&content=" + pText + "&paramsXML=" + lParams);
		try {
			lUrl = new URL( urlService);
			// set up out communications stuff
			lConnection = null;

			// Set up the initial connection
			lConnection = (HttpURLConnection) lUrl.openConnection();
			lConnection.setRequestMethod("GET");
			lConnection.setDoOutput(true);
			lConnection.setReadTimeout(30000);

			lConnection.connect();

			//licenseID=url-encoded-string&content=url-encoded-string&paramsXML=url-encoded-string
			lWriter = new OutputStreamWriter(lConnection.getOutputStream());
			lWriter.write("licenseID=jkew4zd4bwabq2qakcwkkek3&content=" + pText + "&paramsXML=" + lParams);
			lWriter.flush();
			result = ModelFactory.createDefaultModel();
			result.read(new InputStreamReader(lConnection.getInputStream(), "UTF-8"), "");
			//result.write(System.out);
			
		} catch (MalformedURLException e) {
			logger.error("MalformedURLException : ", e);
		} catch (ProtocolException e) {
			logger.error("ProtocolException : ", e);
		} catch (IOException e) {
			logger.error("IOException : ", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Exception : ", e);
		}
		finally {
			// close the connection, set all objects to null
			lConnection.disconnect();
			lWriter = null;
			lConnection = null;
		}

		return result;
	}
}
