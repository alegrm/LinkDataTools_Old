package com.getsemantics.semtools.dbpedia;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class DBpediaSearch {
	private static Logger logger = Logger.getLogger(DBPediaQuery.class.getPackage().getName());
	
	private final String prefixes = 
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " + 
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +  
		"PREFIX dbp: <http://dbpedia.org/ontology/> " +
		"PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
		"PREFIX dbpedia2: <http://dbpedia.org/property/> ";
	
	
	// get the DBpedia Res
	public DBpediaSearch(){
		DOMConfigurator.configure("logger.xml");
		
	}
	
	public Resource search(String pQuery){
		
		Resource resource = null;
		Model m = query(prefixes+pQuery);
		
		if(m!=null){
			/*
			Model model = resource.getModel();
			Property redirect =  model.getProperty("http://dbpedia.org/property/", "redirect");
			
			if(resource.hasProperty(redirect)){
				String newUri= resource.getProperty(redirect).getResource().getURI();
				q = resourceQuery.replaceAll("DBPEDIA_URI", newUri);
		    	logger.debug("			URIExpand is redirected to " + newUri);
		    	resource = query(q, newUri);
			}
			
			StmtIterator iter = resource.listStatements(new SimpleSelector(resModel.getResource(dbpediaUri), redirect, (RDFNode) null));
			if (iter.hasNext()) {
			    while (iter.hasNext()) {
			    	//UPDATES A NEW URI FROM REDIRECT
			    	dbpediaUri = iter.nextStatement().getObject().toString();
			    	q = resourceQuery.replaceAll("DBPEDIA_URI", dbpediaUri);
			    	logger.debug("	is redirected to " + dbpediaUri);
			    	resModel = query(q, dbpediaUri);
			        
			    }	
			} */
		}
		
		return resource;
	}
	
	public static Model query(String query){

		String 	endpoint 					= "http://dbpedia.org/sparql/?format=application%2Frdf%2Bxml&query=";
		boolean blnSocketTimeoutException	= false;
		int		countSocketTimeoutException	= 0;
		
		Model model = null;
		try {
			// Set up the initial connection
			logger.debug(endpoint + URLEncoder.encode(query, "UTF-8"));
			URL url = new URL(endpoint+URLEncoder.encode(query, "UTF-8"));
			
			do{
				try{
					URLConnection connection = url.openConnection();
					connection.setReadTimeout(30000);
					blnSocketTimeoutException=false;
					connection.connect();
					model = ModelFactory.createDefaultModel();
					model.read(connection.getInputStream(), "");
					// add results to the model
					
				}catch(SocketTimeoutException e){
					blnSocketTimeoutException =true;
					countSocketTimeoutException++;
					logger.info("		RETRY connection " + countSocketTimeoutException);
				}
			}while(blnSocketTimeoutException==true && countSocketTimeoutException<3);
			
			if(blnSocketTimeoutException==true && countSocketTimeoutException==3){
				logger.error("	QUERY FIELD: " + query);
			}
			
		
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return model;
	}
}
