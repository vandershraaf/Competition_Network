package com.ashrafishak.compnet.hibernate;

public class City {

	private int id;
	private String city;
	private String country;
	
	public City(){}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCity() {
		return city;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCountry() {
		return country;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
	
}
