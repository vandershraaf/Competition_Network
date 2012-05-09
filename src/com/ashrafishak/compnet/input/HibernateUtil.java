package com.ashrafishak.compnet.input;

import org.hibernate.SessionFactory;



public class HibernateUtil {

	private static final SessionFactory sessionFactory;
	private static HibernateDao hibernateDao2;
	private HibernateDao hibernateDao;
	

	static {
		try{
			sessionFactory = hibernateDao2.getSessionFactory();
		} catch (Throwable tw){
			throw new ExceptionInInitializerError(tw);
		}
	}

	public HibernateDao getHibernateDao() {
		return hibernateDao;
	}

	public void setHibernateDao(HibernateDao hibernateDao) {
		this.hibernateDao = hibernateDao;
		hibernateDao2 = hibernateDao;
	}

	public static SessionFactory getSessionfactory() {
		return sessionFactory;
	}
}
