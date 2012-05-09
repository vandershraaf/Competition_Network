package com.ashrafishak.compnet.hibernate;

public class Office {

	private int id;
	private City city;
	private double latitude;
	private double longitude;
	
	public Office(){}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public City getCity() {
		return city;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLongitude() {
		return longitude;
	}
	
	
}
