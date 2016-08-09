package com.pervasive.sth.network;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.pervasive.sth.exceptions.InvalidRESTClientParametersException;

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
 * @brief	This class provides to implement the REST client used by both
 * 			treasure and hunter device
 */
public class RESTClient {

	private final static Logger LOG = Logger.getLogger(RESTClient.class.getName());

	/*
	 * The URL of the server entry point
	 */
	String _url;

	/*
	 * The HTTP header name
	 */
	String _headerName;

	/*
	 * The HTTP header payload
	 */
	String _headerValue;

	/**
	 * @param	url
	 * @throws	InvalidRESTClientParametersException
	 * @brief	Initialize the REST client
	 */
	public RESTClient(String url) throws InvalidRESTClientParametersException {
		if ( url == null ) {
			throw new InvalidRESTClientParametersException("Invalid URL ( " + url + ")");
		}
		_url = url;
	}

	/**
	 *
	 * @param	name
	 * @param	value
	 * @throws	InvalidRESTClientParametersException
	 * @brief	Sets header name and value to the HTTP header
	 */
	public void addHeader(String name, String value) throws InvalidRESTClientParametersException {
		if ( _headerName == null || value == null ) {
			throw new InvalidRESTClientParametersException("Invalid parameters ( name = " + name + ", value = " + value + ")");
		}
		_headerName = name;
		_headerValue = value;

	}

	/**
	 *
	 * @param	payload
	 * @return
	 * @throws	Exception
	 * @brief	Executes HTTP POST on the server URL with the input payload
	 */
	public String executePost(String payload) throws Exception {
		if ( payload == null ) {
			throw new InvalidRESTClientParametersException("Invalid payload ( payload = " + payload +  ")");
		}
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(_url);
		httpPost.setHeader(_headerName, _headerValue);
		StringEntity entity = new StringEntity(payload, HTTP.UTF_8);
		httpPost.setEntity(entity);
		HttpResponse response = httpClient.execute(httpPost);
		HttpEntity entity1 = response.getEntity();
		return EntityUtils.toString(entity1);
	}

	/**
	 * @return
	 * @throws	Exception
	 * @brief	Executes HTTP GET on the server URL
	 */
	public String executeGet() throws Exception {

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(_url);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		return httpClient.execute(httpget, responseHandler);
	}
}