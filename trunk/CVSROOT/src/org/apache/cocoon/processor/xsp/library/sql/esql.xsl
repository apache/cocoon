<?xml version="1.0"?>
<!--

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

-->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xsp="http://www.apache.org/1999/XSP/Core"
	xmlns:esql="http://apache.org/cocoon/SQL/v2"
>

  <xsl:template name="get-nested-content">
    <xsl:param name="content"/>
	<xsl:choose>
		<xsl:when test="$content/*">
			<xsl:apply-templates select="$content/*"/>
		</xsl:when>
		<xsl:otherwise>"<xsl:value-of select="$content"/>"</xsl:otherwise>
	</xsl:choose>
  </xsl:template>

  <xsl:template name="get-nested-string">
  	<xsl:param name="content"/>
	<xsl:choose>
		<xsl:when test="$content/*">
			""
			<xsl:for-each select="$content/node()">
				<xsl:choose>
					<xsl:when test="name(.)">
						+ <xsl:apply-templates select="."/>
					</xsl:when>
					<xsl:otherwise>
						+ "<xsl:value-of select="translate(.,'&#9;&#10;&#13;','   ')"/>"
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:when>
		<xsl:otherwise>"<xsl:value-of select="normalize-space($content)"/>"</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template match="xsp:page">
	<xsp:page>
		<xsl:apply-templates select="@*"/>
		<xsp:structure>
			<xsp:include>java.sql.DriverManager</xsp:include>
			<xsp:include>java.sql.Connection</xsp:include>
			<xsp:include>java.sql.Statement</xsp:include>
			<xsp:include>java.sql.ResultSet</xsp:include>
			<xsp:include>java.sql.ResultSetMetaData</xsp:include>
			<xsp:include>java.sql.SQLException</xsp:include>
			<xsp:include>java.text.SimpleDateFormat</xsp:include>
			<xsp:include>org.apache.turbine.util.db.pool.DBBroker</xsp:include>
			<xsp:include>org.apache.turbine.util.db.pool.DBConnection</xsp:include>
		</xsp:structure>
		<xsp:logic>
		 static DBBroker _esql_pool = DBBroker.getInstance();
                 class EsqlSession {
                  DBConnection db_connection;
                  Connection connection;
                  boolean close_connection = true;
                  Statement statement;
                  ResultSet resultset;
                  ResultSetMetaData resultset_metadata;
                  int count;
                  int max_rows;
                  int skip_rows;
                 }
                 Stack _esql_sessions = new Stack();
                 EsqlSession _esql_session = null;
		</xsp:logic>
                <xsl:apply-templates select=".//esql:execute-query" mode="generate-method"/>
		<xsl:apply-templates/>
	</xsp:page>
</xsl:template>

<xsl:template match="xsp:page/*">
 <xsl:copy>
  <xsl:apply-templates select="@*"/>
  <xsl:apply-templates/>
 </xsl:copy>
</xsl:template>

<xsl:template match="@*|node()" priority="-1">
 <xsl:copy>
  <xsl:apply-templates select="@*|node()"/>
 </xsl:copy>
</xsl:template>

<xsl:template match="esql:execute-query">
 <xsp:logic>_esql_execute_query_<xsl:value-of select="generate-id(.)"/>(request,response,document,xspParentNode,xspCurrentNode,xspNodeStack,session);</xsp:logic>
</xsl:template>

