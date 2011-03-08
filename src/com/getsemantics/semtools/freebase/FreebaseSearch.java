package com.getsemantics.semtools.freebase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/***
 * Makes a search request of a concept to the Search API of Freebase, and returns a RDF model with the SameAs information with respecting Freebase URI.  
 * For more information about this Freebase service see <a href="http://www.freebase.com/docs/web_services/search">http://www.freebase.com/docs/web_services/search</a>
 * <p>
 * It is recommended to use this class when the name of the concept could not be the exact name. For example the Basketball team of North Carolina, its known 
 * name is NC State Wolfpack basketball and not North Carolina Basketball Team. Thus to search for this concept in Freebase the search API is a good option.
 * An example HTTP request for the North Carolina Basketball team is:
 * <p>
 * http://api.freebase.com/api/service/search?query=North+Carolina+State+basketball&type=/sports/sports_team&indent=1&limit=1
 * <p>
 * The Result of this request is the following JSON array:  
 * 
 * 	{
 *	  "status": "200 OK",
 *	  "code": "/api/status/ok",
 *	  "result": [
 *	  {
 *	    "alias": [
 *	      "North Carolina State basketball"
 *	    ],
 *	    "article": {
 *	      "id": "/guid/9202a8c04000641f800000000461afb3"
 *	    },
 *	    "guid": "#9202a8c04000641f800000000461afb0",
 *	    "id": "/en/nc_state_wolfpack_basketball",
 *	    "image": {
 *	      "id": "/guid/9202a8c04000641f8000000004dec877"
 *	    },
 *	    "name": "NC State Wolfpack basketball",
 *	    "relevance:score": 50.861629486083984,
 *	    "type": [
 *	      {
 *	        "id": "/common/topic",
 *	        "name": "Topic"
 *	      },
 *	      {
 *	        "id": "/sports/sports_team",
 *	        "name": "Sports Team"
 *	      },
 *	      {
 *	        "id": "/basketball/basketball_team",
 *	        "name": "Basketball Team"
 *	      },
 *	      {
 *	        "id": "/sports/school_sports_team",
 *	        "name": "School sports team"
 *	      },
 *	      {
 *	        "id": "/base/marchmadness/topic",
 *	        "name": "Topic"
 *	      },
 *	      {
 *	        "id": "/base/marchmadness/ncaa_basketball_team",
 *	        "name": "NCAA Basketball Team"
 *	      }
 *	    ]
 *	  }
 *	],
 *	  "transaction_id": "cache;cache01.p01.sjc1:8101;2010-04-16T13:04:32Z;0025"
 *	}
 *
 * @author      Alejandra Garcia Rojas Martinez
 * @version     %I%, %G%
 * @since       1.0
 * 
 */

public class FreebaseSearch {

	private final 			String 				freebaseSearch	= "http://api.freebase.com/api/service/search";
	private 				int					limit;
	private					boolean				indent;
	private					double				minScore;
	private					double				score;
	private static final 	Logger 				logger =  Logger.getLogger(FreebaseSearch.class.getPackage().getName());
	/** 
    * Class constructor.
    */
	public FreebaseSearch(){
		DOMConfigurator.configure("logger.xml");
		this.limit	=	1;
		setMinScore(50);
	}
	/**
     * Make a request of a concept.
     *
     * @param concept  name of the concept.
     */
	public JSONObject request(String p_concept ){
		return (request(p_concept, ""));
	}
	
	
	/**
     * Make a request of a concept of a given type.
     *
     * @param	concept	name of the concept
     * @param	conceptType	type of concept according to Freebase (see <a href="http://schemas.freebaseapps.com/">types http://schemas.freebaseapps.com/</a>
     * @retutn 	A JSON object if the request was successful, has at least one result and the relevance score bigger than the minimum set. Returns null if the previous conditions were not accomplished. 
     */
	public JSONObject request(String concept, String conceptType){
		
		HttpURLConnection 	connection 		= null;
		URL 				serverAddress 	= null;
		String 				mql 			= "";
		JSONObject			result		= null;
		
		this.score= 0;
		
		// Create the HTTP request  e.g. http://api.freebase.com/api/service/search?query=North+Carolina+State+basketball&type=/sports/sports_team&indent=1&limit=1
		try {
			mql = "?query="+URLEncoder.encode(concept, "UTF-8") + "&limit=" + this.limit;
			if(!conceptType.equals(""))
				mql += "&type="+ URLEncoder.encode(conceptType, "UTF-8");
			if(this.indent)
				mql += "&indent=1";
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			serverAddress = new URL(this.freebaseSearch+mql);
			// set up out communications stuff
			connection = null;

			// Set up the initial connection
			connection = (HttpURLConnection) serverAddress.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.setReadTimeout(23000);
			connection.connect();

			String line = null;

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
			StringBuilder sb = new StringBuilder();

			while ((line = reader.readLine()) != null)
				sb.append(line + "\n");

			JSONObject response = new JSONObject(sb.toString());

			if( (response.get("status").equals("200 OK")))
			{
				JSONArray res = response.getJSONArray("result");
				if(res.length()>0){
					
					result = (JSONObject)res.get(0);
					this.score = result.getDouble("relevance:score");
					if(this.score < minScore){
						logger.debug(" 	" + concept + " : "+conceptType +"/"+ result.getString("name") + "	IGNORED:" + result.getString("relevance:score"));
						result = null;
						this.score = 0;
					}
					else
						logger.debug(" 	" + concept + ":"+conceptType+"	"+ result.getString("name") + "	OK:" + result.getString("relevance:score"));
				}
				//else logger.debug("	NOT FOUND");
			}
			
		} catch (MalformedURLException e) {
			logger.error("MalformedURLException in request "+ e.getMessage());
			e.printStackTrace();
		} catch (ProtocolException e) {
			logger.error("ProtocolException in request "+ e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("IOException in request "+ e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exception in request "+ e.getMessage());
			e.printStackTrace();
		}
		finally {
			// close the connection, set all objects to null
			connection.disconnect();
			connection = null;
		}
		return result;
	}

	/**
     * Sets the limit number of results in the request. The default limit number is 1.
     *
     * @param limit	new limit value
     */
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	/**
     * Gets the limit number of results in the request.
     *
     * @return limit	the current limit value
     */
	public int getLimit() {
		return limit;
	}
	/**
     * Sets the indent option for display purposes.
     *
     * @param indent the indent limit value
     */
	public void setIndent(boolean indent) {
		this.indent = indent;
	}
	/**
     * Gets the indent value.
     *
     * @return  indent Indent option value. 
     */
	public boolean isIndent() {
		return indent;
	}
	/**
     * Set the minimum relevant score to consider a result
     *
     * @param minScore  minimum score 
     */
	public void setMinScore(double minScore) {
		this.minScore = minScore;
	}
	/**
     * Gets the minimum relevant score to consider a result
     *
     * @return minScore  minimum score 
     */
	public double getMinScore() {
		return minScore;
	}
	/**
     * Gets the relevant score obtained from the search
     *
     * @return minScore  minimum score 
     */

	public double getScore() {
		return score;
	}
}
