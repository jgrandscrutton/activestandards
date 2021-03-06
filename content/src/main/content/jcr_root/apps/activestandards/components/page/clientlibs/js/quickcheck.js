var assetErrorUrl = "/services/as/quickcheck/assetError?assetId={0}&checkpointId={1}&highlightSource={2}";
var getContentUrl = "/services/as/quickcheck/getContent";
var toggleLinkText = "Click here to show {0} view";
var toggleLinkUrl = "javascript:ToggleDisplay('{0}')";

ShowCheckpoint = function(assetId, checkpointId, canHighlightPage, canHighlightSource, currentCheckpoint) {
    console.debug(assetId, checkpointId, canHighlightPage, canHighlightSource, currentCheckpoint.parentElement);

    if (canHighlightPage) {
        LoadPage(assetId, checkpointId, canHighlightSource);
    } else if (canHighlightSource) {
		LoadSource(assetId, checkpointId);
    } else {
    	console.debug("Unable to highlight error");
    	ReadPage();
    }
    
    $("#left li div").addClass("hidden");
    $("#" + checkpointId + " div").removeClass("hidden");
}

if (!String.prototype.format) {
    String.prototype.format = function() {
        var args = arguments;
        return this.replace(/{(\d+)}/g, function(match, number) { 
            return typeof args[number] != 'undefined' ? args[number] : match;
        });
    };
}

LoadPage = function(assetId, checkpointId, canHighlightSource) {
	var contentFrame = $("#contentframe")[0];
	contentFrame.src = assetErrorUrl.format(assetId, checkpointId, false);

	var toggleLink = $("#toggleLink");
	toggleLink.attr("href", toggleLinkUrl.format(assetErrorUrl.format(assetId, checkpointId, true)));
	toggleLink.text(toggleLinkText.format("Source"));
	toggleLink.removeAttr("class");
	
    /*if (canHighlightSource) {
		$.ajax({
        	url: assetErrorUrl.format(assetId, checkpointId, true),
        	success: function(result) {
            	console.debug(result);
        	}
    	});
    }*/
}

LoadSource = function(assetId, checkpointId) {
	$("#toggleLink").attr("class", "hidden");
	$("#contentframe")[0].src = assetErrorUrl.format(assetId, checkpointId, true);
}

ReadPage = function() {
	$("#toggleLink").attr("class", "hidden");
	$("#contentframe")[0].src = window.location.href.replace(".quickcheck", "") + "?wcmmode=disabled"; //getContentUrl;
}

ToggleDisplay = function(url) {
	var contentFrame = $("#contentframe")[0];
	var toggleLink = $("#toggleLink");
	
	toggleLink.attr("href", toggleLinkUrl.format(contentFrame.src));
	toggleLink.text(toggleLinkText.format("Live"));
	contentFrame.src = url;
}

var windowClosing = true;

$(window).bind(
		"beforeunload",
		function(e) {
			var deleteAssetUrl = "/services/as/quickcheck/deleteAsset";
			
			if (windowClosing)
				// TODO delete asset
				$.ajax({
					url: deleteAssetUrl,
					success: function(result) {	}
				});
		}
);

/*$(document).keydown(
	function(e) {
        var keycode;
        if (window.event)
            keycode = window.event.keyCode;
        else if (e)
            keycode = e.which;

        // Mozilla firefox
        if ($.browser.mozilla) {
        	if (keycode == 116 ||(e.ctrlKey && keycode == 82) ||(e.metaKey && keycode == 82)) {
                windowClosing = false;
            }
        } 
        // IE
        else if ($.browser.msie) {
            if (keycode == 116 || (window.event.ctrlKey && keycode == 82)) {
                windowClosing = false;
            }
        }
    }
);*/