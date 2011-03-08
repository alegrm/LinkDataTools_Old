package com.getsemantics.semtools.freebase;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class FreebaseResult {
	
	public static final		String		FREEBASE_NS	= "http://rdf.freebase.com/ns/";
	public static final		String 		FREEBASE_IMG_URL = "http://www.freebase.com/api/trans/raw";
	private static 			Logger 		logger 		= Logger.getLogger(FreebaseResult.class.getPackage().getName());
	
	public FreebaseResult(){
		DOMConfigurator.configure("logger.xml");
	}
	
	public static String createUriFromID(String id){
		String uri ="";
		uri = FREEBASE_NS + (id.substring(1, id.length())).replace("/",".");
		return uri;
	}
	
	public static List<String> getTypes(JSONObject p_result) {
		List<String> types = new ArrayList<String>();
		try {
			JSONArray type = p_result.getJSONArray("type");
			for(int i =0; i<type.length() ; i++){
				JSONObject t = type.getJSONObject(i);
				types.add(t.getString("id"));
			}
		} catch (JSONException e) {
			logger.error(e);
		}
		return types;
	}
	

	
	public static Model linkResult(String pSubjectUri, Property pPredicate, JSONObject pResultJSON) {
		
		Model resultModel = ModelFactory.createDefaultModel();
		Resource subject = resultModel.createResource(pSubjectUri);
		
		Resource object = createJSONPropertiesToModel(resultModel, pResultJSON);
		if(object != null){
		//rdfs:isDefinedBy freebase!
			resultModel.add(resultModel.createStatement(subject, pPredicate, object.as(RDFNode.class)));
		}
		return resultModel;
	}
	
	public static Resource createJenaModel(JSONObject pResultJSON) {
		
		if(pResultJSON==null) return null;
		
		Model lmodel = ModelFactory.createDefaultModel();
		Resource object = createJSONPropertiesToModel(lmodel, pResultJSON);
		return object;
	}
	
	/*
	 * Recursive function to populate a model with a Freebase results
	 * */
	private static Resource createJSONPropertiesToModel(Model model, JSONObject json){
		Resource 	subject = null;
		try{
			String 		id = json.getString("id");
			subject = model.createResource(FREEBASE_NS+ (id.substring(1, id.length())).replace("/","."));
			
			Iterator<String> keys = json.keys();		
			while(keys.hasNext()){
				String key = (String) keys.next();
				if(key.equals("id")){}
				else if(key.equals("type")){
					JSONArray type = json.getJSONArray("type");
					for(int i =0; i<type.length() ; i++){
						JSONObject t = type.getJSONObject(i);
						String tUri = t.getString("id");
						Resource rt =  model.getResource(FREEBASE_NS+(tUri.substring(1, tUri.length()).replace("/",".")));
						if(!rt.hasProperty(RDFS.label))
							model.add(model.createStatement(rt, RDFS.label, model.createLiteral(t.getString("name"))));
						model.add(model.createStatement(subject, RDF.type, rt.as(RDFNode.class)));
					}
				}
				else if(key.equals("name"))
					model.add(model.createStatement(subject, RDFS.label, model.createLiteral(json.getString("name"))));
				else if(key.equals("/common/topic/image"))
					model.add(model.createStatement(subject, FOAF.depiction, model.createLiteral(json.getString("/common/topic/image"))));
				else{
					logger.debug("key:" + key);
					String rdfkey="";
					if(key.startsWith("/")) rdfkey = (key.substring(1, key.length()).replace("/","."));
					else rdfkey = key.replace("/",".");
					Property predicate = model.getProperty(FREEBASE_NS+rdfkey);
					//System.out.println(predicate.getURI() + " " + json.get(key).getClass().getName());
					if(json.get(key).getClass()==JSONObject.class){
						Resource object= createJSONPropertiesToModel(model, json.getJSONObject(key));
						model.add(model.createStatement(subject, predicate, object.as(RDFNode.class)));
					}else if(json.get(key).getClass()==JSONArray.class){
						JSONArray array = json.getJSONArray(key);
						for(int i =0; i<array.length() ; i++){
							if(array.get(i).getClass()== JSONObject.class){
								Resource object= createJSONPropertiesToModel(model,array.getJSONObject(i));
								model.add(model.createStatement(subject, predicate, object.as(RDFNode.class)));
							}
							else{
								model.add(model.createStatement(subject, predicate, model.createLiteral(json.getString(key))));
							}
						}
					}
					else{
						//if(json.get(key).getClass() == String.class ) or any thing else...
						model.add(model.createStatement(subject, predicate, model.createLiteral(json.getString(key))));
					}
				}
			}
		} catch (JSONException e) {
			logger.error(e);
			e.printStackTrace();
		}
		return subject;
		
	}
}
