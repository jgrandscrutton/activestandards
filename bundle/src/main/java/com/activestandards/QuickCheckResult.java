package com.activestandards;

import org.apache.sling.commons.json.JSONArray;

public class QuickCheckResult {
	private int failedCheckpointCount = 0;
	private String assetId = null;
	private JSONArray failedCheckpoints = null;
	
	public QuickCheckResult() {
	}
	
	public QuickCheckResult(String assetId, JSONArray failedCheckpoints) {
		this.assetId = assetId;
		this.failedCheckpoints = failedCheckpoints;
		
		if (failedCheckpoints != null)
			this.failedCheckpointCount = failedCheckpoints.length();
	}
	
	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}
	
	public String getAssetId() {
		return this.assetId;
	}
	
	public void setFailedCheckpoints(JSONArray failedCheckpoints) {
		this.failedCheckpoints = failedCheckpoints;
		
		if (failedCheckpoints != null)
			this.failedCheckpointCount = failedCheckpoints.length();
	}
	
	public JSONArray getFailedCheckpoints() {
		return this.failedCheckpoints;
	}
	
	public int getFailedCheckpointsCount() {
		return this.failedCheckpointCount;
	}
}
