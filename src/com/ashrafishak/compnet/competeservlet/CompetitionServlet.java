package com.ashrafishak.compnet.competeservlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.ashrafishak.compnet.input.CompNetCompetition;

public class CompetitionServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp){
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
		CompNetCompetition c = (CompNetCompetition) context.getBean("compNetComps");
		try {
			resp.getWriter().println("Company Servlet is running!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Here");
		c.processAllCompetition();
		
	}
	
}
