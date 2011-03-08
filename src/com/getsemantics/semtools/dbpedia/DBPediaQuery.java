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
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.JenaException;

public class DBPediaQuery {

	private static Logger logger = Logger.getLogger(DBPediaQuery.class.getPackage().getName());
	
	
	private final String resourceQuery = 
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " + 
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +  
		"PREFIX dbp: <http://dbpedia.org/ontology/> " +
		"PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
		"PREFIX dbpedia2: <http://dbpedia.org/property/> "+
		"CONSTRUCT {  " +
		"<DBPEDIA_URI> rdfs:label ?label . " + 
		"<DBPEDIA_URI> rdf:type ?type .  " +
		"<DBPEDIA_URI> dbp:abstract ?abstract. " + 
		"<DBPEDIA_URI> dc:description ?description. " + 
		"<DBPEDIA_URI> owl:sameAs ?same . " +
		"<DBPEDIA_URI> dbpedia2:redirect ?redirect . " +
		"?type rdfs:label ?tlabel .  }  " +
		"WHERE { <DBPEDIA_URI> rdfs:label ?label . FILTER langMatches( lang(?label), \"en\" ) . " +  
		"OPTIONAL { <DBPEDIA_URI> dbpedia2:redirect ?redirect } . "+ 
		"OPTIONAL {<DBPEDIA_URI> rdf:type ?type} .  " +
		"OPTIONAL {<DBPEDIA_URI> owl:sameAs ?same} . " +
		"OPTIONAL {?type rdfs:label ?tlabel} .  " +
		"OPTIONAL{<DBPEDIA_URI> dbp:abstract ?abstract.  FILTER langMatches( lang(?abstract), \"en\") } . " +
		"OPTIONAL{<DBPEDIA_URI> dc:description ?description.  FILTER langMatches( lang(?description), \"en\") } }" ;
	
	/**
	 * 
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  
PREFIX dbp: <http://dbpedia.org/ontology/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX dbpedia2: <http://dbpedia.org/property/>
CONSTRUCT { 
<http://dbpedia.org/resource/London> rdfs:label ?label . 
<http://dbpedia.org/resource/London> rdf:type ?type . 
<http://dbpedia.org/resource/London> dbp:abstract ?abstract. 
<http://dbpedia.org/resource/London> dc:description ?description. 
<http://dbpedia.org/resource/London> owl:sameAs ?same . 
?type rdfs:label ?tlabel .  } 
WHERE { <http://dbpedia.org/resource/London> rdfs:label ?label . FILTER langMatches( lang(?label), "en" ) .  
OPTIONAL {<http://dbpedia.org/resource/Surfer> dbpedia2:redirect ?redirect } . 
OPTIONAL {<http://dbpedia.org/resource/London> rdf:type ?type} . 
OPTIONAL {<http://dbpedia.org/resource/London> owl:sameAs ?same} .
OPTIONAL {?type rdfs:label ?tlabel} . 
OPTIONAL{<http://dbpedia.org/resource/London> dbp:abstract ?abstract.  FILTER langMatches( lang(?abstract), "en") } .
OPTIONAL{<http://dbpedia.org/resource/London> dc:description ?description.  FILTER langMatches( lang(?description), "en") }
}
	 * 
	 * */
	
	// get the DBpedia Res
	public DBPediaQuery(){
		DOMConfigurator.configure("logger.xml");
		
	}
	
	public Resource expandURI(String dbpediaUri){
		

		String q = resourceQuery.replaceAll("DBPEDIA_URI", dbpediaUri);
		
		Resource resource = query(q, dbpediaUri);
		
		if(resource!=null){
		
			Model model = resource.getModel();
			Property redirect =  model.getProperty("http://dbpedia.org/property/", "redirect");
			
			if(resource.hasProperty(redirect)){
				String newUri= resource.getProperty(redirect).getResource().getURI();
				q = resourceQuery.replaceAll("DBPEDIA_URI", newUri);
		    	logger.debug("			URIExpand is redirected to " + newUri);
		    	resource = query(q, newUri);
			}
			/*
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
	
	public static Resource query(String query, String uri){

		String 	endpoint 					= "http://dbpedia.org/sparql/?format=application%2Frdf%2Bxml&query=";
		boolean blnSocketTimeoutException	= false;
		int		countSocketTimeoutException	= 0;
		
		Resource resource = null;
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
			
			if(!model.contains(model.getResource(uri), null, (RDFNode)null )){
				resource = null;
			}
			else 
				resource = model.getResource(uri);
		
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return resource;
	}
}
