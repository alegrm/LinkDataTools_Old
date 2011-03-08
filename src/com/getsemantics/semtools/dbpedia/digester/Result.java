package com.getsemantics.semtools.dbpedia.digester;

import java.util.*;

public class Result {
	
	private String mlabel, description, imageUrl, uri ;
	private List<SemanticClass> semanticClasses = new ArrayList<SemanticClass>();
	private List<Categories> categories = new ArrayList<Categories>();
   
   public void setMlabel(String mlabel){
   	this.mlabel = mlabel;
   }
   
	public String getMlabel(){
   	return this.mlabel;
   }

   public void setDescription(String description){
   	this.description = description;
   }
   
	public String getDescription(){
   	return this.description;
   }


   public void setImageUrl(String imageUrl){
   	this.imageUrl = imageUrl;
   }
   
	public String getImageUrl(){
   	return this.imageUrl;
   }

   public void setUri(String uri){
   	this.uri = uri;
   }
   
	public String getUri(){
   	return this.uri;
   }
    
   
  public void addSemanticClasses(SemanticClass sclass){ 		
		this.semanticClasses.add(sclass); 
	} 

	public List<SemanticClass> getSemanticClasses() {
		return this.semanticClasses; 
	}       

  public void addCategories(Categories category){ 		
		this.categories.add(category); 
	} 

	public List<Categories> getCategories() {
		return this.categories; 
	}       
      
}