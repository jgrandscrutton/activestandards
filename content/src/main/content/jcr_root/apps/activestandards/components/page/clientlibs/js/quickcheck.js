ShowCheckpoint = function(assetId, checkpointId, canHighlightPage, canHighlightSource) {
    console.debug(assetId, checkpointId, canHighlightPage, canHighlightSource);
	$("#content").empty();
    if (canHighlightPage) {
        LoadPage(assetId, checkpointId, canHighlightSource);
    } else if (canHighlightSource) {
		LoadSource(assetId, checkpointId);
    } else {
    	console.debug("Unable to highlight error");
    	ReadPage();
    }
}

if (!String.prototype.format) {
    String.prototype.format = function() {
        var args = arguments;
        return this.replace(/{(\d+)}/g, function(match, number) { 
            return typeof args[number] != 'undefined' ? args[number] : match;
        });
    };
}

GetApiKey = function() {
    return "zzks8jhtgxxqma9x928hupck";
}

GetWebsite = function(apiKey) {
    var getWebsitesUrl = "http://api.activestandards.com/v1/websites?apiKey={0}";
    
    $.ajax({
        url: getWebsitesUrl.format(apiKey),
        success: function(result) {
            FindAsset(result[0].id, apiKey);
        }
    });
}

FindAsset = function(websiteId, apiKey) {
    var findAssetUrl = "http://api.activestandards.com/v1/assets?websiteId={0}&url={1}&apiKey={2}";
    
    var pageUrl = GetPageUrl();
    
    $.ajax({
        url: findAssetUrl.format(websiteId, pageUrl, apiKey),
        success: function(result) {
            if (result.total > 0) {
                GetPageContent(websiteId, pageUrl, result.assets[0].id, "update", apiKey);
                //UpdateAsset(result.assets[0].id, apiKey);
            } else {
                GetPageContent(websiteId, pageUrl, null, "create", apiKey);
                //CreateAsset(websiteId, pageUrl, apiKey);
            }
        }
    });
}

CreateAsset = function(websiteId, pageUrl, data, apiKey) {
    var createAssetUrl = "http://api.activestandards.com/v1/assets?apiKey={0}";
    var requestData = "websiteId={0}&url={1}&contentType=text%2Fhtml&content={2}";

    $.ajax({
        contentType: "text/html",
        data: requestData.format(websiteId, pageUrl, data),
        type: "POST",
        url: createAssetUrl.format(apiKey),
        success: function(result) {
            CheckAsset(result.id, apiKey);
        }
    });
}

UpdateAsset = function(assetId, data, apiKey) {
    var updateAssetUrl = "http://api.activestandards.com/v1/assets/{0}?apiKey={1}";
    var requestData = "content={0}&param=param";
    
    $.ajax({
        data: requestData.format(data),
        type: "PUT",
        url: updateAssetUrl.format(assetId, apiKey),
        success: function(result) {
            CheckAsset(result.id, apiKey);
        }
    });
}

CheckAsset = function(assetId, apiKey) {
    var checkAssetUrl = "http://api.activestandards.com/v1/assets/{0}/status?apiKey={1}";
    
    $.ajax({
        url: checkAssetUrl.format(assetId, apiKey),
        success: function(result) {
            ListResults(result);
        }
    });
}

GetPageUrl = function() {
    var pageUrl = window.location.href;
    pageUrl = pageUrl.replace("/cf#", "");
    pageUrl = pageUrl.replace(".quickcheck", "");
    return pageUrl;
}

GetPageContent = function(websiteId, pageUrl, assetId, type, apiKey) {
    $.get(pageUrl, function(data) {
        switch (type) {
            case "create":
				CreateAsset(websiteId, pageUrl, data, apiKey);
            case "update":
                UpdateAsset(assetId, data, apiKey);
        }
    });
}

GetFailedCheckpoints = function(statusReport) {
    if (statusReport.checkpoints) {
        return jQuery.grep(statusReport.checkpoints, function(checkpoint) {
            return checkpoint.failed;
        });
    }
    return [];
};

ListResults = function(assetStatus) {
    var errorItemHtml = "<li><a href=\"javascript:ShowCheckpoint('{0}', '{1}', {2}, {3})\">{4} {5}</a></li>";
    console.debug(assetStatus);
    var failedCheckpoints = GetFailedCheckpoints(assetStatus);
    var errorHeader = $("<h2>" + failedCheckpoints.length + " errors found.</h2>");
    var errorList = $("<ul/>");

    for (i = 0; i < failedCheckpoints.length; i++) {
        var failedCheckpoint = failedCheckpoints[i]
        console.debug(failedCheckpoint);
        var errorItem = $(errorItemHtml.format(assetStatus.assetId, failedCheckpoint.id, failedCheckpoint.canHighlight.page, failedCheckpoint.canHighlight.source, failedCheckpoint.reference, failedCheckpoint.name));
        $(errorList).append(errorItem);
    }

    $("#left").append(errorHeader, errorList);
}

LoadPage = function(assetId, checkpointId, canHighlightSource) {
    var assetErrorUrl = "http://api.activestandards.com/v1/assets/{0}/errors/{1}?highlightSource={2}&apiKey={3}";

    $.ajax({
        url: assetErrorUrl.format(assetId, checkpointId, false, GetApiKey()),
        success: function(result) {
            console.debug(result);
            $("#content").append($(result));
        }
    });

    if (canHighlightSource) {
		$.ajax({
        	url: assetErrorUrl.format(assetId, checkpointId, true, GetApiKey()),
        	success: function(result) {
            	console.debug(result);
        	}
    	});
    }
}

LoadSource = function(assetId, checkpointId) {
	var assetErrorUrl = "http://api.activestandards.com/v1/assets/{0}/errors/{1}?highlightSource=true&apiKey={2}";

    $.ajax({
        url: assetErrorUrl.format(assetId, checkpointId, GetApiKey()),
        success: function(result) {
            console.debug(result);
            $("#content").append($(result));
        }
    });
}

ReadPage = function() {
	var getContentUrl = "/services/as/quickcheck/getContent?path={0}";
	var path = window.location.pathname;
	path = path.replace("/cf#", "");
	path = path.replace(".quickcheck", "");
	
	$.ajax({
		url: getContentUrl.format(path),
		success: function(result) {
			$("content").append($(result));
		}
	});
}

//$(document).ready(GetWebsite(GetApiKey()));