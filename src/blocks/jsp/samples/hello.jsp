<%@ page language='java' session='false' %>
<page>
  <title>Hello</title>
  <content>
    <para>This is my first Cocoon2 page!</para>
<%    
out.println("\t<para>With help from JSP on " + new java.util.Date() + "</para>");
%>
  </content>
</page>



