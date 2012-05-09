package com.ashrafishak.compnet.input;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import java.math.*;
import java.util.HashMap;

import com.ashrafishak.compnet.hibernate.*;

public class CompNetLookup {

	private static HashMap<String, Category> categories = new HashMap<String, Category>();
	private static HashMap<String, Tag> tags = new HashMap<String, Tag>();
	private static HashMap<String, City> cities = new HashMap<String, City>();
	private static HashMap<String, Company> companies = new HashMap<String, Company>();

	// CHECKERS
	
	public static boolean checkCategory (Category cat, Session sess){
		Query q = sess.createSQLQuery("SELECT EXISTS (SELECT * FROM category WHERE category = :cat)");
		q.setString("cat", cat.getCategory());
		BigInteger result = (BigInteger)q.uniqueResult();
		if (result.equals(new BigInteger("1"))){
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean checkTag (Tag tag, Session sess){
		Query q = sess.createSQLQuery("SELECT EXISTS (SELECT * FROM tag WHERE name = :tag)");
		q.setString("tag", tag.getName());
		BigInteger result = (BigInteger)q.uniqueResult();
		if (result.equals(new BigInteger("1"))){
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean checkCity (City city, Session sess){
		Query q = sess.createSQLQuery("SELECT EXISTS (SELECT * FROM city WHERE city = :city AND country = :country)");
		q.setString("city", city.getCity());
		q.setString("country", city.getCountry());
		BigInteger result = (BigInteger)q.uniqueResult();
		if (result.equals(new BigInteger("1"))){
			return true;
		} else {
			return false;
		}
	}
	
	// GETTERS
	
	public static Category getCategory (Category cat, Session sess){
		if (categories.get(cat.getCategory()) != null){
			return categories.get(cat.getCategory());
		} else {
			Query q = sess.createQuery("FROM Category WHERE category = :cat");
			q.setString("cat", cat.getCategory());
			Object obj = q.uniqueResult();
			if (obj != null){
				Category cute = (Category) obj;
				categories.put(cute.getCategory(), cute);
				return categories.get(cute.getCategory());
			}
		}
		return null;
	}
	
	public static Tag getTag (Tag tag, Session sess){
		if (tags.get(tag.getName()) != null){
			return tags.get(tag.getName());
		} else {
			Query q = sess.createQuery("FROM Tag WHERE name = :tag");
			q.setString("tag", tag.getName());
			Object obj = q.uniqueResult();
			if (obj != null){
				Tag cute = (Tag) obj;
				tags.put(cute.getName(), cute);
				return tags.get(cute.getName());
			}
		}
		return null;
	}
	
	public static City getCity(City city, Session sess){
		if (cities.get(city.getCity()+city.getCountry())!= null){
			return cities.get(city.getCity()+city.getCountry());
		} else {
			Query q = sess.createQuery("FROM City WHERE city = :city AND country = :country");
			q.setString("city", city.getCity());
			q.setString("country", city.getCountry());
			Object obj = q.uniqueResult();
			if (obj != null){
				City c = (City) obj;
				cities.put(c.getCity()+c.getCountry(), c);
				return cities.get(c.getCity()+c.getCountry());
			}
		}
		return null;	
	}
	
	public static int getCompanyID (Company company, Session sess){
		Query q = sess.createSQLQuery("SELECT id FROM company WHERE permalink = :permalink");
		q.setString("permalink", company.getPermalink());
		Object obj = q.uniqueResult();
		if (obj == null){
			return 0;
		} else {
			return (Integer) obj;
		}
		
	}
	/*
	public static Company getCompany (Company company, Session sess){
		Company comp = companies.get(company.getPermalink());
		if (comp == null){
			Query q = sess.createQuery("FROM Company WHERE permalink = :perm");
			q.setString("perm", company.getPermalink());
			Object obj = q.uniqueResult();
			if (obj == null){
				sess.save(company);
				sess.close();
				Query q1 = sess.createQuery("FROM Company WHERE permalink = :perm");
				q1.setString("perm", company.getPermalink());
				Company obj1 = (Company)q.uniqueResult();
				companies.put(company.getPermalink(), obj1);
				return obj1;
			} else {
				Company q2 = (Company) obj;
				companies.put(q2.getPermalink(), q2);
				return q2;
			}
		} else {
			return comp;
		}		
	}
	
	*/
	
	public static void main(String[] args){
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		HibernateDao dao = (HibernateDao) context.getBean("messageDao");
		Session sess = dao.getSessionFactory().getCurrentSession();
		sess.beginTransaction();
		
		Company comp = new Company();
		comp.setPermalink("facebook");
		//System.out.println(getCompany(comp, sess).getDescription());
		
		/*
		Category cat = new Category();
		cat.setCategory("web");
		System.out.println(getCategory(cat, sess).getCategory());
		
		Tag tag = new Tag();
		tag.setName("facebook");
		System.out.println(getTag(tag, sess).getName());
		
		City city = new City();
		city.setCity("Dublin");
		city.setCountry("IRL");
		System.out.println(getCity(city,sess).getCity());
*/
		sess.getTransaction().commit();
	}
}
