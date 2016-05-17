package com.pervasive.sth.rest;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
 *
 * @author Alex
 */

public class RESTClient {

    private final static Logger LOG = Logger.getLogger(RESTClient.class.getName());

    String url;
    String headerName;
    String headerValue;

    public RESTClient(String s){

        url = s;
    }


    public void addHeader(String name, String value){

        headerName = name;
        headerValue = value;

    }

    public String executePost(String payload) throws Exception {  // If you want to use post method to hit server

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader(headerName, headerValue);
        HttpResponse response = null;
        String result = "";
        StringEntity entity = new StringEntity(payload, HTTP.UTF_8);
        httpPost.setEntity(entity);
        response = httpClient.execute(httpPost);
        HttpEntity entity1 = response.getEntity();
        result = EntityUtils.toString(entity1);
        return result;
    }

    public String executeGet() throws Exception { //If you want to use get method to hit server

        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        String result = null;
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        result = httpClient.execute(httpget, responseHandler);

        return result;
    }
}