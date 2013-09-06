<%--

  ActiveStandards Cloud Service Framework component.

  

--%><%@page session="false"
			contentType="text/html"
            pageEncoding="utf-8"
            import="com.day.cq.i18n.I18n"%>
<%@include file="/libs/foundation/global.jsp"%>
<%@include file="/libs/cq/cloudserviceconfigs/components/configpage/init.jsp"%>
<% 
	I18n i18n = new I18n(request);
	String resPath = resource.getPath().replace("/jcr:content", "");
%>
<div>
    <h3>ActiveStandards QuickCheck Website Settings</h3>   
    <img src="<%=thumbnailPath%>" alt="<%=serviceName%>" style="float: left;" />
    <ul style="float: left; margin: 0px;">
        <li><div class="li-bullet"><strong>Website ID: </strong><%= xssAPI.encodeForHTML(properties.get("websiteid", "")) %></div></li>
    </ul>
</div>