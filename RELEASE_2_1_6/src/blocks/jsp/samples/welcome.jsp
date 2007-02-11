<!--
  Copyright 1999-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<%@ page import="java.util.*" %>
<%
	response.setHeader("Expires", "0");
%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>JSPreader test</title>
<style type="text/css">
BODY {background-color: #FFFFFF; color: #000066; font-family: Verdana, Helvetica, Arial; }
</style>
</head>

<body>
<h3>This is a dynamic output from the JSPReader</h3>
<h4>Current time: <%=new Date()%></h4>
<hr noshade size="1">
<br>
<table width="75%" border="1" cellspacing="0" cellpadding="5">
  <tr bgcolor="#990000"> 
    <th colspan="2" align="left"><font color="#FFFFFF">JSP Usage Samples</font></th>
  </tr>
  <tr> 
    <td width="25%"><a href="hello.jsp">hello.jsp</a></td>
    <td width="56%">Displays a hello page using JSPGenerator to get XML from a 
      JSP then transformes it to HTML using a stylesheet.</td>
  </tr>
  <tr> 
    <td width="25%"><a href="hello.xml">hello.xml</a></td>
    <td width="56%">The same page serialized as XML (without transformation).</td>
  </tr>
  <tr> 
    <td width="25%"><a href="hello.htm">hello.htm</a></td>
    <td width="56%"> 
      <p>The same page serialized as HTML and served by sitemap through the JSPReader.</p>
    </td>
  </tr>
  <tr> 
    <td width="25%"><a href="welcome.htm">welcome.htm</a></td>
    <td width="56%">This page. Direct HTML output from a JSP page served by sitemap 
      through the JSPReader.</td>
  </tr>
</table>
<p><small><a href=".."><br>
  Back to samples</a></small> </p>
</body>
</html>
