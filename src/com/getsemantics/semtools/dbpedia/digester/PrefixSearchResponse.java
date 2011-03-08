package com.getsemantics.semtools.dbpedia.digester;

import java.util.*;

public class PrefixSearchResponse {

	private String xmlns;
	private List<PrefixSearchResult> prefixSearchResult = new ArrayList<PrefixSearchResult>();
	
	public void setXmlns (String r){
		this.xmlns = r;
	}
	
	public String getXmlns (){
		return(this.xmlns);
	}
	
	public void addPrefixSearchResult (PrefixSearchResult res){
		this.prefixSearchResult.add(res);
	}
	
	public List<PrefixSearchResult> getPrefixSearchResult (){
		return this.prefixSearchResult;
	}
		
		
	public void print()
	{
		System.out.println(" [print PrefixSearchResult]");
		System.out.println("		xmnls:" + this.xmlns);
		System.out.println("		#keywordSearchResults:" + this.prefixSearchResult.size());
	}
}