<xsl:template match="esql:execute-query" mode="generate-method">
	<xsl:variable name="use-connection">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="esql:use-connection"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="driver">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="esql:driver"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="dburl">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="esql:dburl"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="username">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="esql:username"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="password">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="esql:password"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="max-rows">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="esql:max-rows"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="skip-rows">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="esql:skip-rows"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="query">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="esql:query"/>
		</xsl:call-template>
	</xsl:variable>
	<xsp:logic>
	 void _esql_execute_query_<xsl:value-of select="generate-id(.)"/>(
	 HttpServletRequest request,
	 HttpServletResponse response,
	 Document document,
	 Node xspParentNode,
	 Node xspCurrentNode,
	 Stack xspNodeStack,
	 HttpSession session) throws Exception {
		if (_esql_session != null) {
		 _esql_sessions.push(_esql_session);
		}
		_esql_session = new EsqlSession();
		try {
		 _esql_session.max_rows = Integer.parseInt(String.valueOf(<xsl:copy-of select="$max-rows"/>));
		} catch (Exception _esql_e) {
		 _esql_session.max_rows = -1;
		}
		try {
		 _esql_session.skip_rows = Integer.parseInt(String.valueOf(<xsl:copy-of select="$skip-rows"/>));
		} catch (Exception _esql_e) {
		 _esql_session.skip_rows = 0;
		}
		try {
		<xsl:choose>
		 <xsl:when test="not(esql:use-connection or esql:dburl)">
                  _esql_session.connection = ((EsqlSession)_esql_sessions.peek()).connection;
		  _esql_session.close_connection = false;
		 </xsl:when>
		 <xsl:when test="esql:use-connection">
		  _esql_session.db_connection = _esql_pool.getConnection(String.valueOf(<xsl:copy-of select="$use-connection"/>));
		  _esql_session.connection = _esql_session.db_connection.getConnection();
		 </xsl:when>
		 <xsl:otherwise>
		  Class.forName(String.valueOf(<xsl:copy-of select="$driver"/>)).newInstance();
		  <xsl:choose>
		   <xsl:when test="esql:username">
		    _esql_session.connection = DriverManager.getConnection(
		     String.valueOf(<xsl:copy-of select="$dburl"/>),
		     String.valueOf(<xsl:copy-of select="$username"/>),
		     String.valueOf(<xsl:copy-of select="$password"/>)
		    );
		   </xsl:when>
		   <xsl:otherwise>
		    _esql_session.connection = DriverManager.getConnection(
		     String.valueOf(<xsl:copy-of select="$dburl"/>),
		    );
		   </xsl:otherwise>
		  </xsl:choose>
		 </xsl:otherwise>
	        </xsl:choose>
	       _esql_session.statement = _esql_session.connection.createStatement();
	       _esql_session.resultset = _esql_session.statement.executeQuery(<xsl:copy-of select="$query"/>);
	       _esql_session.resultset_metadata = _esql_session.resultset.getMetaData();
	       _esql_session.count = 0;
	       if (_esql_session.skip_rows &gt; 0) {
	        while (_esql_session.resultset.next()) {
		 _esql_session.count++;
		 if (_esql_session.count == _esql_session.skip_rows) {
	          break;
		 }
		}
	       }
	       while (_esql_session.resultset.next()) {
	        <xsl:apply-templates select="esql:results/*"/>
		if (_esql_session.max_rows != -1 &amp;&amp; _esql_session.count - _esql_session.skip_rows == _esql_session.max_rows-1) {
		 break;
		}
		_esql_session.count++;
	       }
	       _esql_session.resultset.close();
	       _esql_session.statement.close();
	       } catch (Exception _esql_e) {
		<exception>
		 <message><xsp:expr>_esql_e.getMessage()</xsp:expr></message>
		</exception>
	       } finally {
	       if (_esql_session.close_connection) {
	        _esql_session.connection.close();
	        <xsl:if test="esql:use-connection">
	         _esql_session.pool.releaseConnection(_esql_db_connection);
	        </xsl:if>
	       }
	       if (_esql_sessions.empty()) {
	        _esql_session = null;
	       } else {
	        _esql_session = (EsqlSession)_esql_sessions.pop();
	       }
	      }
	     }
	</xsp:logic>
</xsl:template>

<xsl:template match="esql:results//esql:get-columns">
 <xsp:logic>
  for (int _esql_i=1; _esql_i &lt;= _esql_session.resultset_metadata.getColumnCount(); _esql_i++) {
   Node _esql_node = document.createElement(_esql_session.resultset_metadata.getColumnName(_esql_i));
   _esql_node.appendChild(document.createTextNode(_esql_session.resultset.getString(_esql_i)));
   xspCurrentNode.appendChild(_esql_node);
  }
 </xsp:logic>
</xsl:template>

