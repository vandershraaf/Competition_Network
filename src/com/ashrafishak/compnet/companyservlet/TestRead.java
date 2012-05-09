package com.ashrafishak.compnet.companyservlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class TestRead {
	
	public static void main(String[] args) {
		URL url;
		try {
			url = new URL("http://localhost:8080/Output/outputservlet?from=facebook&egocentric=1&type=json");
			BufferedReader bf = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer sb = new StringBuffer();
			while (bf.ready()){
				sb.append(bf.readLine());
			}
			System.out.println(sb.toString());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
