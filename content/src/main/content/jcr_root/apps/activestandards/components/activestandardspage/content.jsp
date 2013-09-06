<%--

  ActiveStandards Cloud Service Config component.

  

--%><%@page session="false"
			contentType="text/html"
            pageEncoding="utf-8"
            import="com.day.cq.i18n.I18n"%>
<%@include file="/libs/foundation/global.jsp"%>
<%@include file="/libs/cq/cloudserviceconfigs/components/configpage/init.jsp"%>
<% 
	I18n i18n = new I18n(request);
	String resPath = resource.getPath().replace("/jcr:content", "");
	String createLink = "<a href=\"javascript: CQ.cloudservices.editNewConfiguration('"+ resPath + "','"+ resPath +"', false, '"+ i18n.get("Create Framework") +"')\">" +
						i18n.get("create") + "</a>";
%>
<div>
    <h3>ActiveStandards Settings</h3>   
    <img src="<%=thumbnailPath%>" alt="<%=serviceName%>" style="float: left;" />
    <ul style="float: left; margin: 0px;">
        <li><div class="li-bullet"><strong>API Key: </strong><%= xssAPI.encodeForHTML(properties.get("apikey", "")) %></div></li>
        <li class="config-successful-message when-config-successful" style="display: none">
        	ActiveStandards configuration is successful.<br>
			Please <%=createLink %> or edit an QuickCheck framework, and apply it to your <a href=\"/siteadmin\">website</a>.
        </li>
    </ul>
    <div class="when-config-successful" style="display: none">
        <h2 style="border: none; margin-top: 10px; padding-left:0px;"><%= i18n.get("Available Frameworks") %>
        [<a href="javascript: CQ.cloudservices.editNewConfiguration('<%=resPath%>','<%=resPath%>', false, '<%= i18n.get("Create Framework") %>')" 
            style="color: #336600;" title="<%= i18n.get("Create Framework") %>"><b>+</b></a>]
        </h2>
        <%=printChildren(i18n, currentPage, request)%>
    </div>
</div>