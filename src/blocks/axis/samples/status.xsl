<?xml version="1.0" encoding="iso-8859-1"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:java="http://xml.apache.org/axis/wsdd/providers/java"
    xmlns:a="http://xml.apache.org/axis/wsdd/">
    
    <xsl:template match="/">
        <html>
            <head>
                <title>Cocoon as a SOAP Server</title>
		<link rel="stylesheet" type="text/css" href="/styles/main.css"/>
            </head>
        <body>
            <center><h1>Cocoon SOAP Server Status</h1></center>

            <hr/>

            <p>
		Welcome to the Cocoon SOAP server status page.
            </p>
            <p>
		These samples use 
		<a href="http://ws.apache.org/axis/">Apache Axis</a> to give access to
		Cocoon components via SOAP.
	    </p>

            <h2>Active services</h2>

            <p>
                The SOAP server is currently active and the following
                services are deployed:
            </p>

            <p>
                <ul>
                    <xsl:apply-templates select="/page/soapenv:Envelope/soapenv:Body/a:deployment/a:service"/>
                </ul>
            </p>
            <xsl:apply-templates select="//calling-examples"/>
        </body>
	</html>
    </xsl:template>
    
    <xsl:template match="a:service">
        <li>
            <p><b><xsl:value-of select="@name"/></b></p>

            <p>
                Class:

                <xsl:apply-templates select="a:parameter[@name='className']"/>

                <br/>

                Provider:

                <i><xsl:value-of select="@provider"/></i>

                <br/>

                Methods:

                <xsl:apply-templates select="a:parameter[@name='allowedMethods']"/>
            </p>
        </li>
    </xsl:template>

    <xsl:template match="a:parameter">
        <i><xsl:value-of select="@value"/></i>
    </xsl:template>

    <xsl:template match="calling-examples">
	<h2>Client examples</h2>
	<xsl:apply-templates select="example"/>
    </xsl:template>

    <xsl:template match="example">
	<h3>Example using <xsl:value-of select="@language"/></h3>
	<pre>
		<xsl:value-of select="."/>
	</pre>
    </xsl:template>

</xsl:stylesheet>
