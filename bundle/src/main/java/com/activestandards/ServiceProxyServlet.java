package com.activestandards;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.engine.SlingRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.activestandards.rest.Client;
import com.activestandards.rest.Result;
import com.activestandards.service.QuickCheckService;
import com.day.cq.contentsync.handler.util.RequestResponseFactory;
import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.wcm.webservicesupport.Configuration;
import com.day.cq.wcm.webservicesupport.ConfigurationManager;

@SlingServlet( paths={
		"/services/as/quickcheck/assetError",
		"/services/as/quickcheck/getContent",
		"/services/as/quickcheck/deleteAsset"
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
    protected ResourceResolverFactory resourceResolverFactory;
	
	@Reference
	protected SlingRequestProcessor requestProcessor;
	
	@Reference
	protected QuickCheckService quickCheckService;
	
	private static final long serialVersionUID = 3351665482064440449L;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final String serviceIdentifier = "activestandards";
	private String apiKey = null;
	private String proxyHost = null;
	private String proxyPort = null;
    private String assetErrorUrl = "http://api.activestandards.com/v1/assets/%s/errors/%s?highlightSource=%s&apiKey=%s";
	
    private void getApiKey(InheritanceValueMap pageProperties) {
		String[] services = (String[]) pageProperties.getInherited("cq:cloudserviceconfigs", String[].class);
		Configuration cfg = cfgMgr.getConfiguration(serviceIdentifier, services);
		
		if (cfg != null) {
			cfg = cfg.getParent().adaptTo(Configuration.class);
			
			if (cfg != null) {
				this.apiKey = cfg.get("apikey", null);
				log.debug("API Key from CSConfig " + apiKey);
				this.proxyHost = cfg.get("proxyhost", null);
				log.debug("Proxy host from CSConfig " + proxyHost);
				this.proxyPort = cfg.get("proxyport", null);
				log.debug("Proxy port from CSConfig " + proxyPort);
			}
		}
	}
    
	@Override
	public void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
		try
        {
            String uri = request.getRequestURI();
            String referer = quickCheckService.getResourcePathFromUrl(request.getHeader("referer"));
            
            log.info("doGet referrer: " + referer);
            
            ResourceResolver resolver = request.getResourceResolver();
            Resource currentResource = resolver.getResource(referer);
            InheritanceValueMap pageProperties = new HierarchyNodeInheritanceValueMap(currentResource);
            
            getApiKey(pageProperties);

            if ("/services/as/quickcheck/assetError".equals(uri)) {
				String assetId = request.getParameter("assetId");
                String checkpointId = request.getParameter("checkpointId");
                String highlightSource = request.getParameter("highlightSource");

                Client client = new Client(proxyHost, proxyPort);
                Result result = client.doGetCall(String.format(assetErrorUrl, assetId, checkpointId, highlightSource, this.apiKey));
                response.setContentType("text/html");
                PrintWriter out = response.getWriter();
                out.write(new String(result.responseBody));
            }
            
            if ("/services/as/quickcheck/getContent".equals(uri)) {
            	String path = referer + ".html";
            	log.debug("doGet : getContent path : " + path);
            	
        		PrintWriter out = response.getWriter();
            	String pageContent = quickCheckService.getContent(path, resolver);
            	
        		log.debug("doGetCall : getContent : Returning response");
        		response.setContentType("text/html");
        		out.write(pageContent);
            }
            
            if ("/services/as/quickcheck/deleteAsset".equals(uri)) {
            	quickCheckService.deleteAsset(referer + ".html", pageProperties);
            }
        } catch (Exception e) {
        	log.error("Error", e);
        }
	}

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

    }
}