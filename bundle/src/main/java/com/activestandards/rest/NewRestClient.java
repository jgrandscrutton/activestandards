package com.activestandards.rest;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewRestClient {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	public Result doPostCall(String url, List<NameValuePair> params) {
		Result result = new Result();
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		
		try {
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			
			if (entity != null) {
				InputStream in = entity.getContent();
				
				try {
					result.responseBody = IOUtils.toByteArray(in);
				} finally {
					in.close();
				}
			}
		} catch (Exception e) {
			log.error("doPostCall error ", e);
		}
		
		return result;
	}
}
