package com.getsemantics.semtools.dbpedia.digester;

import java.util.*;

public class KeywordSearchResult{

	private List<Result> results = new ArrayList<Result>();

	public KeywordSearchResult(){}

	public void addResult(Result result){ 		
		this.results.add(result); 
	} 

	public List<Result> getResults() {
		return this.results; 
	}

}