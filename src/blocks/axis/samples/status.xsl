<?xml version="1.0" encoding="iso-8859-1"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:java="http://xml.apache.org/axis/wsdd/providers/java"
    xmlns:a="http://xml.apache.org/axis/wsdd/">
    
    <xsl:template match="/page/soapenv:Envelope/soapenv:Body/a:deployment">
        <html>
            <head>
                <title>SOAP Server</title>
            </head>
        <body>
            <center><h1>Cocoon SOAP Server Status</h1></center>

            <hr/>

            <p>Welcome to the Cocoon SOAP server status page</p>

            <p>
                The SOAP server is currently active and the following
                services are deployed:
            </p>

            <p>
                <ul>
                    <xsl:apply-templates select="a:service"/>
                </ul>
            </p>
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

</xsl:stylesheet>
