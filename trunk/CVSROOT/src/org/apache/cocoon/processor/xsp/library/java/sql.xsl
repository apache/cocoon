<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xsp="http://www.apache.org/1999/XSP/Core"
	xmlns:sql="http://www.apache.org/1999/SQL"
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
		<xsl:otherwise>"<xsl:value-of select="$content"/>"</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template match="xsp:page">
	<xsp:page>
		<xsl:apply-templates select="@*"/>
		<xsp:structure>
			<xsp:include>org.apache.cocoon.processor.xsp.library.sql.XSPSQLLibrary</xsp:include>
		</xsp:structure>
		<xsl:apply-templates/>
	</xsp:page>
</xsl:template>

<xsl:template match="sql:execute-query">
	<xsl:variable name="driver">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="sql:driver"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="dburl">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="sql:dburl"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="username">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="sql:username"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="password">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="sql:password"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="doc-element">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="sql:doc-element"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="row-element">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="sql:row-element"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="tag-case">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="sql:tag-case"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="null-indicator">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="sql:null-indicator"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="id-attribute">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="sql:id-attribute"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="id-attribute-column">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="sql:id-attribute-column"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="max-rows">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="sql:max-rows"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="skip-rows">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="sql:skip-rows"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="count-attribute">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="sql:count-attribute"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="query-attribute">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="sql:query-attribute"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="skip-rows-attribute">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="sql:skip-rows-attribute"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="max-rows-attribute">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="sql:max-rows-attribute"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="update-rows-attribute">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="sql:update-rows-attribute"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="namespace">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="sql:namespace"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="query">
		<xsl:call-template name="get-nested-string">
			<xsl:with-param name="content" select="sql:query"/>
		</xsl:call-template>
	</xsl:variable>
	<xsp:logic>
		{
		Integer max_rows = new Integer(-1);
		String max_rows_string = String.valueOf(<xsl:copy-of select="$max-rows"/>);
		try {
			max_rows = new Integer(max_rows_string);
		} catch (Exception e) {}
		Integer skip_rows = new Integer(0);
		String skip_rows_string = String.valueOf(<xsl:copy-of select="$skip-rows"/>);
		try {
			skip_rows = new Integer(skip_rows_string);
		} catch (Exception e) {}
		Hashtable column_formats = new Hashtable();
		<xsl:for-each select="sql:column-format">
		{
			String name = String.valueOf(<xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="sql:name"/></xsl:call-template>);
			String classname = String.valueOf(<xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="sql:class"/></xsl:call-template>);
			String parameters[] = new String[<xsl:value-of select="count(sql:parameter)"/>];
			<xsl:for-each select="sql:parameter">
			 parameters[<xsl:value-of select="position()-1"/>] = String.valueOf(<xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="."/></xsl:call-template>);
			</xsl:for-each>
			Class my_class = Class.forName(classname);
			Object format;
			if (parameters.length == 0) {
				//FIXME: we should try newInstance on the base class here probably
				format = my_class.newInstance();
			} else {
				Class class_ary[] = new Class[parameters.length];
				for (int i=0; i&lt;parameters.length; i++) {
					class_ary[i] = parameters[i].getClass();
				}
				java.lang.reflect.Constructor constructor  = my_class.getConstructor(class_ary);
				format = constructor.newInstance(parameters);
			}
			column_formats.put(name,format);
		}
		</xsl:for-each>

	<xsp:content>
	<xsp:expr>
		XSPSQLLibrary.processQuery(
			document,
			String.valueOf(<xsl:copy-of select="$driver"/>),
			String.valueOf(<xsl:copy-of select="$dburl"/>),
			String.valueOf(<xsl:copy-of select="$username"/>),
			String.valueOf(<xsl:copy-of select="$password"/>),
			String.valueOf(<xsl:copy-of select="$doc-element"/>),
			String.valueOf(<xsl:copy-of select="$row-element"/>),
			String.valueOf(<xsl:copy-of select="$tag-case"/>),
			String.valueOf(<xsl:copy-of select="$null-indicator"/>),
			String.valueOf(<xsl:copy-of select="$id-attribute"/>),
			String.valueOf(<xsl:copy-of select="$id-attribute-column"/>),
			max_rows,
			skip_rows,
			String.valueOf(<xsl:copy-of select="$count-attribute"/>),
			String.valueOf(<xsl:copy-of select="$query-attribute"/>),
			String.valueOf(<xsl:copy-of select="$skip-rows-attribute"/>),
			String.valueOf(<xsl:copy-of select="$max-rows-attribute"/>),
			String.valueOf(<xsl:copy-of select="$update-rows-attribute"/>),
			String.valueOf(<xsl:copy-of select="$namespace"/>),
			String.valueOf(<xsl:copy-of select="$query"/>),
			column_formats)
	</xsp:expr>
	</xsp:content>
	}
	</xsp:logic>
</xsl:template>

        <xsl:template match="@*|node()" priority="-1">
		                <xsl:copy><xsl:apply-templates
						select="@*|node()"/></xsl:copy>
						        </xsl:template>


</xsl:stylesheet>
