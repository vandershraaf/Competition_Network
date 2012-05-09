package com.ashrafishak.compnet.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.ashrafishak.compnet.hibernate.Company;
import com.ashrafishak.compnet.json.JSONArray;
import com.ashrafishak.compnet.json.JSONException;
import com.ashrafishak.compnet.json.JSONObject;

public class CompNetCompetition {

	private ArrayList<String> inserted = new ArrayList<String>();
	//private Logger moduleLogger;
	private HibernateDao hibernateDao;
	private static final Logger moduleLogger = Logger.getLogger("com.ashrafishak.moduleLogger");

	
	public CompNetCompetition(){}

	
	public void processAllCompetition(){
		File file = new File("companies");
		moduleLogger.info("Start here");
		if (file.isDirectory()){
			for (File f: file.listFiles()){
				moduleLogger.info("permalink: "+f.getName().replace(".json", ""));
				processCompetition(f);
			}
		}
	}
	public void processCompetition (File input){
		//check the file from "companies" folder
		//String permalink = input.getName().replace(".json", "");
		if (input.exists() && !input.getName().equals(".svn")){
			Transaction tx = null;
			try {
				BufferedReader bf = new BufferedReader(new FileReader(input));
				StringBuffer sb = new StringBuffer();
				while (bf.ready()){
					sb.append(bf.readLine());
				}
				bf.close();
				String result = sb.toString();
				System.out.println(result);
				JSONObject myjson = new JSONObject(result);
				String currCompany = CompNetFetcher.checkJSONTypeString(myjson, "permalink");
				Company c = new Company();
				c.setPermalink(currCompany);
				inserted.add(currCompany);
				Session sess = this.hibernateDao.getSessionFactory().openSession();
				tx = sess.beginTransaction();
				int currID = CompNetLookup.getCompanyID(c, sess);
				if (!myjson.isNull("competitions")){
					JSONArray comps = myjson.getJSONArray("competitions");
					for (int i = 0; i < comps.length(); i++){
						JSONObject comp = comps.getJSONObject(i).getJSONObject("competitor");
						String name = comp.getString("name");
						String perm = comp.getString("permalink");
						Company c1 = new Company();
						c1.setPermalink(perm);
						int compID = CompNetLookup.getCompanyID(c1, sess);
						if (compID != 0){
							if (!inserted.contains(perm)){
								Query q = sess.createSQLQuery("INSERT INTO competition (competition_1, competition_2) VALUES ("+currID+","+compID+")");
								/*
								q.setInteger("one", currID);
								q.setInteger("two", compID);
								*/
								q.executeUpdate();
							}
						}
					}
				}
				tx.commit();
				sess.close();
			} catch (IOException e) {
				moduleLogger.error("IOException",e);
			} catch (JSONException e) {
				moduleLogger.error("JSONException",e);
			} catch (Exception e){
				if (tx != null){
					tx.rollback();
				}
				moduleLogger.error("Transaction error",e);
			}
		} else {
			
		}
	}
	public void setInserted(ArrayList<String> inserted) {
		this.inserted = inserted;
	}

	public ArrayList<String> getInserted() {
		return inserted;
	}


	public void setHibernateDao(HibernateDao hibernateDao) {
		this.hibernateDao = hibernateDao;
	}

	public HibernateDao getHibernateDao() {
		return hibernateDao;
	}
	
	public static void main(String[] args){
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		CompNetCompetition c = (CompNetCompetition) context.getBean("compNetComps");
		c.processAllCompetition();
	}
	
	
	
}
