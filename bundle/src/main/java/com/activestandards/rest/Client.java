package com.activestandards.rest;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Client {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private String proxyHost = null;
	private int proxyPort = 80;
	
	public Client() {};
	
	public Client(String proxyHost, String proxyPort) {
		if (proxyHost != null)
			this.proxyHost = proxyHost;
		
		if (proxyPort != null)
			this.proxyPort = Integer.parseInt(proxyPort);
	}
	
	public Result doGetCall(String url) throws Exception {
		Result result = new Result();
		
		HttpClient httpClient = new HttpClient();
		
		if (this.proxyHost != null)
			httpClient.getHostConfiguration().setProxy(this.proxyHost, this.proxyPort);
		GetMethod get = new GetMethod(url);
		
		logger.debug("rest.Client " + get.getQueryString());
		result.resultCode = httpClient.executeMethod(get);
		result.contentType = getContentType(get);
		logger.info("rest.Client GET resultCode: " + result.resultCode);
        if (result.resultCode <300) {
            result.responseBody = IOUtils.toByteArray(get.getResponseBodyAsStream());
        }
		
		return result;
	}
	
	public Result doPostCall(String url, Map<String,String> bodyParts) throws Exception {
        Result restResult = new Result();
        logger.info("doPostCall : url " + url);
        HttpClient httpclient = new HttpClient();
        PostMethod post = new PostMethod(url);

        if (bodyParts != null) {
        	post.setRequestBody(addNameValuePairs(bodyParts));
        }
        
        restResult.resultCode = httpclient.executeMethod(post);
        logger.info("rest.Client POST resultCode: " + restResult.resultCode);
        if (restResult.resultCode <300) {
            restResult.responseBody = IOUtils.toByteArray(post.getResponseBodyAsStream());
        }
        
        return restResult;

    }

    public Result doDeleteCall(String url) throws Exception {
        Result result = new Result();
        logger.debug("doDeleteCall : " + url);
        HttpClient client = new HttpClient();
        DeleteMethod delete = new DeleteMethod(url);
        
        result.resultCode = client.executeMethod(delete);
        logger.info("doDeleteCall : result code " + result.resultCode);
        /*if (result.resultCode < 300) {
        	result.responseBody = IOUtils.toByteArray(delete.getResponseBodyAsStream());
        }*/
        
        return result;
    }

    public Result doPutCall(String url, String requestData) throws Exception {
    	Result result = new Result();
        logger.info("doPutCall : " + url);
        HttpClient client = new HttpClient();
        PutMethod put = new PutMethod(url);
        
        if (requestData != null) {
        	put.setRequestEntity(new StringRequestEntity(requestData, "application/x-www-form-urlencoded", "UTF-8"));
        }
        
        result.resultCode = client.executeMethod(put);
        logger.info("doPutCall : result code " + result.resultCode);
        
        if (result.resultCode < 300) {
        	result.responseBody = IOUtils.toByteArray(put.getResponseBodyAsStream());
        }
        
        return result;
    }
    
    private NameValuePair[] addNameValuePairs(Map<String,String> params) {
        if (params == null)
            return null;
        int partSize = params.size();
        NameValuePair[] parts = null;

        if (partSize > 0) {
            parts = new NameValuePair[partSize];
            int partCount = 0;
            Set<String> paramKeys = params.keySet();
            Iterator<String> itKeys = paramKeys.iterator();
            while (itKeys.hasNext()) {
                String pname = (String) itKeys.next();
                String pvalue = (String) params.get(pname);
                parts[partCount] = new NameValuePair(pname,pvalue);
                //logger.info("param key " + pname + " " + pvalue);
                partCount++;
            }
        }
        return parts;

    }

    private String getContentType(HttpMethodBase method) {
        String response = "";

        Header header = method.getResponseHeader("Content-Type");
        if (header != null) {
            response = header.getValue();
        }
        return response;
    }
}
