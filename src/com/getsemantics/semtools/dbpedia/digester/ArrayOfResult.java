package com.getsemantics.semtools.dbpedia.digester;

import java.util.*;

public class ArrayOfResult{

	private List<Result> results = new ArrayList <Result> ();

	public ArrayOfResult(){}

	public void addResult(Result result){ 		
		this.results.add(result); 
	} 

	public List<Result> getResults() {
		return this.results; 
	}

}