<?xml version="1.0"?>
<!-- $Id: esql.xsl,v 1.23 2000-10-17 04:35:44 balld Exp $-->
<!--

 ============================================================================
                   The Apache Software License, Version 1.2
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
	xmlns:xspdoc="http://apache.org/cocoon/XSPDoc/v1"
>
<xspdoc:title>the esql logicsheet</xspdoc:title>

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
			<xsp:include>java.sql.PreparedStatement</xsp:include>
			<xsp:include>java.sql.ResultSet</xsp:include>
			<xsp:include>java.sql.ResultSetMetaData</xsp:include>
			<xsp:include>java.sql.SQLException</xsp:include>
			<xsp:include>java.text.SimpleDateFormat</xsp:include>
			<xsp:include>java.text.DecimalFormat</xsp:include>
			<xsp:include>org.apache.turbine.services.db.PoolBrokerService</xsp:include>
			<xsp:include>org.apache.turbine.util.db.pool.DBConnection</xsp:include>
		</xsp:structure>
		<xsp:logic>
		 static PoolBrokerService _esql_pool = PoolBrokerService.getInstance();
                 class EsqlSession {
                  DBConnection db_connection=null;
                  Connection connection=null;
                  boolean close_connection = true;
		  String query;
                  Statement statement;
		  PreparedStatement prepared_statement;
                  ResultSet resultset;
                  ResultSetMetaData resultset_metadata;
                  int count;
                  int max_rows;
                  int skip_rows;
                  boolean has_resultset;
		  int update_count;
                 }
		</xsp:logic>
		<xsl:for-each select=".//esql:execute-query[not(@inner-method='no')]">
		 <xsl:call-template name="generate-code">
		  <xsl:with-param name="inner-method">yes</xsl:with-param>
		 </xsl:call-template>
		</xsl:for-each>
		<xsl:apply-templates/>
	</xsp:page>
</xsl:template>

<xsl:template match="xsp:page/*[not(namespace-uri(.)='http://www.apache.org/1999/XSP/Core')]">
 <xsl:copy>
  <xsl:apply-templates select="@*"/>
  <xsp:logic>
   Stack _esql_sessions = new Stack();
   EsqlSession _esql_session = null;
  </xsp:logic>
  <xsl:apply-templates/>
 </xsl:copy>
</xsl:template>

<xsl:template match="@*|node()" priority="-1">
 <xsl:copy>
  <xsl:apply-templates select="@*|node()"/>
 </xsl:copy>
</xsl:template>

 <xspdoc:desc>indicates that a sql connection is going to be defined and one or more queries may be executed</xspdoc:desc>
