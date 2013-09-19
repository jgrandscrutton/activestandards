<%@include file="/libs/foundation/global.jsp"%><%@
page import="com.activestandards.QuickCheckResult,com.activestandards.service.QuickCheckService,org.apache.sling.commons.json.JSONArray,org.apache.sling.commons.json.JSONObject;"%><%
    QuickCheckService qcs = sling.getService(QuickCheckService.class);
	QuickCheckResult result = qcs.doAssetCheck(request, pageProperties);
	int failedCheckpointsCount = result.getFailedCheckpointsCount();
%><body>
    <div id="container">
        <h1>ActiveStandards QuickCheck</h1>
        <div id="left">
        	<h2><%=failedCheckpointsCount %> error<% if (failedCheckpointsCount != 1) { %>s<% } %> found.</h2>
<%
	if (result.getFailedCheckpointsCount() > 0) {
		JSONArray failedCheckpoints = result.getFailedCheckpoints();
%>			<ul>
<%
		for (int i = 0; i < failedCheckpointsCount; i++) {
			JSONObject checkpoint = failedCheckpoints.getJSONObject(i);
			JSONObject canHighlight = checkpoint.getJSONObject("canHighlight");
%>				<li><a href="javascript:ShowCheckpoint('<%=result.getAssetId() %>', '<%=checkpoint.getString("id") %>', <%=canHighlight.getString("page") %>, <%=canHighlight.getString("source") %>)"><%=checkpoint.getString("reference") %> <%=checkpoint.getString("name").replace("<", "&lt;") %></a></li>
<%
		}
%>			</ul>
<%
	}
%>        </div>
        <div id="right">
            <div id="header">
<<<<<<< HEAD
                <!-- Source/in-context highlighting toggle will go here -->&nbsp;
=======
                <a id="toggleLink" class="hidden"></a>
>>>>>>> jgs-working
            </div>
            <div id="content">
                <iframe id="contentframe" style="width:100%;height:100%" src="about:blank"></iframe>
            </div>
        </div>
        <div class="clearfix"></div>
    </div>
    <cq:includeClientLib js="apps.activestandards.quickcheck"/>
</body>