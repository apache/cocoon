<?xml version="1.0" encoding="UTF-8"?>
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
	Cocoon Feedback Wizard XMLForm processing and displaying stylesheet.	
  
  This stylesheet merges an XMLForm document into 
  a final document. It includes other presentational
  parts of a page orthogonal to the xmlform.

  author: Ivelin Ivanov, ivelin@apache.org, May 2002
  author: Konstantin Piroumian <kpiroumian@protek.com>, September 2002
  author: Simon Price <price@bristol.ac.uk>, September 2002

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xf="http://apache.org/cocoon/jxforms/1.0"
	exclude-result-prefixes="xalan" >
	<xsl:template match="document">
		<html>
			<head>
				<title>JXForms - Cocoon Feedback Wizard</title>
				<link href="/styles/main.css" type="text/css" rel="stylesheet"/>
				<style type="text/css"> <![CDATA[
                  B{color : white;background-color : blue;}
                  input { background-color: #FFFFFF; color: #000099; border: 1px solid #0000FF; }		
                  select { background-color: #FFFFFF; color: #000099 }
                  .caption { line-height: 195% }
                  .error { color: #FF0000; }	      
                  .help { color: #0000FF; font-style: italic; }
                  .invalid { color: #FF0000; border: 2px solid #FF0000; }
                  .info { color: #0000FF; border: 1px solid #0000FF; }
                  .repeat { border: 0px inset #999999;border: 1px inset #999999; width: 100%; }
                  .group { border: 0px inset #999999;border: 0px inset #999999;  width: 100%; }
                  .sub-table { border: none; }
                  .button { background-color: #FFFFFF; color: #000099; border: 1px solid #666666; width: 70px; }
                  .plaintable { border: 0px inset black;border: 0px inset black; width: 100%; }
                  ]]> 
               </style>				
			</head>
			<body>
				<xsl:apply-templates />
			</body>
		</html>
	</xsl:template>
	<xsl:template match="*">
		<xsl:copy-of select="." />
	</xsl:template>
</xsl:stylesheet>
