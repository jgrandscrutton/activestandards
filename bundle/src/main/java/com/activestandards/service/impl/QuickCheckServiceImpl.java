package com.activestandards.service.impl;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.engine.SlingRequestProcessor;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.activestandards.QuickCheckResult;
import com.activestandards.rest.Client;
import com.activestandards.rest.Result;
import com.activestandards.service.QuickCheckService;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.contentsync.handler.util.RequestResponseFactory;
import com.day.cq.wcm.webservicesupport.Configuration;
import com.day.cq.wcm.webservicesupport.ConfigurationManager;

@Component(
		label = "ActiveStandards QuickCheck Service",
		description = "Service the validates a page against ActiveStandards' checkpoints",
		metatype = true,
		immediate = true)
@Service(QuickCheckService.class)
public class QuickCheckServiceImpl implements QuickCheckService {
	
	
	/**
	* OSGi Properties *
	*/
	private static final boolean DEFAULT_ENABLED = false;
	private boolean enabled = DEFAULT_ENABLED;
	@Property(label = "Service Enable/Disable", description = "Enables/Disables the service without nullifying service reference objects. This enable/disabling must be implemented in all public methods of this service.", boolValue = DEFAULT_ENABLED)
	public static final String PROP_ENABLED = "prop.enabled";
	
	/* OSGi Service References */
	@Reference
	protected ConfigurationManager cfgMgr;
	
	@Reference
	protected SlingRequestProcessor slingRequestProcessor;
	
	@Reference
    protected ResourceResolverFactory resourceResolverFactory;
	
	@Reference
    protected RequestResponseFactory requestResponseFactory;
	
	/**
	 * default logger
	 */
	private static final Logger log = LoggerFactory.getLogger(QuickCheckServiceImpl.class);
	
	/* Class variables */
	private final String serviceIdentifier = "activestandards";
	private final String serverAddressFormat = "http://%s:%s";
	private String serverAddress = null;
	private String pageUrl = null;
	private HttpServletRequest request;
	private String proxyHost = null;
	private String proxyPort = null;
	private String apiKey = null;
	private String websiteId = null;
	private String assetId = null;
	private static final String findAssetUrl = "http://api.activestandards.com/v1/assets?websiteId=%s&url=%s&apiKey=%s";
	private static final String createAssetUrl = "http://api.activestandards.com/v1/assets?apiKey=%s";
	private static final String updateAssetUrl = "http://api.activestandards.com/v1/assets/%s?apiKey=%s";
	private static final String updateRequestData = "content=%s&param=param";
	private static final String checkAssetUrl = "http://api.activestandards.com/v1/assets/%s/status?apiKey=%s";
	private static final String deleteAssetUrl = "http://api.activestandards.com/v1/assets/%s?apiKey=%s";
	
	public QuickCheckResult doAssetCheck(HttpServletRequest request, InheritanceValueMap pageProperties) {
		init(request, pageProperties);
		getConfig(pageProperties);
		
		QuickCheckResult result = new QuickCheckResult();
		
		if (this.apiKey != null && this.websiteId != null && this.pageUrl != null) {
			int assetCount = findAsset(this.websiteId, this.pageUrl, this.apiKey);
			
			String pageContent = getContent(this.pageUrl);
			
			if (assetCount > 0 && this.assetId != null && pageContent != null) {
				log.debug("doAssetCheck : update");
				result.setAssetId(updateAsset(this.assetId, pageContent, this.apiKey));
			} else if (assetCount == 0 && pageContent != null) {
				log.debug("doAssetCheck : create");
				result.setAssetId(createAsset(this.websiteId, this.pageUrl, pageContent, this.apiKey));
			}
			
			log.debug("doAssetCheck : assetId " + this.assetId);
			if (result.getAssetId() != null) {
				JSONObject jo = checkAsset(result.getAssetId(), this.apiKey);
				
				if (jo != null) {
					result.setFailedCheckpoints(getFailedCheckpoints(jo));
					log.debug("doAssetCheck : failedCheckpoints " + result.getFailedCheckpoints());
				}
			}
		}
		
		return result;
	}
	
	public String getAssetId() {
		return this.assetId;
	}
	
	private void init(HttpServletRequest request, InheritanceValueMap pageProperties) {
		this.request = request;
		this.serverAddress = String.format(this.serverAddressFormat, request.getServerName(), request.getServerPort());
		this.pageUrl = request.getRequestURI().toString();
		this.pageUrl = this.pageUrl.replace("/cf#", "");
		this.pageUrl = this.pageUrl.replace(".quickcheck", "");
	}
	
