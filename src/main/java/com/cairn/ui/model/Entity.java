package com.cairn.ui.model;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public class Entity{
    public static HttpEntity<String> getEntityWithBody( User usr, String url, String requestBody){
    	HttpEntity<String> entity = null;
    	HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());
	    headers.add("Content-Type", "application/json");
	    entity = new HttpEntity<>(requestBody, headers);
    	return entity;
    }
    
    public static HttpEntity<String> getEntity( User usr, String url){
    	HttpEntity<String> entity = null;
    	HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());
	    headers.add("Content-Type", "application/json");
	    entity = new HttpEntity<>(headers);
    	return entity;
    }
}