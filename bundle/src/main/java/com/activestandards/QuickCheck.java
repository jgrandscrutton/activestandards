package com.activestandards;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.engine.SlingRequestProcessor;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.activestandards.rest.Client;
import com.activestandards.rest.Result;

import com.day.cq.contentsync.handler.util.RequestResponseFactory;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.wcm.webservicesupport.Configuration;
import com.day.cq.wcm.webservicesupport.ConfigurationManager;

@Component (immediate=true, metatype=false)
public class QuickCheck {
	@Reference ConfigurationManager cfgMgr;
	@Reference
	private SlingRequestProcessor slingRequestProcessor;
	
	@Reference
    private ResourceResolverFactory resourceResolverFactory;
	
	@Reference
    private RequestResponseFactory requestResponseFactory;
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final String serviceIdentifier = "activestandards";
	//private final String API_KEY = "your_api_key_here"; // replace with ActiveStandards API Key
	private String serverAddress = "http://%s:%s";
	private String pageUrl = null;
	private HttpServletRequest request;
	private String apiKey = null;
	private String websiteId = null;
	private String assetId = null;
	private String findAssetUrl = "http://api.activestandards.com/v1/assets?websiteId=%s&url=%s&apiKey=%s";
	private String createAssetUrl = "http://api.activestandards.com/v1/assets?apiKey=%s";
	private String updateAssetUrl = "http://api.activestandards.com/v1/assets/%s?apiKey=%s";
	private String updateRequestData = "content=%s&param=param";
	private String checkAssetUrl = "http://api.activestandards.com/v1/assets/%s/status?apiKey=%s";
	
	public QuickCheck() {
		//this.apiKey = getApiKey();
	}
	
	public QuickCheck(HttpServletRequest request, InheritanceValueMap pageProperties) {
		BundleContext bundleContext = FrameworkUtil.getBundle(QuickCheck.class).getBundleContext();
		ServiceReference serviceRef = bundleContext.getServiceReference(ConfigurationManager.class.getName());
		this.cfgMgr = (ConfigurationManager) bundleContext.getService(serviceRef);
		
		serviceRef = bundleContext.getServiceReference(RequestResponseFactory.class.getName());
		this.requestResponseFactory = (RequestResponseFactory) bundleContext.getService(serviceRef);
		
		serviceRef = bundleContext.getServiceReference(ResourceResolverFactory.class.getName());
		this.resourceResolverFactory = (ResourceResolverFactory) bundleContext.getService(serviceRef);
		
		serviceRef = bundleContext.getServiceReference(SlingRequestProcessor.class.getName());
		this.slingRequestProcessor = (SlingRequestProcessor) bundleContext.getService(serviceRef);
		
		this.request = request;
		
		this.serverAddress = String.format(this.serverAddress, request.getServerName(), request.getServerPort());
		this.pageUrl = request.getRequestURI().toString();
		this.pageUrl = this.pageUrl.replace("/cf#", "");
		this.pageUrl = this.pageUrl.replace(".quickcheck", "");
		getWebsiteIdAndApiKey(pageProperties);
	}
	
	private void getWebsiteIdAndApiKey(InheritanceValueMap pageProperties) {
		String[] services = (String[]) pageProperties.getInherited("cq:cloudserviceconfigs", String[].class);
		Configuration cfg = cfgMgr.getConfiguration(serviceIdentifier, services);
		
		if (cfg != null) {
			this.websiteId = (String) cfg.get("websiteid", null);
			log.info("WebsiteID from CSConfig: " + websiteId);
			
			cfg = cfg.getParent().adaptTo(Configuration.class);
			
			if (cfg != null) {
				this.apiKey = cfg.get("apikey", null);
				log.info("API Key from CSConfig " + apiKey);
			}
		}
	}
	
	private int findAsset(String websiteId, String pageUrl, String apiKey) {
		int assetCount = 0;
		
		try {
			Client client = new Client();
			Result result = client.doGetCall(String.format(findAssetUrl, websiteId, this.serverAddress + pageUrl, apiKey));
			
			if (result.responseBody != null) {
				JSONObject jo = new JSONObject(new String(result.responseBody));
				assetCount = jo.getInt("total");
				
				if (assetCount > 0) {
					this.assetId = jo.getJSONArray("assets").getJSONObject(0).getString("id");
				}
			}
		} catch (Exception e) {
			log.error("findAsset error ", e);
		}
		
		return assetCount;
	}
	
