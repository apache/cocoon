<?xml version="1.0" encoding="UTF-8"?>
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
				<style type="text/css"> <![CDATA[
              H1{font-family : sans-serif,Arial,Tahoma;color : white;background-color : #0086b2;} 
              BODY{font-family : sans-serif,Arial,Tahoma;color : black;background-color : white;} 
              B{color : white;background-color : blue;} 
              HR{color : #0086b2;}
              input { background-color: #FFFFFF; color: #000099; border: 1px solid #0000FF; }		
              table { background-color: #EEEEEE; color: #000099; font-size: x-small; border: 2px solid brown;}
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
              ]]> </style>
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
