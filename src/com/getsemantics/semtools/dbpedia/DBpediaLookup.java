package com.getsemantics.semtools.dbpedia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import com.getsemantics.semtools.dbpedia.digester.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/***
 * Makes a look up of a concept to dbpedia look up service using HTTP GET request and response.
 * For more information about this service see <a href="http://lookup.dbpedia.org/api/search.asmx?op=KeywordSearch">http://lookup.dbpedia.org/api/search.asmx?op=KeywordSearch</a>
 * <p>
 * Example of HTTP/1.1 DBpedlia lookup request:
 * GET /api/search.asmx/KeywordSearch?QueryString=string&QueryClass=string&MaxHits=string  
 * Host: lookup.dbpedia.org
 * <p>
 * The Result of this request is the following is s Jena Modle object containing RDF describing the found concept ant a OWL.sameAs that
 * relates the looked up concept with the found concept. 
 */

/*
 * 
 * Think about replacing this Look Up with the DbpediaQuery, because if I want to ensure disambiguation when asking for a person/production I can better filter there...	
 * 
 * */
public class DBpediaLookup {

	private final 	String 				urlService 		= "http://lookup.dbpedia.org/api/search.asmx/KeywordSearch";
	private 		int 				MaxHits;
	private			String				LinkUri;
	private			double				similarityMinScore;
	private 		Levenshtein 		similartityAlgorithm;
	private			boolean				doubleCheckSimilarity;
	private			boolean				obligatoryClass;
	
	private static Logger logger = Logger.getLogger(DBpediaLookup.class.getPackage().getName());
	
	/** 
	 * Class constructor. Initialise variables:
	 * MaxHits 				= 3;
	 * similarityMinScore 	= 1.0;
	 */
	public DBpediaLookup(){
		DOMConfigurator.configure("logger.xml");
		MaxHits 				= 3;
		similarityMinScore 		= 1.0;
		similartityAlgorithm 	= new Levenshtein();
		doubleCheckSimilarity	= false;
		obligatoryClass			= false;
	}
	
