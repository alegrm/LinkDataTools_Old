package com.getsemantics.semtools.freebase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;

public class FreebaseQuery {
	
	private final 	String 				mqlread = "http://api.freebase.com/api/service/mqlread";
	private static 	Logger 				logger = Logger.getLogger(FreebaseQuery.class.getPackage().getName());
	
	public FreebaseQuery(){
		DOMConfigurator.configure("logger.xml");
	}
	
	public JSONObject request(String query){

		HttpURLConnection connection = null;
		URL serverAddress = null;
		JSONObject result = null;
		
		try {
			//logger.debug(query);
			serverAddress = new URL(mqlread +"?query="+URLEncoder.encode(query,"UTF-8"));
			// set up out communications stuff
			connection = null;

			// Set up the initial connection
			connection = (HttpURLConnection) serverAddress.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.setReadTimeout(50000);
			connection.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null)
				sb.append(line + "\n");

			JSONObject response = new JSONObject(sb.toString());

			if( (response.get("status").equals("200 OK")))
			{
				JSONArray res = response.getJSONArray("result");
				if(res.length()>0){
					result = (JSONObject)res.get(0);
				}
			}
			
		} catch (MalformedURLException e) {
			logger.error("MalformedURLException: " + e.getMessage());
			e.printStackTrace();
		} catch (ProtocolException e) {
			logger.error("ProtocolException: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("IOException: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage());
			e.printStackTrace();
		}
		finally {
			// close the connection, set all objects to null
			connection.disconnect();
			connection = null;
		}

		return result;
	}

}
