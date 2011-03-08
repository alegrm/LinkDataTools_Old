package com.getsemantics.semtools.dbpedia.digester;

import java.util.*;

public class PrefixSearchResult{

	private List<Result> results = new ArrayList<Result>();

	public PrefixSearchResult(){}

	public void addResult(Result result){ 		
		this.results.add(result); 
	} 

	public List<Result> getResults() {
		return this.results; 
	}

}