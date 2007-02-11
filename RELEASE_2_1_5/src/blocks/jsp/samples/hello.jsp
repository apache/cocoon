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



