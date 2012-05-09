package com.ashrafishak.compnet.companyservlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.ashrafishak.compnet.input.CompanyQueue;

public class CompanyServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp){
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
		CompanyQueue q = (CompanyQueue) context.getBean("companyQueue");
		try {
			resp.getWriter().println("Company Servlet is running!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Here");
		//q.processCompanies();
		
	}
	
}
