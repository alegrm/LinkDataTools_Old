package com.getsemantics.semtools.dbpedia.digester;

import java.util.*;

public class KeywordSearchResponse{

	private String xmlns;
	private List<KeywordSearchResult> keywordSearchResults = new ArrayList<KeywordSearchResult>();
	
	public void setXmlns (String r){
		this.xmlns = r;
	}
	
	public String getXmlns (){
		return(this.xmlns);
	}
	
	public void addKeywordSearchResult (KeywordSearchResult res){
		this.keywordSearchResults.add(res);
	}
	
	public List<KeywordSearchResult> getKeywordSearchResult (){
		return this.keywordSearchResults;
	}
		
		
	public void print()
	{
		System.out.println(" [print KeywordSearchResult]");
		System.out.println("		xmnls:" + this.xmlns);
		System.out.println("		#keywordSearchResults:" + this.keywordSearchResults.size());
	}
}
