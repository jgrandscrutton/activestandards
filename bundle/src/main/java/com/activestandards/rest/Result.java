package com.activestandards.rest;

import java.util.Map;

public class Result {
	public int resultCode = 0;
	public String contentType = "";
	public Map<String,String> headers;
	public byte[] responseBody;
}
