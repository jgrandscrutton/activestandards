package com.activestandards.service;

import javax.servlet.http.HttpServletRequest;

import com.activestandards.QuickCheckResult;
import com.day.cq.commons.inherit.InheritanceValueMap;

import org.apache.sling.api.resource.ResourceResolver;

public interface QuickCheckService {
	public QuickCheckResult doAssetCheck(HttpServletRequest request, InheritanceValueMap pageProperties);
	
	public String getAssetId();
	
	public String getContent(String pageUrl, ResourceResolver resolver);
	
	public void deleteAsset(String pageUrl, InheritanceValueMap pageProperties);
	
	public String getResourcePathFromUrl(String pageUrl);
}