<xsl:template match="esql:execute-query">
 <xsl:choose>
  <xsl:when test="@inner-method='no'">
   <xsl:call-template name="generate-code">
    <xsl:with-param name="inner-method">no</xsl:with-param>
   </xsl:call-template>
  </xsl:when>
  <xsl:otherwise>
   <xsp:logic>_esql_execute_query_<xsl:value-of select="generate-id(.)"/>(request,response,document,xspParentNode,xspCurrentNode,xspNodeStack,session,_esql_sessions,_esql_session);</xsp:logic>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template name="generate-code">
	<xsl:param name="inner-method"/>
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
	<xsl:variable name="statement">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="esql:statement"/>
		</xsl:call-template>
	</xsl:variable>
	<xsp:logic>
	 <xsl:choose>
	  <xsl:when test="$inner-method='yes'">
	 void _esql_execute_query_<xsl:value-of select="generate-id(.)"/>(
	 HttpServletRequest request,
	 HttpServletResponse response,
	 Document document,
	 Node xspParentNode,
	 Node xspCurrentNode,
	 Stack xspNodeStack,
	 HttpSession session,
	 Stack _esql_sessions,
	 EsqlSession _esql_session) throws Exception {
	  </xsl:when>
	  <xsl:when test="$inner-method='no'">
	   {
          </xsl:when>
	 </xsl:choose>
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
		     String.valueOf(<xsl:copy-of select="$dburl"/>)
		    );
		   </xsl:otherwise>
		  </xsl:choose>
		 </xsl:otherwise>
	        </xsl:choose>
	       <xsl:choose>
	        <xsl:when test="esql:query">
	         _esql_session.query = String.valueOf(<xsl:copy-of select="$query"/>);
	         _esql_session.statement = _esql_session.connection.createStatement();
                 _esql_session.has_resultset = _esql_session.statement.execute(_esql_session.query);
		</xsl:when>
		<xsl:when test="esql:statement">
		 _esql_session.prepared_statement = _esql_session.connection.prepareStatement(String.valueOf(<xsl:copy-of select="$statement"/>));
		 _esql_session.statement = _esql_session.prepared_statement;
		 <xsl:text>_esql_session.prepared_statement.</xsl:text>
		 <xsl:for-each select=".//esql:parameter">
		  <xsl:choose>
		   <xsl:when test="@type">
		    <xsl:variable name="type"><xsl:value-of select="concat(translate(substring(@type,0,1),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),substring(@type,1))"/></xsl:variable>
                    <xsl:text>set</xsl:text><xsl:value-of select="$type"/>(<xsl:value-of select="position()"/>,<xsl:call-template name="get-nested-content"><xsl:with-param name="content" select="."/></xsl:call-template>);<xsl:text>
		    </xsl:text>
		   </xsl:when>
		   <xsl:otherwise>
		  <xsl:text>setString(</xsl:text><xsl:value-of select="position()"/>,String.valueOf(<xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="."/></xsl:call-template>));<xsl:text>
		  </xsl:text>
		   </xsl:otherwise>
		  </xsl:choose>
		 </xsl:for-each>
	         _esql_session.has_resultset = _esql_session.prepared_statement.execute();
		</xsl:when>
	       </xsl:choose>
               if (_esql_session.has_resultset) {
                _esql_session.resultset = _esql_session.statement.getResultSet();
	        _esql_session.resultset_metadata = _esql_session.resultset.getMetaData();
                _esql_session.update_count = -1;
	        _esql_session.count = 0;
	        if (_esql_session.skip_rows &gt; 0) {
	         while (_esql_session.resultset.next()) {
		  _esql_session.count++;
		  if (_esql_session.count == _esql_session.skip_rows) {
	           break;
		  }
		 }
	        }
	        boolean _esql_results_<xsl:value-of select="generate-id(.)"/> = false;
	        while (_esql_session.resultset.next()) {
		 _esql_results_<xsl:value-of select="generate-id(.)"/> = true;
	         <xsl:apply-templates select="esql:results/*"/>
		 if (_esql_session.max_rows != -1 &amp;&amp; _esql_session.count - _esql_session.skip_rows == _esql_session.max_rows-1) {
		  break;
		 }
		 _esql_session.count++;
	        }
	        _esql_session.resultset.close();
	        if (!_esql_results_<xsl:value-of select="generate-id(.)"/>) {
                 <xsl:apply-templates select="esql:no-results/*"/>
	        }
               } else {
                _esql_session.update_count = _esql_session.statement.getUpdateCount();
                <xsl:apply-templates select="esql:count-results/*"/>
               }
	       _esql_session.statement.close();
	       } catch (Exception _esql_exception) {
		<xsl:if test="esql:error-results//esql:get-stacktrace">
		 StringWriter _esql_exception_writer = new StringWriter();
		 _esql_exception.printStackTrace(new PrintWriter(_esql_exception_writer));
		</xsl:if>
		<xsl:apply-templates select="esql:error-results/*"/>
	       } finally {
	       if (_esql_session.close_connection) {
	        if (_esql_session.connection != null) _esql_session.connection.close();
	        <xsl:if test="esql:use-connection">
	         _esql_pool.releaseConnection(_esql_session.db_connection);
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

<xsl:template match="esql:statement//esql:parameter">"?"</xsl:template>

<xspdoc:desc>if the query has results, this element's children will be instantiated for each row in the result set</xspdoc:desc>
<xsl:template match="esql:execute-query/esql:results">
 <xsl:apply-templates/>
</xsl:template>

<xspdoc:desc>if the query has no results, this element's children will be instantiated once</xspdoc:desc>
<xsl:template match="esql:execute-query/esql:no-results">
 <xsl:apply-templates/>
</xsl:template>

<xspdoc:desc>if the query results in an error, this element's children will be instantiated once</xspdoc:desc>
<xsl:template match="esql:execute-query/esql:error-results">
 <xsl:apply-templates/>
</xsl:template>

 <xspdoc:desc>results in a set of elements whose names are the names of the columns. the elements each have one text child, whose value is the value of the column interpreted as a string. No special formatting is allowed here. If you want to mess around with the names of the elements or the value of the text field, use the type-specific get methods and write out the result fragment yourself.</xspdoc:desc>
<xsl:template match="esql:results//esql:get-columns">
 <xsp:logic>
  for (int _esql_i=1; _esql_i &lt;= _esql_session.resultset_metadata.getColumnCount(); _esql_i++) {
   Node _esql_node = document.createElement(_esql_session.resultset_metadata.getColumnName(_esql_i));
   _esql_node.appendChild(document.createTextNode(_esql_session.resultset.getString(_esql_i)));
   xspCurrentNode.appendChild(_esql_node);
  }
 </xsp:logic>
</xsl:template>

 <xspdoc:desc>returns the value of the given column as a string</xspdoc:desc>
<xsl:template match="esql:results//esql:get-string" name="get-string">
 <xsp:expr><xsl:call-template name="get-resultset"/>.getString(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

 <xspdoc:desc>returns the value of the given column as a date. if a format attribute exists, its value is taken to be a date format string as defined in java.text.SimpleDateFormat, and the result is formatted accordingly.</xspdoc:desc>
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

 <xspdoc:desc>returns the value of the given column as a time. if a format attribute exists, its value is taken to be a date format string as defined in java.text.SimpleDateFormat, and the result is formatted accordingly.</xspdoc:desc>
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

 <xspdoc:desc>returns the value of the given column as a timestamp. if a format attribute exists, its value is taken to be a date format string as defined in java.text.SimpleDateFormat, and the result is formatted accordingly.</xspdoc:desc>
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

 <xspdoc:desc>returns the value of the given column as true or false</xspdoc:desc>
<xsl:template match="esql:results//esql:get-boolean">
 <xsp:expr><xsl:call-template name="get-resultset"/>.getBoolean(<xsl:call-template name="get-column"/>) ? "true" : "false"</xsp:expr>
</xsl:template>

 <xspdoc:desc>returns the value of the given column as a double. if a format attribute exists, its value is taken to be a decimal format string as defined in java.text.DecimalFormat, and the result is formatted accordingly.</xspdoc:desc>
<xsl:template match="esql:results//esql:get-double">
 <xsl:choose>
  <xsl:when test="@format">
   <xsp:expr>new DecimalFormat("<xsl:value-of select="@format"/>").format(new Double(<xsl:call-template name="get-resultset"/>.getDouble(<xsl:call-template name="get-column"/>)))</xsp:expr>
  </xsl:when>
  <xsl:otherwise>
   <xsp:expr>""+<xsl:call-template name="get-resultset"/>.getDouble(<xsl:call-template name="get-column"/>)</xsp:expr>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

 <xspdoc:desc>returns the value of the given column as a float. if a format attribute exists, its value is taken to be a decimal format string as defined in java.text.DecimalFormat, and the result is formatted accordingly.</xspdoc:desc>
<xsl:template match="esql:results//esql:get-float">
 <xsl:choose>
  <xsl:when test="@format">
   <xsp:expr>new DecimalFormat("<xsl:value-of select="@format"/>").format(new Float(<xsl:call-template name="get-resultset"/>.getFloat(<xsl:call-template name="get-column"/>)))</xsp:expr>
  </xsl:when>
  <xsl:otherwise>
   <xsp:expr>""+<xsl:call-template name="get-resultset"/>.getFloat(<xsl:call-template name="get-column"/>)</xsp:expr>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

 <xspdoc:desc>returns the value of the given column as an integer</xspdoc:desc>
<xsl:template match="esql:results//esql:get-int">
 <xsp:expr>""+<xsl:call-template name="get-resultset"/>.getInt(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

 <xspdoc:desc>returns the value of the given column as a long</xspdoc:desc>
<xsl:template match="esql:results//esql:get-long">
 <xsp:expr>""+<xsl:call-template name="get-resultset"/>.getLong(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

 <xspdoc:desc>returns the value of the given column as a short</xspdoc:desc>
<xsl:template match="esql:results//esql:get-short">
 <xsp:expr>""+<xsl:call-template name="get-resultset"/>.getShort(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

 <xspdoc:desc>returns the value of the given column interpeted as an xml fragment. the fragment is parsed by the default xsp parser and the document element is returned. if a root attribute exists, its value is taken to be the name of an element to wrap around the contents of the fragment before parsing.</xspdoc:desc>
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

 <xspdoc:desc>returns the position of the current row in the result set</xspdoc:desc>
<xsl:template match="esql:results//esql:get-row-number">
 <xsp:expr>_esql_session.count</xsp:expr>
</xsl:template>

 <xspdoc:desc>returns the name of the given column. the column mus tbe specified by number, not name.</xspdoc:desc>
<xsl:template match="esql:results//esql:get-column-name">
 <xsp:expr><xsl:call-template name="get-resultset"/>.getMetaData().getColumnName(<xsl:value-of select="@column"/>)</xsp:expr>
</xsl:template>

 <xspdoc:desc>returns the label of the given column. the column mus tbe specified by number, not name.</xspdoc:desc>
<xsl:template match="esql:results//esql:get-column-label">
 <xsp:expr><xsl:call-template name="get-resultset"/>.getMetaData().getColumnLabel(<xsl:value-of select="@column"/>)</xsp:expr>
</xsl:template>

 <xspdoc:desc>returns the name of the type of the given column. the column must be specified by number, not name.</xspdoc:desc>
<xsl:template match="esql:results//esql:get-column-type-name">
 <xsp:expr><xsl:call-template name="get-resultset"/>.getMetaData().getColumnTypeName(<xsl:value-of select="@column"/>)</xsp:expr>
</xsl:template>

 <xspdoc:desc>returns the message of the current exception</xspdoc:desc>
<xsl:template match="esql:error-results//esql:get-message">
 <xsp:expr>_esql_exception.getMessage()</xsp:expr>
</xsl:template>

 <xspdoc:desc>returns the current exception as a string</xspdoc:desc>
<xsl:template match="esql:error-results//esql:to-string">
 <xsp:expr>_esql_exception.toString()</xsp:expr>
</xsl:template>

 <xspdoc:desc>returns the stacktrace of the current exception</xspdoc:desc>
<xsl:template match="esql:error-results//esql:get-stacktrace">
 <xsp:expr>_esql_exception_writer.toString()</xsp:expr>
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

 <xspdoc:desc>used internally to determine which column is the given column. if a column attribute exists and its value is a number, it is taken to be the column's position. if the value is not a number, it is taken to be the column's name. if a column attribute does not exist, an esql:column element is assumed to exist and to render as a string (after all of the xsp instructions have been evaluated), which is taken to be the column's name.</xspdoc:desc>
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