	private String createAsset(String websiteId, String pageUrl, String pageContent, String apiKey) {
		String assetId = null;
		
		try {
			HashMap<String,String> headerParams = new HashMap<String,String>();
			headerParams.put("Content-Type", "application/x-www-form-urlencoded");
			
			HashMap<String,String> bodyParts = new HashMap<String,String>();
			bodyParts.put("websiteId", websiteId);
			bodyParts.put("url", this.serverAddress + pageUrl);
			bodyParts.put("contentType", "text/html");
			bodyParts.put("content", pageContent);
			
			Client client = new Client();
			Result result = client.doPostCall(String.format(createAssetUrl, apiKey), bodyParts);
			
			if (result.responseBody != null) {
				JSONObject jo = new JSONObject(new String(result.responseBody));
				assetId = jo.getString("id");
			}
		} catch (Exception e) {
			log.error("createAsset error ", e);
		}
		
		return assetId;
	}
	
	private String updateAsset(String assetId, String pageContent, String apiKey) {
		String requestData = String.format(updateRequestData, pageContent);
		
		try {
			Client client = new Client();
			Result result = client.doPutCall(String.format(updateAssetUrl, assetId, apiKey), requestData);
			
			if (result.responseBody != null) {
				JSONObject jo = new JSONObject(new String(result.responseBody));
				assetId = jo.getString("id");
			} else {
				assetId = null;
			}
		} catch (Exception e) {
			log.error("updateAsset error ", e);
		}
		
		return assetId;
	}
	
	private JSONObject checkAsset(String assetId, String apiKey) {
		JSONObject jo = null;
		
		try {
			Client client = new Client();
			Result result = client.doGetCall(String.format(checkAssetUrl, assetId, apiKey));
			
			if (result.responseBody != null) {
				jo = new JSONObject(new String(result.responseBody));
			}
		} catch (Exception e) {
			log.error("checkAsset error ", e);
		}
		
		return jo;
	}
	
	private String getContent(String pageUrl) {
		String pageContent = null;
		log.info("getContent : pageUrl : " + pageUrl);
		
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			HttpServletRequest mockRequest = requestResponseFactory.createRequest("GET", pageUrl);
			HttpServletResponse mockResponse = requestResponseFactory.createResponse(outputStream);
			slingRequestProcessor.processRequest(mockRequest, mockResponse, createResolver(this.request.getUserPrincipal().getName()));
			
			pageContent = outputStream.toString();
		} catch (Exception e) {
			log.error("getContent error ", e);
		}
		
		return pageContent;
	}
	
	private JSONArray getFailedCheckpoints(JSONObject jo) {
		JSONArray ja = new JSONArray();
		
		try {
			JSONArray checkpoints = jo.getJSONArray("checkpoints");
			
			for (int i = 0; i < checkpoints.length(); i++) {
				JSONObject checkpoint = checkpoints.getJSONObject(i);
				
				if (Boolean.parseBoolean(checkpoint.getString("failed"))) {
					ja.put(checkpoint);
				}
			}
		} catch (Exception e) {
			log.error("getFailedCheckpoints error", e);
		}
		return ja;
	}
	
	public JSONArray doAssetCheck() {
		JSONArray ja = new JSONArray();
		
		if (this.apiKey != null && this.websiteId != null && this.pageUrl != null) {
			int assetCount = findAsset(this.websiteId, this.pageUrl, this.apiKey);
			//log.info(Integer.toString(assetCount));
			String pageContent = getContent(this.pageUrl);
			
			if (assetCount > 0 && this.assetId != null && pageContent != null) {
				log.info("doAssetCheck : update");
				this.assetId = updateAsset(this.assetId, pageContent, this.apiKey);
			} else if (assetCount == 0 && pageContent != null) {
				log.info("doAssetCheck : create");
				this.assetId = createAsset(this.websiteId, this.pageUrl, pageContent, this.apiKey);
			}
			
			log.info("doAssetCheck : assetId " + this.assetId);
			if (this.assetId != null) {
				JSONObject jo = checkAsset(this.assetId, this.apiKey);
				
				if (jo != null) {
					ja = getFailedCheckpoints(jo);
					log.info("doAssetCheck : failedCheckpoints " + ja);
				}
			}
		}
		
		return ja;
	}
	
	/*public String getApiKey() {
		return this.API_KEY;
	}*/
	
	public String getAssetId() {
		return this.assetId;
	}
	
	private ResourceResolver createResolver(String userId) throws RepositoryException, LoginException {
		HashMap<String, Object> authInfo = new HashMap<String, Object>();
		authInfo.put(ResourceResolverFactory.USER_IMPERSONATION, userId);
		
        return resourceResolverFactory.getAdministrativeResourceResolver(authInfo);
		
	}
}
