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

<!--
    The code is based on Motorola's VoxML (Version 1.2) implementation of
    the emerging VoiceXML standard (version 0.9 as of 17 August '99).
    Foremost is the absence of <form></form> tags that delineate input. That
    said, when this file is rendered in Motorola's Mobile ADK simulator
    (MADK, version 1 beta 4 this November) the interaction looks like this:

    Computer: Hello world! This is my first voice enabled cocoon page.

    If user says "repeat" then the computer repeats the prompt.
    If the user says "goodbye" then the computer says "Goodbye" and ends the
    session.

    Written by Theodore B. Achacoso, MD 991122 ted@groupserve.com
    CVS $Id: page2vml.xsl,v 1.3 2004/03/10 10:18:52 cziegeler Exp $
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="page">
    <DIALOG>
     <STEP NAME="init">
      <PROMPT><xsl:apply-templates/></PROMPT>
      <INPUT TYPE="OPTIONLIST">
       <OPTION NEXT="#init">repeat</OPTION>
       <OPTION NEXT="#goodbye">goodbye</OPTION>
      </INPUT>
     </STEP>
     <STEP NAME="goodbye">
       <PROMPT>Goodbye</PROMPT>
       <INPUT TYPE="NONE" NEXT="#end"/>
     </STEP>
    </DIALOG>
  </xsl:template>

  <xsl:template match="title">
   <!-- ignore -->
  </xsl:template>

  <xsl:template match="para">
   <xsl:apply-templates/>
  </xsl:template>

</xsl:stylesheet>
