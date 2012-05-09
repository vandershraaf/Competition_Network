package com.ashrafishak.compnet.input;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ashrafishak.compnet.hibernate.Category;
import com.ashrafishak.compnet.hibernate.City;
import com.ashrafishak.compnet.hibernate.Company;
import com.ashrafishak.compnet.hibernate.Office;
import com.ashrafishak.compnet.hibernate.Tag;
import com.ashrafishak.compnet.json.*;

public class CompNetFetcher {

	private String baseURL;
	private HibernateDao hibernateDao;
	private static final Logger moduleLogger = Logger.getLogger("com.ashrafishak.moduleLogger");
	private static HashMap<String, Company> companies = new HashMap<String, Company>();
	
	public CompNetFetcher(){
		
	}
	
	// NOTE: Don't have to look at this method
	public static Company getCompany (JSONObject myjson, Session sess){
		if (myjson != null){
			try {
				Company comp;
				String permalink = myjson.getString("permalink");
				comp = companies.get(myjson.getString("permalink"));
				if (comp == null){
					Query q = sess.createQuery("FROM Company WHERE permalink = :perm");
					q.setString("perm", permalink);
					Object obj = q.uniqueResult();
					if (obj == null){
						new CompNetFetcher().insertCompanyToDB(myjson, sess);
						Query q1 = sess.createQuery("FROM Company WHERE permalink = :perm");
						q1.setString("perm", permalink);
						Company obj1 = (Company)q.uniqueResult();
						companies.put(permalink, obj1);
						return obj1;
					} else {
						Company q2 = (Company) obj;
						companies.put(q2.getPermalink(), q2);
						return q2;
					}
				} else {
					return comp;
				}	
			}  catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e){
				moduleLogger.info("Transaction error [fetcher]",e);
			}
		}
		return null;
		
	}
	
	/**
	 * Parse JSON from the JSONObject
	 * @param myjson
	 * @param sess
	 */
	public void insertCompanyToDB(JSONObject myjson, Session sess){
		try {
			String permalink = myjson.getString("permalink");
			
			// Category 
			String category = checkJSONTypeString(myjson,"category_code");
			Category cat = new Category();
			cat.setCategory(category);
			if (!CompNetLookup.checkCategory(cat, sess) && category != null){
				sess.saveOrUpdate(cat);
			} else {
				cat = CompNetLookup.getCategory(cat, sess);
			}
			// Company
			String name = checkJSONTypeString(myjson,"name");
			String homepage = checkJSONTypeString(myjson,"homepage_url");
			// TODO: some of employee num is not really a number
			int numEmp = checkJSONTypeInt(myjson,"number_of_employees");
			int foundedYear = checkJSONTypeInt(myjson,"founded_year");
			// TODO: founded month is not an int????
			String desc = checkJSONTypeString(myjson,"description");
			String totalFunding = checkJSONTypeString(myjson,"total_money_raised");
			String thisImg = null;
			if (!myjson.isNull("image")){
				JSONObject image = myjson.getJSONObject("image");
				if (image != null){
					JSONArray arr = image.getJSONArray("available_sizes");
					thisImg = (String)arr.getJSONArray(0).get(1);
				}
			}
			Company company = new Company();
			company.setPermalink(permalink);
			company.setName(name);
			company.setWebsite(homepage);
			company.setNumEmployees(numEmp);
			company.setFoundedYear(foundedYear);
			company.setDescription(desc);
			company.setTotalFunding(totalFunding);
			company.setCategory(cat);
			company.setImageLink(thisImg);
			// Tag
			String tagList = checkJSONTypeString(myjson,"tag_list");
			HashSet<Tag> tags = null;
			if (tagList != null){
				String[] splitted = tagList.split(",");
				tags = new HashSet<Tag>();
				for (String s: splitted){
					s = s.trim();
					Tag tag = new Tag();
					tag.setName(s);
					if (!CompNetLookup.checkTag(tag, sess)){
						sess.saveOrUpdate(tag);
					} else {
						tag = CompNetLookup.getTag(tag, sess);
					}
					tags.add(tag);
				}
			}
			company.setTags(tags);

			// Office
			JSONArray offices = myjson.getJSONArray("offices");
			HashSet<Office> offic = new HashSet<Office>();
			for (int i = 0; i < offices.length(); i++){
				JSONObject office = offices.getJSONObject(i);
				String c = checkJSONTypeString (office,"city");
				String countryCode = checkJSONTypeString (office,"country_code");
				City city = new City();
				city.setCity(c);
				city.setCountry(countryCode);
				if (!CompNetLookup.checkCity(city, sess)){
					sess.saveOrUpdate(city);
				} else {
					city = CompNetLookup.getCity(city, sess);
				}
				double longitude = checkJSONTypeDouble(office,"longitude");
				double latitude = checkJSONTypeDouble(office,"latitude");
				Office off = new Office();
				off.setCity(city);
				off.setLatitude(latitude);
				off.setLongitude(longitude);
				offic.add(off);
				sess.saveOrUpdate(off);
			}
			company.setOffices(offic);

			sess.merge(company);
			
		} catch (Exception e){
			moduleLogger.info("Exception", e);
		}
		
	}
	
	/**
	 * Get JSONObject from URL
	 * @param baseURL
	 * @param permalink
	 */
	public void parseJson(String baseURL, String permalink){
		String link = baseURL+permalink+".js";
		moduleLogger.info("["+permalink+"] "+link);
		StringBuffer sb = new StringBuffer();
		URL url;
		Transaction tx = null;
		try {
			url = new URL(link);
			new File("companies").mkdir();
			File file = new File("companies/"+permalink+".json");
			FileOutputStream fos = new FileOutputStream(file);
			BufferedReader bf = new BufferedReader(new InputStreamReader(url.openStream()));
			while (bf.ready()){
				fos.write(bf.readLine().getBytes());
			}
			fos.close();
			bf.close();
			bf = new BufferedReader(new FileReader(file));
			while (bf.ready()){
				sb.append(bf.readLine());
			}
			bf.close();
			String result = sb.toString();
			// Initialize JSON Object by using String of the JSON file
			JSONObject myjson = new JSONObject(result);
			//insertCompanyToDB(myjson);
			
		} catch (MalformedURLException e) {
			moduleLogger.error("MalformedURLException", e);
		} catch (IOException e) {
			moduleLogger.error("IOException", e);
		} catch (JSONException e) {
			moduleLogger.error("JSONException", e);
		} 
	}
	

	public static String checkJSONTypeString (JSONObject root, String tag){
		try {
			if (root.get(tag) instanceof String){
				return root.getString(tag);
			} else {
				return null;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static int checkJSONTypeInt (JSONObject root, String tag){
		try {
			if (root.get(tag) instanceof String){
				return 0;
			} else {
				if (root.isNull(tag)){
					return 0;
				} else {
					return root.getInt(tag);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static double checkJSONTypeDouble (JSONObject root, String tag){
		try {
			if (root.get(tag) instanceof String){
				return 0;
			} else {
				if (root.isNull(tag)){
					return 0;
				} else {
					return root.getDouble(tag);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public Timestamp getTimestamp(int month, int day, int year){
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(year, month, day);
		return new Timestamp(cal.getTimeInMillis());
	}
	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	public String getBaseURL() {
		return baseURL;
	}
	
	public static void main(String[] args){
		
		
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		CompNetFetcher cf = (CompNetFetcher) context.getBean("compNetFetcher");
		String base = "http://api.crunchbase.com/v/1/company/";
		//cf.parseJson(base,"hairy-ape");
		/*
		cf.parseJson(base,"facebook");

		
		cf.parseJson(base,"wetpaint");
		cf.parseJson(base,"adventnet");
		cf.parseJson(base,"zoho");
		
		cf.parseJson(base,"digg");
		cf.parseJson(base,"photobucket");
		cf.parseJson(base,"omnidrive");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		cf.parseJson(base,"postini");
		cf.parseJson(base,"twitter");
		*/
		

	}

	
	public void setHibernateDao(HibernateDao hibernateDao) {
		this.hibernateDao = hibernateDao;
	}

	public HibernateDao getHibernateDao() {
		return hibernateDao;
	}

}
