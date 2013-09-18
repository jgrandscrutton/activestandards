ShowCheckpoint = function(assetId, checkpointId, canHighlightPage, canHighlightSource) {
    console.debug(assetId, checkpointId, canHighlightPage, canHighlightSource);
	//$("#content").empty();
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

LoadPage = function(assetId, checkpointId, canHighlightSource) {
	var assetErrorUrl = "/services/as/quickcheck/assetError?assetId={0}&checkpointId={1}&highlightSource={2}";

    $.ajax({
        url: assetErrorUrl.format(assetId, checkpointId, false),
        success: function(result) {
            console.debug(result);
            //$("#content").append($(result));
			window.frames[0].document.documentElement.innerHTML = result;
        }
    });

    if (canHighlightSource) {
		$.ajax({
        	url: assetErrorUrl.format(assetId, checkpointId, true),
        	success: function(result) {
            	console.debug(result);
        	}
    	});
    }
}

LoadSource = function(assetId, checkpointId) {
	var assetErrorUrl = "/services/as/quickcheck/assetError?assetId={0}&checkpointId={1}&highlightSource=true";

    $.ajax({
        url: assetErrorUrl.format(assetId, checkpointId),
        success: function(result) {
            console.debug(result);
            //$("#content").append($(result));
			window.frames[0].document.documentElement.innerHTML = result;
        }
    });
}

ReadPage = function() {
	var getContentUrl = "/services/as/quickcheck/getContent";
	
	$.ajax({
		url: getContentUrl,
		success: function(result) {
			console.debug(result);
			//$("content").append($(result));
			window.frames[0].document.documentElement.innerHTML = result;
		}
	});
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