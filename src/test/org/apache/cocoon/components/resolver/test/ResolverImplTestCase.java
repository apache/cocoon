/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
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

*/
package org.apache.cocoon.components.resolver.test;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import junit.swingui.TestRunner;

import org.apache.avalon.excalibur.testcase.ExcaliburTestCase;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.resolver.ResolverImpl;
import org.apache.cocoon.environment.commandline.CommandLineContext;
import org.apache.cocoon.util.IOUtils;
import org.apache.excalibur.xml.EntityResolver;
import org.xml.sax.InputSource;

/**
 * A test case for components/resolver/ResolverImpl
 *
 * @author <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @author <a href="mailto:crossley@apache.org">David Crossley</a>
 * @version CVS $Id: ResolverImplTestCase.java,v 1.4 2003/05/12 13:26:17 stephan Exp $
 */
public final class ResolverImplTestCase
         extends ExcaliburTestCase
         implements Initializable, Disposable
{
    /**
     * The name of the temporary OASIS Catalog file.
     * The file will be automatically created, then removed when the
     * test is finished.
     *
     * @since
     */
    protected final static String CATALOG_FILE_NAME = "catalog";

    /**
     * The name of a temporary character entity set, here ISOnum.
     * The file will be automatically created, then removed when the
     * test is finished.
     *
     * @since
     */
    protected final static String CATALOG_ISONUM_PEN_FILE_NAME = "ISOnum.pen";

    /**
     * The contents of the temporary OASIS Catalog file, with just
     * sufficient definitions for testing purposes.
     *
     * @since
     */
    protected final static String CATALOG =
        "-- OASIS catalog for Apache Cocoon testing purposes --\n" +
        "\n" +
        "OVERRIDE YES\n" +
        "\n" +
        "-- ISO public identifiers for sets of character entities --\n" +
        "PUBLIC \"ISO 8879:1986//ENTITIES Publishing//EN//XML\" \"ISOpub.pen\"\n" +
        "PUBLIC \"ISO 8879:1986//ENTITIES Numeric and Special Graphic//EN//XML\" \"ISOnum.pen\"\n" +
        "\n" +
        "-- Document Type Definitions --\n" +
        "PUBLIC \"-//APACHE//DTD Documentation V1.0//EN\" \"document-v10.dtd\"\n" +
        "\n" +
        "";

    /**
     * The actual ISOnum character entity set.
     *
     * @since
     */
    protected final static String CATALOG_ISONUM_PEN =
        "<!-- (C) International Organization for Standardization 1986\n" +
        "Permission to copy in any form is granted for use with\n" +
        "conforming SGML systems and applications as defined in\n" +
        "ISO 8879, provided this notice is included in all copies.\n" +
        "-->\n" +
        "<!-- Character entity set. Typical invocation:\n" +
        "<!ENTITY % ISOnum PUBLIC\n" +
        "\"ISO 8879:1986//ENTITIES Numeric and Special Graphic//EN//XML\">\n" +
        "%ISOnum;\n" +
        "-->\n" +
        "<!-- This version of the entity set can be used with any SGML document\n" +
        "which uses ISO 10646 as its document character set.\n" +
        "This includes XML documents and ISO HTML documents.\n" +
        "This entity set uses hexadecimal numeric character references.\n" +
        "          \n" +
        "Creator: Rick Jelliffe, Allette Systems\n" +
        "     \n" +
        "Version: 1997-07-07\n" +
        "-->\n" +
        "\n" +
        "<!ENTITY half   \"&#189;\" ><!--=fraction one-half-->\n" +
        "<!ENTITY frac12 \"&#189;\" ><!--=fraction one-half-->\n" +
        "<!ENTITY frac14 \"&#188;\" ><!--=fraction one-quarter-->\n" +
        "<!ENTITY frac34 \"&#190;\" ><!--=fraction three-quarters-->\n" +
        "<!ENTITY frac18 \"&#x215B;\" >\n" +
        "<!-- or \"&#xB1;&#x202;&#x2044;&#x2088;\" --><!--=fraction one-eighth-->\n" +
        "<!ENTITY frac38 \"&#x215C;\" >\n" +
        "<!-- or \"&#xB3;&#x2044;&#x2088;\" --><!--=fraction three-eighths-->\n" +
        "<!ENTITY frac58 \"&#x215D;\" >\n" +
        "<!-- or \"&#x2075;&#x2044;&#x2088;\" --><!--=fraction five-eighths-->\n" +
        "<!ENTITY frac78 \"&#x215E;\" >\n" +
        "<!-- or \"&#x2077;&#x2044;&#x2088;\" --><!--=fraction seven-eighths-->\n" +
        "\n" +
        "<!ENTITY sup1   \"&#185;\" ><!--=superscript one-->\n" +
        "<!ENTITY sup2   \"&#178;\" ><!--=superscript two-->\n" +
        "<!ENTITY sup3   \"&#179;\" ><!--=superscript three-->\n" +
        "\n" +
        "<!ENTITY plus   \"+\" ><!--=plus sign B:-->\n" +
        "<!ENTITY plusmn \"&#xB1;\" ><!--/pm B: =plus-or-minus sign-->\n" +
        "<!ENTITY lt     \"&#38;#60;\"      ><!--=less-than sign R:-->\n" +
        "<!ENTITY equals \"=\"      ><!--=equals sign R:-->\n" +
        "<!ENTITY gt     \">\"      ><!--=greater-than sign R:-->\n" +
        "<!ENTITY divide \"&#247;\" ><!--/div B: =divide sign-->\n" +
        "<!ENTITY times  \"&#215;\" ><!--/times B: =multiply sign-->\n" +
        "\n" +
        "<!ENTITY curren \"&#164;\" ><!--=general currency sign-->\n" +
        "<!ENTITY pound  \"&#163;\" ><!--=pound sign-->\n" +
        "<!ENTITY dollar \"$\"      ><!--=dollar sign-->\n" +
        "<!ENTITY cent   \"&#162;\" ><!--=cent sign-->\n" +
        "<!ENTITY yen    \"&#165;\" ><!--/yen =yen sign-->\n" +
        "\n" +
        "<!ENTITY num    \"#\" ><!--=number sign-->\n" +
        "<!ENTITY percnt \"&#37;\" ><!--=percent sign-->\n" +
        "<!ENTITY amp    \"&#38;#38;\" ><!--=ampersand-->\n" +
        "<!ENTITY ast    \"*\" ><!--/ast B: =asterisk-->\n" +
        "<!ENTITY commat \"@\" ><!--=commercial at-->\n" +
        "<!ENTITY lsqb   \"[\" ><!--/lbrack O: =left square bracket-->\n" +
        "<!ENTITY bsol   \"\\\" ><!--/backslash =reverse solidus-->\n" +
        "<!ENTITY rsqb   \"]\" ><!--/rbrack C: =right square bracket-->\n" +
        "<!ENTITY lcub   \"{\" ><!--/lbrace O: =left curly bracket-->\n" +
        "<!ENTITY horbar \"&#x2015;\" ><!--=horizontal bar-->\n" +
        "<!ENTITY verbar \"|\" ><!--/vert =vertical bar-->\n" +
        "<!ENTITY rcub   \"}\" ><!--/rbrace C: =right curly bracket-->\n" +
        "<!ENTITY micro  \"&#181;\" ><!--=micro sign-->\n" +
        "<!ENTITY ohm    \"&#2126;\" ><!--=ohm sign-->\n" +
        "<!ENTITY deg    \"&#176;\" ><!--=degree sign-->\n" +
        "<!ENTITY ordm   \"&#186;\" ><!--=ordinal indicator, masculine-->\n" +
        "<!ENTITY ordf   \"&#170;\" ><!--=ordinal indicator, feminine-->\n" +
        "<!ENTITY sect   \"&#167;\" ><!--=section sign-->\n" +
        "<!ENTITY para   \"&#182;\" ><!--=pilcrow (paragraph sign)-->\n" +
        "<!ENTITY middot \"&#183;\" ><!--/centerdot B: =middle dot-->\n" +
        "<!ENTITY larr   \"&#x2190;\" ><!--/leftarrow /gets A: =leftward arrow-->\n" +
        "<!ENTITY rarr   \"&#x2192;\" ><!--/rightarrow /to A: =rightward arrow-->\n" +
        "<!ENTITY uarr   \"&#x2191;\" ><!--/uparrow A: =upward arrow-->\n" +
        "<!ENTITY darr   \"&#x2193;\" ><!--/downarrow A: =downward arrow-->\n" +
        "<!ENTITY copy   \"&#169;\" ><!--=copyright sign-->\n" +
        "<!ENTITY reg    \"&#174;\" ><!--/circledR =registered sign-->\n" +
        "<!ENTITY trade  \"&#8482;\" ><!--=trade mark sign-->\n" +
        "<!ENTITY brvbar \"&#xA6;\" ><!--=bren (vertical) bar-->\n" +
        "<!ENTITY not    \"&#xAC;\" ><!--/neg /lnot =not sign-->\n" +
        "<!ENTITY sung   \"&#x266A;\" ><!--=music note (sung text sign)-->\n" +
        "\n" +
        "<!ENTITY excl   \"!\" ><!--=exclamation mark-->\n" +
        "<!ENTITY iexcl  \"&#xA1;\" ><!--=inverted exclamation mark-->\n" +
        "<!ENTITY quot   '\"' ><!--=quotation mark-->\n" +
        "<!ENTITY apos   \"'\" ><!--=apostrophe-->\n" +
        "<!ENTITY lpar   \"(\" ><!--O: =left parenthesis-->\n" +
        "<!ENTITY rpar   \")\" ><!--C: =right parenthesis-->\n" +
        "<!ENTITY comma  \",\" ><!--P: =comma-->\n" +
        "<!ENTITY lowbar \"_\" ><!--=low line-->\n" +
        "<!ENTITY hyphen \"&#x2010;\" ><!--=hyphen-->\n" +
        "<!ENTITY period \".\" ><!--=full stop, period-->\n" +
        "<!ENTITY sol    \"/\" ><!--=solidus-->\n" +
        "<!ENTITY colon  \":\" ><!--/colon P:-->\n" +
        "<!ENTITY semi   \";\" ><!--=semicolon P:-->\n" +
        "<!ENTITY quest  \"?\" ><!--=question mark-->\n" +
        "<!ENTITY iquest \"&#xBF;\" ><!--=inverted question mark-->\n" +
        "<!ENTITY laquo  \"&#x2039;\" ><!--=angle quotation mark, left\n" +
        "But note that Unicode 1 & Maler & el Andaloussi give &#xAB; -->\n" +
        "<!ENTITY raquo  \"&#x203A;\" ><!--=angle quotation mark, right\n" +
        "But note that Unicode 1 & Maler & el Andaloussi give &#xBB; -->\n" +
        "<!ENTITY lsquo  \"&#x2018;\" ><!--=single quotation mark, left-->\n" +
        "<!ENTITY rsquo  \"&#x2019;\" ><!--=single quotation mark, right-->\n" +
        "<!ENTITY ldquo  \"&#x201C;\" ><!--=double quotation mark, left-->\n" +
        "<!ENTITY rdquo  \"&#x201D;\" ><!--=double quotation mark, right-->\n" +
        "<!ENTITY nbsp   \"&#160;\" ><!--=no break (required) space-->\n" +
        "<!ENTITY shy    \"&#173;\" ><!--=soft hyphen-->\n" +
        "";
    private DefaultContext context;
    private ResolverImpl resolverImpl;
    private File workDir;
    private File commandlineContextDir;


    /**
     * Constructor for the ResolverImplTestCase object
     *
     * @since
     */
    public ResolverImplTestCase() {
        this("ResolverImplTestCase");
    }


    /**
     * Constructor for the ResolverImplTestCase object
     *
     * @param  name  Description of Parameter
     * @since
     */
    public ResolverImplTestCase(String name) {
        super(name);
    }


    /**
     * The main program for the ResolverImplTestCase class.
     *
     * @param  args           The command line arguments
     * @exception  Exception  Description of Exception
     * @since
     */
    public static void main(final String[] args) throws Exception {
        final String[] testCaseName = {ResolverImplTestCase.class.getName()};
        TestRunner.main(testCaseName);
    }

    /**
     * The JUnit setup method. Lookup the resolver role.
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void setUp() throws Exception {
        super.setUp();

        String role = EntityResolver.ROLE;
        resolverImpl = (ResolverImpl) manager.lookup(role);
        assertNotNull("ResolverImpl is null", resolverImpl);
    }

    /**
     * Invoked before components are created.
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void initialize() throws Exception {
        workDir = new File(System.getProperty("java.io.tmpdir"),
            Constants.DEFAULT_WORK_DIR);
        commandlineContextDir = new File(workDir.getAbsolutePath());

        // create catalog releated files in context dir
        getLogger().debug("Creating test catalog files");
        File f;
        f = IOUtils.createFile(commandlineContextDir, CATALOG_FILE_NAME);
        IOUtils.serializeString(f, CATALOG);

        f = IOUtils.createFile(commandlineContextDir,
                CATALOG_ISONUM_PEN_FILE_NAME);
        IOUtils.serializeString(f, CATALOG_ISONUM_PEN);
    }

    /**
     * Invoked after components have been disposed.
     *
     * @since
     */
    public void dispose() {
        assertTrue( resolverImpl == null );

        // remove catalog related files in context dir
        getLogger().debug("Removing test catalog files");

        File f;
        f = new File(commandlineContextDir, CATALOG_ISONUM_PEN_FILE_NAME);
        getLogger().debug("Removing catalog file " + f.toString());
        if (!f.delete()) {
            getLogger().warn("Cannot remove catalog file " + f.toString());
        }

        f = new File(commandlineContextDir, CATALOG_FILE_NAME);
        getLogger().debug("Removing catalog file " + f.toString());
        if (!f.delete()) {
            getLogger().warn("Cannot remove catalog file " + f.toString());
        }

        f = new File(commandlineContextDir.toString());
        getLogger().debug("Removing catalog file " + f.toString());
        if (!f.delete()) {
            getLogger().warn("Cannot remove catalog file " + f.toString());
        }
    }

    /**
     * The teardown method for JUnit
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void tearDown() throws Exception {
        super.tearDown();

        if (resolverImpl != null) {
            manager.release(resolverImpl);
            resolverImpl = null;
        }
    }

    /**
     * JUnit test case:
     * Ensure CatalogManager.properties configuration file is available.
     * This is a mandatory file which must be available on the classpath.
     * The properties that are defined for resolver can also be over-ridden
     * via cocoon.xconf
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void testCatalogPropertiesAvailable() throws Exception {
        Class clazz = this.getClass();
        String catalog_manager_props_name = "/CatalogManager.properties";
        InputStream is = clazz.getResourceAsStream(catalog_manager_props_name);

        assertNotNull("Cannot access " + catalog_manager_props_name, is);
        Properties catalog_manager_props = new Properties();
        catalog_manager_props.load(is);
        is.close();
    }

    /**
     * JUnit test case:
     * Ask for an entity using the proper publicId and systemId
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void testAvailableResolveEntity() throws Exception {
        assertNotNull("ResolverImpl is null", resolverImpl);

        String public_id;
        String system_id;
        InputSource is;
        public_id = "ISO 8879:1986//ENTITIES Numeric and Special Graphic//EN//XML";
        system_id = "ISOnum.pen";
        is = resolverImpl.resolveEntity(public_id, system_id);
        assertNotNull("InputSource is null for " +
                "'" + public_id + "'" + ", " +
                "'" + system_id + "'", is);
                
        // close the entity stream, otherwise removing it will fail
        // (note that normally the parser would handle this)
        java.io.Reader entity_r = is.getCharacterStream();
        if (entity_r != null) {
            entity_r.close();
        }
        java.io.InputStream entity_is = is.getByteStream();
        if (entity_is != null) {
            entity_is.close();
        }
        is = null;
    }

    /**
     * JUnit test case:
     * Ask for an entity using deliberately non-existent publicId and systemId
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void testNonAvailableResolveEntity() throws Exception {
        assertNotNull("ResolverImpl is null", resolverImpl);
        String public_id;
        String system_id;
        InputSource is;
        public_id = "publicId which does not exist";
        system_id = "systemId which does not exist";
        is = resolverImpl.resolveEntity(public_id, system_id);
        assertEquals("InputSource is null for " +
                "'" + public_id + "'" + ", " +
                "'" + system_id + "'", null, is);
        is = null;
    }

    /**
     * This method may be overwritten by subclasses to put additional objects
     * into the context programmatically.
     *
     * @param  context  The feature to be added to the Context attribute
     * @since
     */
    protected void addContext(DefaultContext context) {
        this.context = context;

        context.put(Constants.CONTEXT_WORK_DIR, workDir);

        CommandLineContext commandline_context = new CommandLineContext(commandlineContextDir.toString());
        commandline_context.enableLogging(getLogEnabledLogger());
        context.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT, commandline_context);
    }
}
