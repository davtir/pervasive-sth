package com.pervasive.sth.rest;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * @author Alex
 */

public class RESTClient {

	private final static Logger LOG = Logger.getLogger(RESTClient.class.getName());

	String _url;
	String _headerName;
	String _headerValue;

	public RESTClient(String s) {
		_url = s;
	}


	public void addHeader(String name, String value) {
		_headerName = name;
		_headerValue = value;

	}

	public String executePost(String payload) throws Exception {

		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(_url);
		httpPost.setHeader(_headerName, _headerValue);
		StringEntity entity = new StringEntity(payload, HTTP.UTF_8);
		httpPost.setEntity(entity);
		HttpResponse response = httpClient.execute(httpPost);
		HttpEntity entity1 = response.getEntity();
		return EntityUtils.toString(entity1);
	}

	public String executeGet() throws Exception { //If you want to use get method to hit server

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(_url);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		return httpClient.execute(httpget, responseHandler);
	}
}