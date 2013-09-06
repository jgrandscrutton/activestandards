package com.activestandards;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.engine.SlingRequestProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.activestandards.rest.Client;
import com.activestandards.rest.Result;

import com.day.cq.contentsync.handler.util.RequestResponseFactory;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.wcm.webservicesupport.Configuration;
import com.day.cq.wcm.webservicesupport.ConfigurationManager;

@SlingServlet( paths={
		"/services/as/quickcheck/assetError",
		"/services/as/quickcheck/getContent"
		} )
public class ServiceProxyServlet extends SlingAllMethodsServlet {
	/**
	 * 
	 */
	@Reference
	protected ConfigurationManager cfgMgr;
	
	@Reference
	protected RequestResponseFactory requestResponseFactory;
	
	@Reference
	protected SlingRequestProcessor requestProcessor;
	
	private static final long serialVersionUID = 3351665482064440449L;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final String serviceIdentifier = "activestandards";
	private String apiKey;
    private String assetErrorUrl = "http://api.activestandards.com/v1/assets/%s/errors/%s?highlightSource=%s&apiKey=%s";
	
    private void getApiKey(InheritanceValueMap pageProperties) {
		String[] services = (String[]) pageProperties.getInherited("cq:cloudserviceconfigs", String[].class);
		Configuration cfg = cfgMgr.getConfiguration(serviceIdentifier, services);
		
		if (cfg != null) {
			cfg = cfg.getParent().adaptTo(Configuration.class);
			
			if (cfg != null) {
				this.apiKey = cfg.get("apikey", null);
				log.info("API Key from CSConfig " + apiKey);
			}
		}
	}
    
	@Override
	public void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
		try
        {
            String uri = request.getRequestURI();
            
            ResourceResolver resolver = request.getResourceResolver();
            Resource currentResource = resolver.getResource(uri);
            InheritanceValueMap pageProperties = currentResource.adaptTo(InheritanceValueMap.class);

            if ("/services/as/quickcheck/assetError".equals(uri)) {
				String assetId = request.getParameter("assetId");
                String checkpointId = request.getParameter("checkpointId");
                String highlightSource = request.getParameter("highlightSource");

                Client restClient = new Client();
                Result result = restClient.doGetCall(String.format(assetErrorUrl, assetId, checkpointId, highlightSource, this.apiKey));
                response.setContentType("text/html");
                PrintWriter out = response.getWriter();
                out.write(new String(result.responseBody));
            }
            
            if ("/services/as/quickcheck/getContent".equals(uri)) {
            	String path = request.getParameter("path");
            	log.info("doGet : getContent path : " + path);
            	
            	if (path != null) {
            		path = path.replace("/cf#", "");
            		path = path.replace(".quickcheck", "");
            		
            		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            		log.info("doGetCall : getContent : Creating request");
            		HttpServletRequest mockRequest = requestResponseFactory.createRequest("GET", path);
            		HttpServletResponse mockResponse = requestResponseFactory.createResponse(outputStream);
            		log.info("doGetCall : getContent : Processing request");
            		requestProcessor.processRequest(mockRequest, mockResponse, resolver);
            		
            		mockResponse.getWriter().flush();
            		PrintWriter out = response.getWriter();
            		
            		log.info("doGetCall : getContent : Returning response");
            		response.setContentType("text/html");
            		out.write(outputStream.toString());
            	} else {
            		response.setContentType("application/json");
            		PrintWriter out = response.getWriter();
            		out.write("{\"refererUrl\" : \"No referer URL to 'GET'\"}");
            	}
            }
        } catch (Exception e) {
        	log.error("Error", e);
        }
	}

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

    }
}