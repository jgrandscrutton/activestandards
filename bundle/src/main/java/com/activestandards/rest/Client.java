package com.activestandards.rest;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.io.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Client {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public Result doGetCall(String url) throws Exception {
		Result result = new Result();
		
		HttpClient httpClient = new HttpClient();
		//httpClient.getHostConfiguration().setProxy("localhost", 8888);
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

    public Result doDeleteCall(String url, String requestData) throws Exception {
        Result result = new Result();
        logger.info("doPutCall : " + url);
        HttpClient client = new HttpClient();
        PutMethod put = new PutMethod(url);
        
        if (requestData != null) {
        	put.setRequestEntity(new StringRequestEntity(requestData, "text/plain", "UTF-8"));
        }
        
        result.resultCode = client.executeMethod(put);
        logger.info("doPutCall : result code " + result.resultCode);
        if (result.resultCode < 300) {
        	result.responseBody = IOUtils.toByteArray(put.getResponseBodyAsStream());
        }
        
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
    private Part[] addParts(Map<String, String> params, Map<String,byte[]> inputDocs) {
        Part[] parts = null;
        int partSize = 0;
        if (params != null) partSize = params.size();
        if (inputDocs != null) partSize = partSize + inputDocs.size();

        if (partSize > 0) {
            parts = new Part[partSize];

            int partCount = 0;
            if (params != null) {
                Set<String> paramKeys = params.keySet();
                Iterator<String> itKeys = paramKeys.iterator();
                while (itKeys.hasNext()) {
                    String pname = (String) itKeys.next();
                    String pvalue = (String) params.get(pname);
                    parts[partCount] = new StringPart(pname, pvalue);
                    partCount++;
                }
            }
            if (inputDocs != null) {
                Set<String> docKeys = inputDocs.keySet();
                Iterator<String> itDocs = docKeys.iterator();
                while (itDocs.hasNext()) {
                    String dname = (String) itDocs.next();
                    byte[] dvalue = (byte[]) inputDocs.get(dname);
                    ByteArrayPartSource baps = new ByteArrayPartSource(dname, dvalue);
                    parts[partCount] = new FilePart(dname,baps);
                    partCount++;
                }
            }
        }

        return parts;
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
