package com.getsemantics.semtools.dbpedia;

import java.util.Iterator;

import com.getsemantics.semtools.dbpedia.digester.Result;
import com.getsemantics.semtools.dbpedia.digester.SemanticClass;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class DBPediaResult {
	
	public static final String DBPEDIA_RESOURCE_BASE = "http://dbpedia.org/resource/";

	public static Model createModelFromLookUp(Result p_result, String pLinkUri, Property pRelation, Resource giveType) {
		
		Model lmodel = ModelFactory.createDefaultModel();
		Resource resSame = lmodel.createResource(pLinkUri);
		Resource resResult = lmodel.createResource(p_result.getUri());
		
		lmodel.add(lmodel.createStatement(resSame, pRelation, resResult.as(RDFNode.class)));
		lmodel.add(lmodel.createLiteralStatement(resResult, DC.description, lmodel.createLiteral(p_result.getDescription()))) ;
		lmodel.add(lmodel.createLiteralStatement(resResult, RDFS.label, lmodel.createLiteral(p_result.getMlabel()))) ;
		//lmodel.add(lmodel.createLiteralStatement(resResult, RDFS.isDefinedBy, lmodel.createLiteral(p_result.getMlabel()))) ;
		
		if(giveType!=null)
			lmodel.add(lmodel.createLiteralStatement(resResult, RDF.type, giveType.as(RDFNode.class)));
		
		Iterator <SemanticClass> itClass =  p_result.getSemanticClasses().iterator();
		while (itClass.hasNext()){
			SemanticClass mclass = itClass.next();
			Resource c = lmodel.createResource(mclass.getUri());
			resResult.addProperty(RDF.type, c.as(RDFNode.class));
		}
		return lmodel;
	}
	
	public static Model createModelFromLookUp(Result p_result) {
		
		Model lmodel = ModelFactory.createDefaultModel();
		
		Resource resResult = lmodel.createResource(p_result.getUri());
		
		lmodel.add(lmodel.createLiteralStatement(resResult, DC.description, lmodel.createLiteral(p_result.getDescription()))) ;
		lmodel.add(lmodel.createLiteralStatement(resResult, RDFS.label, lmodel.createLiteral(p_result.getMlabel()))) ;
		//lmodel.add(lmodel.createLiteralStatement(resResult, RDFS.isDefinedBy, lmodel.createLiteral(p_result.getMlabel()))) ;
		
		Iterator <SemanticClass> itClass =  p_result.getSemanticClasses().iterator();
		while (itClass.hasNext()){
			SemanticClass mclass = itClass.next();
			Resource c = lmodel.createResource(mclass.getUri());
			resResult.addProperty(RDF.type, c.as(RDFNode.class));
		}
		return lmodel;
	}

}