	private void getConfig(InheritanceValueMap pageProperties) {
		String[] services = (String[]) pageProperties.getInherited("cq:cloudserviceconfigs", String[].class);
		Configuration cfg = cfgMgr.getConfiguration(serviceIdentifier, services);
		
		if (cfg != null) {
			this.websiteId = (String) cfg.get("websiteid", null);
			log.info("WebsiteID from CSConfig: " + websiteId);
			
			cfg = cfg.getParent().adaptTo(Configuration.class);
			
			if (cfg != null) {
				this.apiKey = cfg.get("apikey", null);
				log.info("API Key from CSConfig " + apiKey);
				this.proxyHost = cfg.get("proxyhost", null);
				log.info("Proxy host from CSConfig " + proxyHost);
				this.proxyPort = cfg.get("proxyport", null);
				log.info("Proxy port from CSConfig " + proxyPort);
			}
		}
	}
	
	private int findAsset(String websiteId, String pageUrl, String apiKey) {
		int assetCount = 0;
		
		try {
			Client client = new Client(this.proxyHost, this.proxyPort);
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
			
			Client client = new Client(this.proxyHost, this.proxyPort);
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
			Client client = new Client(this.proxyHost, this.proxyPort);
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
			Client client = new Client(this.proxyHost, this.proxyPort);
			Result result = client.doGetCall(String.format(checkAssetUrl, assetId, apiKey));
			
			if (result.responseBody != null) {
				jo = new JSONObject(new String(result.responseBody));
			}
		} catch (Exception e) {
			log.error("checkAsset error ", e);
		}
		
		return jo;
	}
	
	public void deleteAsset(String pageUrl, InheritanceValueMap pageProperties) {
		getConfig(pageProperties);
		
		if (this.apiKey != null && this.websiteId != null && pageUrl != null) {
			int assetCount = findAsset(this.websiteId, pageUrl, this.apiKey);
			
			if (assetCount > 0) {
				try {
					Client client = new Client(this.proxyHost, this.proxyPort);
					Result result = client.doDeleteCall(String.format(deleteAssetUrl, this.assetId, this.apiKey));
					log.debug("deleteAsset status " + result.resultCode);
				} catch (Exception e) {
					log.error("deleteAsset error ", e);
				}
			}
		}
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
	
	public String getContent(String pageUrl, ResourceResolver resolver) {
		String pageContent = null;
		log.debug("getContent : pageUrl : " + pageUrl);
		
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			HttpServletRequest mockRequest = requestResponseFactory.createRequest("GET", pageUrl);
			HttpServletResponse mockResponse = requestResponseFactory.createResponse(outputStream);
			slingRequestProcessor.processRequest(mockRequest, mockResponse, resolver);
			
			pageContent = outputStream.toString();
		} catch (Exception e) {
			log.error("getContent error ", e);
		}
		
		return pageContent;
	}
	
	private String getContent(String pageUrl) {
		String pageContent = null;
		log.info("getContent : pageUrl : " + pageUrl);
		
		try {
			ResourceResolver resolver = createResolver(this.request.getUserPrincipal().getName());
			pageContent = getContent(pageUrl, resolver);
		} catch (Exception e) {
			log.error("getContent error ", e);
		}
		
		return pageContent;
	}
	
	private ResourceResolver createResolver(String userId) throws RepositoryException, LoginException {
		HashMap<String, Object> authInfo = new HashMap<String, Object>();
		authInfo.put(ResourceResolverFactory.USER_IMPERSONATION, userId);
		
        return resourceResolverFactory.getAdministrativeResourceResolver(authInfo);
		
	}
	
	public String getResourcePathFromUrl(String pageUrl) {
		pageUrl = pageUrl.substring(pageUrl.indexOf("/", pageUrl.indexOf("//") + 2));
		pageUrl = pageUrl.replace("/cf#", "");
		pageUrl = pageUrl.replace(".quickcheck", "");
		pageUrl = pageUrl.replace(".html", "");
        
    	return pageUrl;
	}
	
	/**
	* OSGi Component Methods *
	*/
	@Activate
	protected void activate(final ComponentContext componentContext) throws Exception {
		final Map<String, String> properties = (Map<String, String>) componentContext.getProperties();
		
		configure(properties);
	}
	
	@Deactivate
	protected void deactivate(ComponentContext ctx) {
		this.enabled = false;
	}
	
	protected void configure(final Map<String, String> properties) {
		// Global Service Enabled/Disable Setting
		this.enabled = PropertiesUtil.toBoolean(properties.get(PROP_ENABLED), DEFAULT_ENABLED);
	}

}
