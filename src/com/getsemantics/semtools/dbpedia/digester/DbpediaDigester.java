package com.getsemantics.semtools.dbpedia.digester;

import org.apache.commons.digester.Digester;
import java.io.StringReader;
/**
 * The classes in this packages aim to process the XML results of the DBPedia Lookup service transforming them in 
 * java objects to their easier manipulation. 
 * */
public class DbpediaDigester {

	public ArrayOfResult digestArrayOfResult(StringReader  input) {
		try {
			Digester dig = new Digester();
			dig.setValidating(false);

			dig.addObjectCreate("ArrayOfResult", ArrayOfResult.class);
			dig.addSetProperties("ArrayOfResult");

			dig.addObjectCreate("ArrayOfResult/Result", Result.class);

			dig.addBeanPropertySetter("ArrayOfResult/Result/Label", "mlabel");
			dig.addBeanPropertySetter("ArrayOfResult/Result/Description", "description");
			dig.addBeanPropertySetter("ArrayOfResult/Result/ImageUrl", "imageUri");
			dig.addBeanPropertySetter("ArrayOfResult/Result/URI", "uri");

			dig.addObjectCreate("ArrayOfResult/Result/Classes/Class", SemanticClass.class);
			dig.addBeanPropertySetter("ArrayOfResult/Result/Classes/Class/Label", "mlabel");
			dig.addBeanPropertySetter("ArrayOfResult/Result/Classes/Class/URI", "uri");
			dig.addSetNext("ArrayOfResult/Result/Classes/Class", "addSemanticClasses");

			dig.addObjectCreate("ArrayOfResult/Result/Categories/Category", Categories.class);
			dig.addBeanPropertySetter("ArrayOfResult/Result/Categories/Category/Label", "mlabel");
			dig.addBeanPropertySetter("ArrayOfResult/Result/Categories/Category/URI", "uri");		
			dig.addSetNext("ArrayOfResult/Result/Categories/Category", "addCategories");

			dig.addSetNext("ArrayOfResult/Result", "addResult");

			return ((ArrayOfResult) dig.parse(input));

		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return null;
	}

	public KeywordSearchResponse digestKeywordSearchResponse(StringReader  input) {
		try {
			Digester dig = new Digester();
			dig.setValidating(false);

			dig.addObjectCreate("KeywordSearchResponse", KeywordSearchResponse.class);
			dig.addSetProperties("KeywordSearchResponse");

			dig.addObjectCreate("KeywordSearchResponse/KeywordSearchResult", KeywordSearchResult.class);
			dig.addSetProperties("KeywordSearchResponse/KeywordSearchResult");

			dig.addObjectCreate("KeywordSearchResponse/KeywordSearchResult/Result", Result.class);

			dig.addBeanPropertySetter("KeywordSearchResponse/KeywordSearchResult/Result/Label", "mlabel");
			dig.addBeanPropertySetter("KeywordSearchResponse/KeywordSearchResult/Result/Description", "description");
			dig.addBeanPropertySetter("KeywordSearchResponse/KeywordSearchResult/Result/ImageUrl", "imageUri");
			dig.addBeanPropertySetter("KeywordSearchResponse/KeywordSearchResult/Result/URI", "uri");

			dig.addObjectCreate("KeywordSearchResponse/KeywordSearchResult/Result/Classes/Class", SemanticClass.class);
			dig.addBeanPropertySetter("KeywordSearchResponse/KeywordSearchResult/Result/Classes/Class/Label", "mlabel");
			dig.addBeanPropertySetter("KeywordSearchResponse/KeywordSearchResult/Result/Classes/Class/URI", "uri");
			dig.addSetNext("KeywordSearchResponse/KeywordSearchResult/Result/Classes/Class", "addSemanticClasses");

			dig.addObjectCreate("KeywordSearchResponse/KeywordSearchResult/Result/Categories/Category", Categories.class);
			dig.addBeanPropertySetter("KeywordSearchResponse/KeywordSearchResult/Result/Categories/Category/Label", "mlabel");
			dig.addBeanPropertySetter("KeywordSearchResponse/KeywordSearchResult/Result/Categories/Category/URI", "uri");		
			dig.addSetNext("KeywordSearchResponse/KeywordSearchResult/Result/Categories/Category", "addCategories");

			dig.addSetNext("KeywordSearchResponse/KeywordSearchResult/Result", "addResult");

			dig.addSetNext("KeywordSearchResponse/KeywordSearchResult", "addKeywordSearchResult");

			return ((KeywordSearchResponse) dig.parse(input));

		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return null;
	}

	public PrefixSearchResponse digestPrefixSearchResponse(StringReader  input) {
		try {
			Digester dig = new Digester();
			dig.setValidating(false);

			dig.addObjectCreate("PrefixSearchResponse", PrefixSearchResponse.class);
			dig.addSetProperties("PrefixSearchResponse");

			dig.addObjectCreate("PrefixSearchResponse/PrefixSearchResult", PrefixSearchResult.class);
			dig.addSetProperties("PrefixSearchResponse/PrefixSearchResult");

			dig.addObjectCreate("PrefixSearchResponse/PrefixSearchResult/Result", Result.class);

			dig.addBeanPropertySetter("PrefixSearchResponse/PrefixSearchResult/Result/Label", "mlabel");
			dig.addBeanPropertySetter("PrefixSearchResponse/PrefixSearchResult/Result/Description", "description");
			dig.addBeanPropertySetter("PrefixSearchResponse/PrefixSearchResult/Result/ImageUrl", "imageUri");
			dig.addBeanPropertySetter("PrefixSearchResponse/PrefixSearchResult/Result/URI", "uri");

			dig.addObjectCreate("PrefixSearchResponse/PrefixSearchResult/Result/Classes/Class", SemanticClass.class);
			dig.addBeanPropertySetter("PrefixSearchResponse/PrefixSearchResult/Result/Classes/Class/Label", "mlabel");
			dig.addBeanPropertySetter("PrefixSearchResponse/PrefixSearchResult/Result/Classes/Class/URI", "uri");
			dig.addSetNext("PrefixSearchResponse/PrefixSearchResult/Result/Classes/Class", "addSemanticClasses");

			dig.addObjectCreate("PrefixSearchResponse/PrefixSearchResult/Result/Categories/Category", Categories.class);
			dig.addBeanPropertySetter("PrefixSearchResponse/PrefixSearchResult/Result/Categories/Category/Label", "mlabel");
			dig.addBeanPropertySetter("PrefixSearchResponse/PrefixSearchResult/Result/Categories/Category/URI", "uri");		
			dig.addSetNext("PrefixSearchResponse/PrefixSearchResult/Result/Categories/Category", "addCategories");

			dig.addSetNext("PrefixSearchResponse/PrefixSearchResult/Result", "addResult");

			dig.addSetNext("PrefixSearchResponse/PrefixSearchResult", "addPrefixSearchResult");


			return ((PrefixSearchResponse) dig.parse(input));

		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return null;
	}

}