	public Resource lookUp(String pLookUpText, String[] pRangeTypes){
		
		Model				model			= null;
		Resource			resultResc		= null;
		String 				line 			= null;
		String 				result 			= "";
		BufferedReader 		reader			=null;
		HttpURLConnection 	lConnection 	= null;
		URL 				lUrl			= null;
		
		// KeywordSearch?QueryString=string&QueryClass=string&MaxHits=string";
		//logger.debug(this.urlService+"?QueryString=" + pLookUpText + "&MaxHits="+this.MaxHits+"&QueryClass=");
		try {
			lUrl = new URL(this.urlService+"?QueryString=" + URLEncoder.encode(pLookUpText, "UTF-8")+ "&MaxHits="+this.MaxHits+"&QueryClass=");
			// set up out communications stuff
			lConnection = null;
			// Set up the initial connection
			lConnection = (HttpURLConnection) lUrl.openConnection();
			lConnection.setRequestMethod("GET");
			lConnection.setDoOutput(true);
			lConnection.setReadTimeout(30000);
			lConnection.connect();
			
			//logger.info("Encoding:" + lConnection.getContentEncoding());
			
			reader = new BufferedReader(new InputStreamReader(lConnection.getInputStream()));
			while ((line = reader.readLine()) != null) {
				result += line;
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 
		if(result !=""){
			String patternStr = "<ArrayOfResult(.*?)</ArrayOfResult>";
			Pattern pattern = Pattern.compile(patternStr);   
			Matcher matcher = pattern.matcher(result);
		
			if (matcher.find()){
				
				String groupStr = matcher.group(0);
				//logger.debug("		 matcher.groupCount() "+ matcher.groupCount());
				
				StringReader xml = new StringReader(groupStr);
				DbpediaDigester digester = new DbpediaDigester();
				
				ArrayOfResult arrayRes = digester.digestArrayOfResult(xml);
				List<Result> results = arrayRes.getResults();
				//logger.debug("		results.size() "+ results.size());
				if(results.size() > 0 ){
					Iterator <Result> it;
					// compare results with types provided in the parameters
					
					if(pRangeTypes != null){
						it =  results.iterator();
						while (it.hasNext() && resultResc==null){
							Result r = it.next();
							//	compare if the classes are the same type as requested in the parameters
							Iterator <SemanticClass> itClass =  r.getSemanticClasses().iterator();
							boolean found = false;
							while (itClass.hasNext()){
								SemanticClass mclass = itClass.next();
								for(int i = 0; i<pRangeTypes.length; i++){
									// compare that the type set is contained in the type got (lower case comperison)
									//logger.debug("			Compare "+ mclass.getMlabel().toLowerCase() + " with " +pRangeTypes[i].toLowerCase());
									if(mclass.getMlabel().toLowerCase().contains(pRangeTypes[i].toLowerCase())){
										logger.debug("			LOOKUP found type "+ pLookUpText + "	"+ pRangeTypes[i] + " CLASS " + mclass.getMlabel());
										if(doubleCheckSimilarity){
											logger.debug("			DOUBLE CHECK ");
											if (similartityAlgorithm.getSimilarity(pLookUpText, r.getMlabel()) >= similarityMinScore){
												logger.debug("				OK");
												model = DBPediaResult.createModelFromLookUp(r);
												resultResc = model.getResource(r.getUri());
												found = true;
											}
										}
										else{
											
											model = DBPediaResult.createModelFromLookUp(r);
											resultResc = model.getResource(r.getUri());
											found= true;
										}
									}
								}
								if(found) break;
							}
						}
						//logger.debug("		result = "+ result);
					}
					//else{
					if(model==null && obligatoryClass==false){
						// compare similarity
						Result res = null;
						double maxScore=0.0;
						it =  results.iterator();
						// 	gets the result that is more similar
						while (it.hasNext()){
							Result r = it.next();
							double s = similartityAlgorithm.getSimilarity(pLookUpText.toLowerCase(), r.getMlabel().toLowerCase());
							if (s > maxScore){
								res = r;
								maxScore = s;
							}
						}
						if(maxScore >= similarityMinScore){
							logger.debug("		LOOKUP found similarity "+ pLookUpText + "	"+ res.getUri() + " SCORE " + maxScore);
							model = DBPediaResult.createModelFromLookUp(res);
							resultResc = model.getResource(res.getUri());
						}
						//
					}
						
				}else logger.debug("		LOOKUP result 0");
			}else logger.debug("		LOOKUP matcher empty");
		} else	logger.debug("		LOOKUP result empty");
		
		return resultResc;
	}
	
	/**
     * Lookup a concept of a given type. If found it creates a Model that contains:
     * - Found resource and provided descriptors (rdf:type, skos:subject, dc:description)
     * - Links the found resource with the resource provided in the parameter using the given Property
     * - Assigns a rdf:type to the found resource
     *
     * @param	pLookUpText - name of the concept to find
     * @param	pLinkSubjectUri	- the resource to be linked to the result
     * @param	pLinkPredicate - the property to link the found resource to the pLinkSubjectUri
     * @param	pRdfType - if not null, the this type is given to the found resource. If this parameter is not set, then a similarity comparison will take place to return the most similar which similarity is higher than the minimum score.
     * @param	pRangeTypes	- if not null it filters results to the given types of this property
     * 	
     * @retutn 	Model - If the concept was found, it returns a Jena Model object with the resuls and the created links. 
     */
	public Model lookUp(String pLookUpText, String pLinkSubjectUri, Property pLinkPredicate, Resource pRdfType, String[] pRangeTypes){
		
		this.LinkUri = pLinkSubjectUri;
		
		Model				model			= null;
		String 				line 			= null;
		String 				result 			= "";
		BufferedReader 		reader			=null;
		HttpURLConnection 	lConnection 	= null;
		URL 				lUrl			= null;
		
		// KeywordSearch?QueryString=string&QueryClass=string&MaxHits=string";
		
		try {
			//logger.debug(this.urlService+"?QueryString=" + URLEncoder.encode(pLookUpText, "UTF-8")+ "&MaxHits="+this.MaxHits+"&QueryClass=");
			lUrl = new URL(this.urlService+"?QueryString=" + URLEncoder.encode(pLookUpText, "UTF-8")+ "&MaxHits="+this.MaxHits+"&QueryClass=");
			// set up out communications stuff
			lConnection = null;
			// Set up the initial connection
			lConnection = (HttpURLConnection) lUrl.openConnection();
			lConnection.setRequestMethod("GET");
			lConnection.setDoOutput(true);
			lConnection.setReadTimeout(30000);
			lConnection.connect();
			
			//System.out.println("ENCODING:"  + lConnection.getContentEncoding());
			reader = new BufferedReader(new InputStreamReader(lConnection.getInputStream(), "UTF-8"));
			while ((line = reader.readLine()) != null) {
				result += line;
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(result !=""){
			String patternStr = "<ArrayOfResult(.*?)</ArrayOfResult>";
			Pattern pattern = Pattern.compile(patternStr);   
			Matcher matcher = pattern.matcher(result);
			
			if (matcher.find()){
				//logger.debug("	matcher.groupCount() " + matcher.groupCount());
				
				String groupStr = matcher.group(0);
				
				StringReader xml = new StringReader(groupStr);
				DbpediaDigester digester = new DbpediaDigester();
				
				ArrayOfResult arrayRes = digester.digestArrayOfResult(xml);
				List<Result> results = arrayRes.getResults();
				logger.debug("		results.size() " + results.size());
				double maxScore=0.0;
				Result resSimilaruty = null;
				if(results.size() > 0 ){
					Iterator <Result> it;
					// compare results with types provided in the parameters
					it =  results.iterator();
					
					while (it.hasNext()){
						Result r = it.next();
						
						Iterator <SemanticClass> itClass =  r.getSemanticClasses().iterator();
											
						if(pRangeTypes != null && itClass.hasNext()){
							logger.debug("		Comapre Types " + r.getUri() +  " test ranges " + Arrays.toString(pRangeTypes));
							//	compare if the classes are the same type as requested in the parameters 
							while (itClass.hasNext()){
								SemanticClass mclass = itClass.next();
								for(int i = 0; i<pRangeTypes.length; i++){
									// compare that the type set is contained in the type got (lower case comperison)
									logger.debug("		COMPARE " + mclass.getUri().toLowerCase() + " - "+ pRangeTypes[i].toLowerCase());
									if(mclass.getUri().toLowerCase().contains(pRangeTypes[i].toLowerCase())){
										logger.debug("		FOUND TYPE "+ pLookUpText + "	"+ r.getUri()+ " CLASS " + mclass.getMlabel());
										model = DBPediaResult.createModelFromLookUp(r, LinkUri, pLinkPredicate, pRdfType);
										resSimilaruty = null;
										break;
									}
								}
								if(model != null) break;
							}
							
							if(model != null) break;
						}
						else{
							logger.debug("		Comapre Similarity " + r.getMlabel() +  " with " + pLookUpText);
							// 	gets the result that is more similar
							double s = similartityAlgorithm.getSimilarity(pLookUpText, r.getMlabel());
							if (s > maxScore){
								resSimilaruty = r;
								maxScore = s;
							}
							
						}
						
					}
					
					if(resSimilaruty != null){
						// compare similarity
						
						if(maxScore >= similarityMinScore){
							logger.debug("		FOUND SIMILARITY "+ pLookUpText + "	"+ resSimilaruty.getUri() + " SCORE " + maxScore);
							model = DBPediaResult.createModelFromLookUp(resSimilaruty, LinkUri, pLinkPredicate, pRdfType);
						}
						else logger.debug("		REJECTED SIMILARITY " + maxScore);
					}
						
				}
				else logger.debug("		Result size 0 ");
			}
			else logger.debug("		ArrayOfResult empty ");
		} 
		else	logger.debug("		result empty ");
		
		return model;
	}
	
	
	/**
     * Sets the max number of results that the Lookup can bring. Default is 3. 
     *
     * @param maxHits new max hits value
     */
	public void setMaxHits(int maxHits) {
		MaxHits = maxHits;
	}
	/**
     * Gets the MaxHits current value.
     *
     * @return MaxHits value
     */
	public int getMaxHits() {
		return MaxHits;
	}
	/**
     * Sets the minimum similarity value to evaluate two strings. Maximum default is 1.
     *
     * @param similarityScore new similarity score value
     */
	public void setSimilarityMinScore(double similarityScore) {
		this.similarityMinScore = similarityScore;
	}
	/**
     * Gets the similarityMinScore current value.
     *
     * @return similarityMinScore value
     */
	public double getSimilarityMinScore() {
		return similarityMinScore;
	}

	public void setDoubleCheckSimilarity(boolean doubleCheckSimilarity) {
		this.doubleCheckSimilarity = doubleCheckSimilarity;
	}

	public boolean isDoubleCheckSimilarity() {
		return doubleCheckSimilarity;
	}

	public void setObligatoryClass(boolean obligatoryClass) {
		this.obligatoryClass = obligatoryClass;
	}

	public boolean isObligatoryClass() {
		return obligatoryClass;
	}

}
