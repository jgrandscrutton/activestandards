// Set up a namespace to contain our code

CQ.Ext.namespace("activestandards", "activestandards.button");

// Sidekick action to fire the custom action
activestandards.button.QuickCheck = {
    "text": "ActiveStandards QuickCheck",
    "context": CQ.wcm.Sidekick.PAGE,
    "handler": function() {
        GetPageUrl = function() {
			var pageUrl = window.location.href;
            if (pageUrl.indexOf("/cf#") > 0) {
                pageUrl = pageUrl.replace("/cf#", "");
            }
            return pageUrl;
        }

        OpenResultsWindow = function(failedCheckpoints, pageUrl) {
            var pageUrl = GetPageUrl()
            var urlParts = pageUrl.split(".");
            var length = urlParts.length;
            urlParts[length - 1] = "quickcheck." + urlParts[length - 1];
            pageUrl = urlParts.join(".");

            var resultsWindow = window.open(pageUrl);
        }

        OpenResultsWindow();
    }
};

// add this action to the default list
CQ.wcm.Sidekick.DEFAULT_ACTIONS.push(activestandards.button.QuickCheck);