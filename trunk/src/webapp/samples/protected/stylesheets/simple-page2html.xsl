<?xml version="1.0"?>
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

<!-- CVS: $Id: simple-page2html.xsl,v 1.3 2004/03/10 10:27:55 cziegeler Exp $ -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="context://samples/common/style/xsl/html/simple-page2html.xsl"/>
 
  <xsl:template match="linkbar">
    <div>
      [
      <a href="login"> login </a>
      |
      <a href="page"> protected </a>
      |
      <a href="do-logout"> logout </a>
      ]
    </div>
  </xsl:template>

</xsl:stylesheet>
<!-- vim: set et ts=2 sw=2: -->
