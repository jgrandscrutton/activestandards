<%@include file="/libs/foundation/global.jsp"%><%@
page import="com.activestandards.QuickCheck,org.apache.sling.commons.json.JSONArray,org.apache.sling.commons.json.JSONObject;"%><%
    QuickCheck qc = new QuickCheck(request, response);
	JSONArray ja = qc.doAssetCheck();
%><body>
    <div id="container">
        <h1>ActiveStandards QuickCheck</h1>
        <div id="left">
			<% if (ja.length() > 0) { %>
			<h2><%=ja.length() %> errors found.</h2>
			<ul>
				<%
					for (int i = 0; i < ja.length(); i++) {
						JSONObject checkpoint = ja.getJSONObject(i);
						JSONObject canHighlight = checkpoint.getJSONObject("canHighlight");
				%>
				<li><a href="javascript:ShowCheckpoint('<%=qc.getAssetId() %>', '<%=checkpoint.getString("id") %>', <%=canHighlight.getString("page") %>, <%=canHighlight.getString("source") %>)"><%=checkpoint.getString("reference") %> <%=checkpoint.getString("name") %></a></li>
				<% } %>
			</ul>
			<% } %>
        </div>
        <div id="right">
            <div id="header">
                &nbsp;
            </div>
            <div id="content">
                &nbsp;
            </div>
        </div>
        <div class="clearfix"></div>
    </div>
    <cq:includeClientLib js="apps.activestandards.quickcheck"/>
</body>