<xsl:template match="esql:results//esql:get-string" name="get-string">
 <xsp:expr><xsl:call-template name="get-resultset"/>.getString(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

<xsl:template match="esql:results//esql:get-date">
 <xsl:choose>
  <xsl:when test="@format">
   <xsp:expr>new SimpleDateFormat("<xsl:value-of select="@format"/>").format(<xsl:call-template name="get-resultset"/>.getDate(<xsl:call-template name="get-column"/>))</xsp:expr>
  </xsl:when>
  <xsl:otherwise>
   <xsp:expr><xsl:call-template name="get-resultset"/>.getDate(<xsl:call-template name="get-column"/>)</xsp:expr>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="esql:results//esql:get-time">
 <xsl:choose>
  <xsl:when test="@format">
   <xsp:expr>new SimpleDateFormat("<xsl:value-of select="@format"/>").format(<xsl:call-template name="get-resultset"/>.getTime(<xsl:call-template name="get-column"/>))</xsp:expr>
  </xsl:when>
  <xsl:otherwise>
   <xsp:expr><xsl:call-template name="get-resultset"/>.getTime(<xsl:call-template name="get-column"/>)</xsp:expr>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="esql:results//esql:get-timestamp">
 <xsl:choose>
  <xsl:when test="@format">
   <xsp:expr>new SimpleDateFormat("<xsl:value-of select="@format"/>").format(<xsl:call-template name="get-resultset"/>.getTimestamp(<xsl:call-template name="get-column"/>))</xsp:expr>
  </xsl:when>
  <xsl:otherwise>
   <xsp:expr><xsl:call-template name="get-resultset"/>.getTimestamp(<xsl:call-template name="get-column"/>)</xsp:expr>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="esql:results//esql:get-xml">
 <xsl:variable name="content">
  <xsl:choose>
   <xsl:when test="@root">
    <xsl:text>"&lt;</xsl:text>
    <xsl:value-of select="@root"/>
    <xsl:text>&gt;"+</xsl:text>
    <xsl:call-template name="get-string"/>
    <xsl:text>+"&lt;/</xsl:text>
    <xsl:value-of select="@root"/>
    <xsl:text>&gt;"</xsl:text>
   </xsl:when>
   <xsl:otherwise>
    <xsl:call-template name="get-string"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:variable>
 <xsp:expr>this.xspParser.parse(new InputSource(new StringReader(<xsl:copy-of select="$content"/>))).getDocumentElement()</xsp:expr>
</xsl:template>

<xsl:template match="esql:results//esql:get-row-number">
 <xsp:expr>_esql_session.count</xsp:expr>
</xsl:template>

<xsl:template match="esql:results//esql:get-column-name">
 <xsp:expr>_esql_session.resultset_metadata.getColumnName(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

<xsl:template match="esql:results//esql:get-column-label">
 <xsp:expr>_esql_session.resultset_metadata.getColumnLabel(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

<xsl:template match="esql:results//esql:get-column-type-name">
 <xsp:expr>_esql_session.resultset_metadata.getColumnTypeName(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

<xsl:template name="get-resultset">
 <xsl:choose>
  <xsl:when test="@ancestor">
   <xsl:text>((EsqlSession)_esql_sessions.elementAt(_esql_sessions.size()-</xsl:text>
   <xsl:value-of select="@ancestor"/>
   <xsl:text>)).resultset</xsl:text>
  </xsl:when>
  <xsl:otherwise>
   <xsl:text>_esql_session.resultset</xsl:text>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template name="get-column">
 <xsl:choose>
  <xsl:when test="@column">
   <xsl:choose>
    <xsl:when test="not(string(number(@column))='NaN')">
     <xsl:value-of select="@column"/>
    </xsl:when>
    <xsl:otherwise>
     <xsl:text>"</xsl:text>
     <xsl:value-of select="@column"/>
     <xsl:text>"</xsl:text>
    </xsl:otherwise>
   </xsl:choose>
  </xsl:when>
  <xsl:when test="esql:column">
   <xsl:call-template name="get-nested-string">
    <xsl:with-param name="content" select="esql:column"/>
   </xsl:call-template>
  </xsl:when>
 </xsl:choose>
</xsl:template>

</xsl:stylesheet>
