package com.ashrafishak.compnet.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ashrafishak.compnet.json.JSONArray;
import com.ashrafishak.compnet.json.JSONException;
import com.ashrafishak.compnet.json.JSONObject;

public class CompanyQueue {

	private String baseURL;
	public static HashMap<String, String> compMap = new HashMap<String, String>();
	private CompNetFetcher fetcher;
	private static final Logger moduleLogger = Logger.getLogger("com.ashrafishak.moduleLogger");

	
	public CompanyQueue () {
	}
	/*
	public void processCompanies(){
		try {
			StringBuffer sb = new StringBuffer();
			BufferedReader bf = new BufferedReader(new FileReader(new File("companies.js")));
			while (bf.ready()){
				sb.append(bf.readLine());
			}	
			bf.close();
			JSONObject compJson = new JSONObject("{'companies': "+sb.toString()+"}");
			JSONArray compArray = compJson.getJSONArray("companies");
			for (int i = 0; i < compArray.length(); i++){
				JSONObject comp = compArray.getJSONObject(i);
				compMap.put(comp.getString("name"), comp.getString("permalink"));
			}
			for (String name: compMap.keySet()){
				//this.fetcher.parseJson(this.baseURL, compMap.get(name));
				Thread.sleep(1000);
			}
		} catch (MalformedURLException e) {
			moduleLogger.error("MalformedURLException", e);
		} catch (IOException e) {
			moduleLogger.error("IOException",e);
		} catch (JSONException e) {
			moduleLogger.error("JSONException",e);
		} catch (InterruptedException e) {
			moduleLogger.error("InterruptedException", e);
		}
		
	}
	*/
	
	public static void main (String[] args){
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		CompanyQueue q = (CompanyQueue) context.getBean("companyQueue");
		//q.processCompanies();
	}

	public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}


	public void setFetcher(CompNetFetcher fetcher) {
		this.fetcher = fetcher;
	}


	public CompNetFetcher getFetcher() {
		return fetcher;
	}
}
