package com.ashrafishak.compnet.hibernate;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Company {

	private int id;
	private String permalink;
	private String name;
	private String website;
	private String description;
	private String totalFunding;
	private int numEmployees;
	private String imageLink;
	private Category category;
	private int foundedYear;
	private Set<Tag> tags = new HashSet<Tag>();
	private Set<Office> offices = new HashSet<Office>();
	private ArrayList<String> competitions = new ArrayList<String>();
	
	public Company () {}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setPermalink(String permalink) {
		this.permalink = permalink;
	}

	public String getPermalink() {
		return permalink;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getWebsite() {
		return website;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setTotalFunding(String totalFunding) {
		this.totalFunding = totalFunding;
	}

	public String getTotalFunding() {
		return totalFunding;
	}

	public void setNumEmployees(int numEmployees) {
		this.numEmployees = numEmployees;
	}

	public int getNumEmployees() {
		return numEmployees;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Category getCategory() {
		return category;
	}

	
	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public Set<Tag> getTags() {
		return tags;
	}

	public void setOffices(Set<Office> offices) {
		this.offices = offices;
	}

	public Set<Office> getOffices() {
		return offices;
	}

	public void setImageLink(String imageLink) {
		this.imageLink = imageLink;
	}

	public String getImageLink() {
		return imageLink;
	}

	public void setFoundedYear(int foundedYear) {
		this.foundedYear = foundedYear;
	}

	public int getFoundedYear() {
		return foundedYear;
	}

	public void setCompetitions(ArrayList<String> competitions) {
		this.competitions = competitions;
	}

	public ArrayList<String> getCompetitions() {
		return competitions;
	}

	
	
	
}
