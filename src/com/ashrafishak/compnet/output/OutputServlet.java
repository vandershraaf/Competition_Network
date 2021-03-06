package com.ashrafishak.compnet.output;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.ashrafishak.compnet.hibernate.Company;
import com.ashrafishak.compnet.hibernate.Office;
import com.ashrafishak.compnet.input.CompNetFetcher;
import com.ashrafishak.compnet.input.CompNetLookup;
import com.ashrafishak.compnet.input.HibernateDao;
import com.ashrafishak.compnet.json.JSONArray;
import com.ashrafishak.compnet.json.JSONException;
import com.ashrafishak.compnet.json.JSONObject;
import com.ashrafishak.compnet.json.JSONStringer;
import com.ashrafishak.compnet.json.JSONWriter;

public class OutputServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static final Logger moduleLogger = Logger.getLogger("com.ashrafishak.moduleLogger");
	private HashMap<String, String> filterColors = new HashMap<String, String>();

	public void doPost(HttpServletRequest req,HttpServletResponse resp){
		String from = req.getParameter("from");
		int egocentric = Integer.parseInt(req.getParameter("egocentric"));
		String filter = req.getParameter("filter");
		
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
		CompNetFetcher fetcher = (CompNetFetcher) context.getBean("compNetFetcher");
		String baseURL = fetcher.getBaseURL();
		String result = "";
		if (egocentric <= 10){
			if (filter.equals("none")){
				//result = outputJSON(baseURL, from, egocentric,fetcher);
				result= outputJSONFilter(baseURL, from, egocentric, fetcher, null);

			} else {
				result= outputJSONFilter(baseURL, from, egocentric, fetcher, filter);
			}
		} else {
			try {
				resp.getWriter().println("Too many egocentric");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		req.setAttribute("result", result);	
		req.setAttribute("colors", filterColors);
		RequestDispatcher dispatcher = 
			  getServletContext().getRequestDispatcher("/view.jsp");
		try {
			dispatcher.forward(req, resp);
		} catch (ServletException e1) {
			moduleLogger.error("ServletException",e1);
		} catch (IOException e1) {
			moduleLogger.error("IOException",e1);
		}
	}
	public void doGet(HttpServletRequest req, HttpServletResponse resp){
		// GET parameter: from, egocentric, mode [full|partial]
			// from = the permalink for starting node
			// egocentric = N-egocentric network (for partial mode, only 10 is allowed)
			// mode = full (can be run if all competition data is already in database
			// 		  partial (when not all in database)
		//CompNetCompetition c = (CompNetCompetition) context.getBean("compNetComps");
	
		String from = req.getParameter("from");
		int egocentric = Integer.parseInt(req.getParameter("egocentric"));
		String type = req.getParameter("type");
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
		CompNetFetcher fetcher = (CompNetFetcher) context.getBean("compNetFetcher");
		String baseURL = fetcher.getBaseURL();
		String result = "";
		if (type.equals("csv")){
			if (egocentric <= 10){
				resp.setContentType("application/octet-stream");
			    resp.setHeader("Content-Disposition","attachment;filename="+from+".csv");
				//String result = processNodePartial(from, 0,egocentric);
				result = outputCSV(baseURL, from, egocentric,fetcher);
			} else {
				try {
					resp.getWriter().println("Too many egocentric");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (type.equals("json")){
			if (egocentric <= 10){
				resp.setContentType("application/octet-stream");
			    resp.setHeader("Content-Disposition","attachment;filename="+from+".json");
				//String result = processNodePartial(from, 0,egocentric);
				result = outputJSON(baseURL, from, egocentric,fetcher);
			} else {
				try {
					resp.getWriter().println("Too many egocentric");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			try {
				resp.getWriter().println("This file type is not available");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			ServletOutputStream out = resp.getOutputStream();
			InputStream in = 
                new ByteArrayInputStream(result.getBytes("UTF-8"));
			byte[] outputByte = new byte[4096];
			while(in.read(outputByte, 0, 4096) != -1){
				out.write(outputByte, 0, 4096);
			}
			in.close();
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}			
	}
	
	public String outputJSONFilter(String baseURL, String permalink, int maxLevel, CompNetFetcher fetcher, String filter){
		Stack<JSON> objects = new Stack<JSON>();
		ArrayList<String> list = new ArrayList<String>();
		//ArrayList<Company> companies = new ArrayList<Company>();
		HashMap<String, Company> companies = new HashMap<String, Company>();
		ArrayList<String> colors = new ArrayList<String>();
		JSONStringer jwrite = new JSONStringer();
		colors.add("#FF0000");
		colors.add("#00FFFF");
		colors.add("#0000FF");
		colors.add("#0000A0");
		colors.add("#ADD8E6");
		colors.add("#800080");
		colors.add("#FFFF00");
		colors.add("#00FF00");
		colors.add("#FF00FF");
		colors.add("#C0C0C0");
		colors.add("#FFFFFF");
		colors.add("#FFA500");
		colors.add("#A52A2A");
		colors.add("#800000");
		colors.add("#008000");
		colors.add("#808000");
		int count = 0;
		JSON curr = new JSON();
		curr.setLevel(1);
		curr.setPermalink(permalink);
		curr.setParent(null);
		objects.push(curr);
		SessionFactory factory = fetcher.getHibernateDao().getSessionFactory();
		Session sess = factory.openSession();
		//Transaction tx = sess.beginTransaction();
		try {
			while (!objects.isEmpty()){
				JSON popped = objects.pop();
				int currLevel = popped.getLevel();
				if (currLevel <= maxLevel){
					// START PARSING THE JSON
					JSONObject currNode = processURL(baseURL, popped.getPermalink());
					Company currCompany = null;
					if (currNode != null){
						 currCompany = CompNetFetcher.getCompany(currNode, sess);
						if (currCompany != null && !companies.containsKey(popped.getPermalink())){
							 moduleLogger.info("parent company: "+currCompany.getName());
							companies.put(popped.getPermalink(), currCompany);
						}
					}
					if (currNode != null && !currNode.isNull("competitions")){
						JSONArray comps = currNode.getJSONArray("competitions");
						for (int i = 0; i < comps.length(); i++){
							JSONObject comp = comps.getJSONObject(i).getJSONObject("competitor");
							JSON child = new JSON();
							child.setPermalink(comp.getString("permalink"));
							child.setLevel(currLevel + 1);
							child.setParent(currNode.getString("permalink"));
							if (popped.getParent() == null || !popped.getParent().equals(comp.getString("permalink"))){
								//sb.append(currNode.getString("permalink")+","+comp.getString("permalink")+"\n");
								String pair = currNode.getString("permalink")+","+comp.getString("permalink");
								if (!list.contains(pair)){
									list.add(pair);
									currCompany.getCompetitions().add(comp.getString("permalink"));
								}
								objects.push(child);
							}
							if (!companies.containsKey(comp.getString("permalink"))){
								JSONObject newNode = processURL(baseURL, comp.getString("permalink"));
								if (newNode != null){
									Company c = CompNetFetcher.getCompany(newNode, sess);
									if (c != null){
										moduleLogger.info("competitions: "+c.getName());
										companies.put(comp.getString("permalink"), c);
									}
								} else {
									moduleLogger.info("this company is null: "+comp.getString("permalink"));
								}
							}
						}
					}
				}
			}
			moduleLogger.info("companies size: "+companies.size());
			jwrite.array();
			for (String strCompany: companies.keySet()){
				Company company = companies.get(strCompany);
				moduleLogger.info("company: "+company.getName());
				if (company != null){
					jwrite.object();
					jwrite.key("id").value(company.getPermalink());
					jwrite.key("name").value(company.getName());
					jwrite.key("data");
						jwrite.object();
						moduleLogger.info("name: "+company.getPermalink());
						moduleLogger.info("description: "+company.getDescription());
						moduleLogger.info("num_employees: "+company.getNumEmployees());
						jwrite.key("website").value(company.getWebsite());
						jwrite.key("description").value(company.getDescription());
						jwrite.key("total_funding").value(company.getTotalFunding());
						jwrite.key("num_employees").value(company.getNumEmployees());
						String category = null;
						if (company.getCategory() != null){
							category = company.getCategory().getCategory();
							jwrite.key("category").value(company.getCategory().getCategory());
						}
						jwrite.key("founded_year").value(company.getFoundedYear());
						String cityFull = null;
						if (company.getOffices() != null && company.getOffices().size() > 0){
							Office firstOffice = (Office)company.getOffices().toArray()[0];
							if (firstOffice.getCity() != null){
								cityFull = firstOffice.getCity().getCity()+","+firstOffice.getCity().getCountry();
								jwrite.key("city").value(cityFull);
							}
						}
						String currColor = null;
						if (filter != null){
							if (filter.equals("category")){
								if (filterColors.get(category) == null){
									int mod = count % (colors.size());
									filterColors.put(category, colors.get(mod));
									count++;
								}
								currColor = filterColors.get(category);
							} else if (filter.equals("city")){
								if (filterColors.get(cityFull) == null){
									int mod = count % (colors.size());
									filterColors.put(cityFull, colors.get(mod));
									count++;
								}
								currColor = filterColors.get(cityFull);
							} 
						}
						if (currColor == null){
							jwrite.key("$color").value("#00FF00");
						} else {
							jwrite.key("$color").value(currColor);
						}
						jwrite.key("$type").value("circle");
						jwrite.key("$dim").value(7);
						jwrite.endObject();
					if (company.getCompetitions() != null && company.getCompetitions().size() > 0){
							jwrite.key("adjacencies");
							jwrite.array();
						// TODO: check if the competitions are in the companies list
						for (String comp: company.getCompetitions()){
							jwrite.object();
							jwrite.key("nodeFrom").value(company.getPermalink());
							jwrite.key("nodeTo").value(comp);
							jwrite.key("data");
								jwrite.object();
								jwrite.key("$color").value("#00FF00");
								jwrite.endObject();
							jwrite.endObject();
						}
							jwrite.endArray();
					}
					jwrite.endObject();
				} else {
					moduleLogger.info("str null: "+strCompany);
				}
			}
			jwrite.endArray();
			moduleLogger.info("json string up: "+jwrite.toString());
			//tx.commit();
			sess.close();
		} catch (JSONException e){
			e.printStackTrace();
		} catch (Exception e){
			/*
			if (tx.isActive()){
				tx.rollback();
			}
			*/
			moduleLogger.error("Transaction error",e);
		}
		moduleLogger.info("json string down: "+jwrite.toString());
		return jwrite.toString();
	}
	
	public String outputJSON(String baseURL, String permalink, int maxLevel, CompNetFetcher fetcher){
		ArrayList<String> list = getOutput(baseURL, permalink,maxLevel, fetcher);
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		for (String p: list){
			System.out.println(p);
			String[] split = p.split(",");
			String first = split[0];
			String second = split[1];
			if (map.get(first) == null){
				map.put(first, new ArrayList<String>());
			}
			map.get(first).add(second);
		}
		try {
			JSONStringer jwrite = new JSONStringer();
			jwrite.array();
			for (String currNode: map.keySet()){
				jwrite.object();
				jwrite.key("adjacencies");
					jwrite.array();
					for (String adj: map.get(currNode)){
						jwrite.object();
						jwrite.key("nodeFrom").value(currNode);
						jwrite.key("nodeTo").value(adj);
						jwrite.key("data");
							jwrite.object();
							jwrite.key("color").value("#557EAA");
							jwrite.endObject();
						jwrite.endObject();
					}
					jwrite.endArray();
				jwrite.key("id").value(currNode);
				jwrite.key("name").value(currNode);
				jwrite.key("data");
					jwrite.object();
					jwrite.key("color").value("#EBB056");
					jwrite.key("type").value("circle");
					jwrite.key("dim").value(11);
					jwrite.key("makan").value("ya");
					jwrite.endObject();
				
				jwrite.endObject();
				
			}
			jwrite.endArray();
			return jwrite.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	

	
	public static void main(String[] args){
		HashMap<String,  ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		map.put("twitter", new ArrayList<String>());
		map.get("twitter").add("facebook");
		map.get("twitter").add("zoho");
		map.put("facebook", new ArrayList<String>());
		map.get("facebook").add("zoho");
		/*
		long start = System.currentTimeMillis();
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		CompNetFetcher fetcher = (CompNetFetcher) context.getBean("compNetFetcher");
		String baseURL = fetcher.getBaseURL();
		String output = new OutputServlet().outputJSON(baseURL,"facebook", 2, fetcher);
		System.out.println(output);
		//System.out.println(new OutputServlet().testIterate("http://api.crunchbase.com/v/1/company/","facebook",3));
		long end = System.currentTimeMillis();
		System.out.println("program time: "+(end-start));
		*/
	}

	public String outputCSV(String baseURL, String permalink, int maxLevel, CompNetFetcher fetcher){
		ArrayList<String> list = getOutput (baseURL, permalink,maxLevel, fetcher);
		StringBuffer sb = new StringBuffer();
		for (String p: list){
			sb.append(p+"\n");
		}
		return sb.toString();
	}
	
	public ArrayList<String> getOutput (String baseURL, String permalink, int maxLevel, CompNetFetcher fetcher){
		Stack<JSON> objects = new Stack<JSON>();
		ArrayList<String> list = new ArrayList<String>();
		JSON curr = new JSON();
		curr.setLevel(1);
		curr.setPermalink(permalink);
		curr.setParent(null);
		objects.push(curr);
		SessionFactory factory = fetcher.getHibernateDao().getSessionFactory();
		try {
			while (!objects.isEmpty()){
				JSON popped = objects.pop();
				int currLevel = popped.getLevel();
				if (currLevel <= maxLevel){
					JSONObject currNode = processURL(baseURL, popped.getPermalink());
					Company companyCurr = new Company();
					companyCurr.setPermalink(currNode.getString("permalink"));
					Session sess = factory.openSession();
					if (CompNetLookup.getCompanyID(companyCurr, sess) == 0){
						fetcher.insertCompanyToDB(currNode, sess);
					}
					sess.close();
					if (!currNode.isNull("competitions")){
						JSONArray comps = currNode.getJSONArray("competitions");
						for (int i = 0; i < comps.length(); i++){
							JSONObject comp = comps.getJSONObject(i).getJSONObject("competitor");
							JSON child = new JSON();
							child.setPermalink(comp.getString("permalink"));
							child.setLevel(currLevel + 1);
							child.setParent(currNode.getString("permalink"));
							if (popped.getParent() == null || !popped.getParent().equals(comp.getString("permalink"))){
								//sb.append(currNode.getString("permalink")+","+comp.getString("permalink")+"\n");
								String pair = currNode.getString("permalink")+","+comp.getString("permalink");
								if (!list.contains(pair)){
									list.add(pair);
								}
								objects.push(child);
							}
						}
					}
				}
			}
		} catch (JSONException e){
			e.printStackTrace();
		}
		return list;
	}
	
	
	
	private class JSON {
		private String permalink;
		private String parent;
		private int level;

		public void setLevel(int level) {
			this.level = level;
		}
		public int getLevel() {
			return level;
		}
		public void setPermalink(String permalink) {
			this.permalink = permalink;
		}
		public String getPermalink() {
			return permalink;
		}
		public void setParent(String parent) {
			this.parent = parent;
		}
		public String getParent() {
			return parent;
		}
		
	}
	public boolean checkCompEntry (int compId1, int compId2, Session sess){
		Query q = sess.createSQLQuery("SELECT * FROM competition WHERE competition_1 = :one AND competition_2 = :two");
		q.setInteger("one", compId1);
		q.setInteger("two", compId2);
		Object lala = q.uniqueResult();
		if (lala == null){
			return false;
		} else {
			return true;
		}		
	}
	public JSONObject processURL (String baseURL, String permalink){
		String link = baseURL+permalink+".js";
		moduleLogger.info("["+permalink+"] "+link);
		URL url;
		
		try {
			url = new URL(link);
			InputStream in = url.openStream();
			String result = IOUtils.toString(in);
			in.close();
			JSONObject myjson = new JSONObject(result);
			return myjson;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/*public String processNodePartial (String permalink, int parentID, int currEgo){
		if (currEgo == 0){
			return "";
		}
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
		CompNetFetcher fetcher = (CompNetFetcher) context.getBean("compNetFetcher");
		HibernateDao hibernateDao = fetcher.getHibernateDao();
		String baseURL = fetcher.getBaseURL();
		Transaction tx = null;
		try {
			Session sess = hibernateDao.getSessionFactory().openSession();
			StringBuffer sb = new StringBuffer();
			Company currCompany = new Company();
			currCompany.setPermalink(permalink);
			if (CompNetLookup.getCompanyID(currCompany, sess) == 0){
				fetcher.parseJson(baseURL, permalink);
			} 
			JSONObject currObj = processURL(baseURL, permalink);
			if (!currObj.isNull("competitions")){
				tx = sess.beginTransaction();
				JSONArray comps = currObj.getJSONArray("competitions");
				for (int i = 0; i < comps.length(); i++){
					JSONObject comp = comps.getJSONObject(i).getJSONObject("competitor");
					String perm = comp.getString("permalink");
					Company compComp = new Company();
					compComp.setPermalink(perm);
					
					if (CompNetLookup.getCompanyID(compComp, sess) == 0){
						fetcher.parseJson(baseURL, perm);
					}
					
				}
				int currID = CompNetLookup.getCompanyID(currCompany, sess);
				for (int i = 0; i < comps.length(); i++){
					JSONObject comp = comps.getJSONObject(i).getJSONObject("competitor");
					String perm = comp.getString("permalink");
					Company compCompany = new Company();
					compCompany.setPermalink(perm);
					int compID = CompNetLookup.getCompanyID(compCompany, sess);
					
					if (compID != 0){
						if (compID != parentID){
							if (checkCompEntry(currID, compID,sess) == false){
								Query q = sess.createSQLQuery("INSERT INTO competition (competition_1, competition_2) VALUES ("+currID+","+compID+")");
								q.executeUpdate();
							}
						}	
					}
					
					if (compID != parentID){
						sb.append(permalink+","+perm+"\n");
					}
					sb.append(processNodePartial(perm, currID,currEgo - 1));
				}
				tx.commit();
				sess.close();
				return sb.toString();
			}
			if (sess.isConnected()){
				sess.close();
			}
			
		} catch (Exception e){
			if (tx.isActive()){
				tx.rollback();
			}
			moduleLogger.error("Transaction error",e);
			
		}
		return null;
	}*/
	public void setFilterColors(HashMap<String, String> filterColors) {
		this.filterColors = filterColors;
	}
	public HashMap<String, String> getFilterColors() {
		return filterColors;
	}
	
	
	
	
	
	
	
	
	
}
