/*
 * Copyright 2003,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.pluto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.cocoon.portal.pluto.factory.ControllerFactoryImpl;
import org.apache.cocoon.portal.pluto.om.PortletApplicationDefinitionImpl;
import org.apache.cocoon.portal.pluto.om.PortletDefinitionRegistryImpl;
import org.apache.cocoon.portal.pluto.om.ServletDefinitionImpl;
import org.apache.cocoon.portal.pluto.om.ServletMapping;
import org.apache.cocoon.portal.pluto.om.WebApplicationDefinitionImpl;
import org.apache.cocoon.portal.pluto.om.common.DescriptionImpl;
import org.apache.cocoon.portal.pluto.om.common.DescriptionSetImpl;
import org.apache.cocoon.portal.pluto.om.common.DisplayNameImpl;
import org.apache.cocoon.portal.pluto.om.common.DisplayNameSetImpl;
import org.apache.cocoon.portal.pluto.om.common.TagDefinition;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.pluto.om.ControllerFactory;
import org.apache.pluto.om.common.Parameter;
import org.apache.pluto.om.common.ParameterCtrl;
import org.apache.pluto.om.common.ParameterSet;
import org.apache.pluto.om.common.ParameterSetCtrl;
import org.apache.pluto.om.common.SecurityRoleRef;
import org.apache.pluto.om.common.SecurityRoleRefSet;
import org.apache.pluto.om.common.SecurityRoleRefSetCtrl;
import org.apache.pluto.om.common.SecurityRoleSet;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.om.servlet.ServletDefinition;
import org.apache.pluto.om.servlet.ServletDefinitionCtrl;
import org.apache.pluto.om.servlet.ServletDefinitionListCtrl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.InputSource;

/**
 * First version of a simple portlet deploy tool for the Cocoon Portal.
 * It works very similar to the deploy tool of the Pluto project (most
 * code is taken and improved from the Pluto tool!).
 * The only difference is that this deploy tool does not copy the taglib
 * definition for the portlet tags, so you have to have these in your
 * portlet war already!
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: Deploy.java,v 1.4 2004/06/23 14:14:02 cziegeler Exp $
 */
public class Deploy {

    //attributes for the web.xml creation for portlets
    public final static String WEB_PORTLET_PUBLIC_ID = "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";
    public final static String WEB_PORTLET_DTD = "http://java.sun.com/dtd/web-app_2_3.dtd";

    private static boolean debug = false;
    private static final String webInfDir = File.separatorChar + "WEB-INF" + File.separatorChar;

    /**
     * Deploy the archive
     * Unpack the archive in the servlet engine context directory
     */
    public static void deployArchive(final String webAppsDir, 
                                     final String warFile,
                                     final String warFileName)
    throws IOException {
        System.out.println("Deploying '" + warFileName + "' ...");

        final String destination = webAppsDir + warFileName;

        if ( debug) {
            System.out.println("  unpacking '" + warFile + "' ...");
        }
        final JarFile jarFile = new JarFile(warFile);
        final Enumeration files = jarFile.entries();
        while (files.hasMoreElements()) {
            JarEntry entry = (JarEntry) files.nextElement();

            File file = new File(destination, entry.getName());
            File dirF = new File(file.getParent());
            dirF.mkdirs();
            if (entry.isDirectory()) {
                file.mkdirs();
            } else {
                byte[] buffer = new byte[1024];
                int length = 0;
                InputStream fis = jarFile.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(file);
                while ((length = fis.read(buffer)) >= 0) {
                    fos.write(buffer, 0, length);
                }
                fos.close();
            }

        }

        if ( debug ) {
            System.out.println("Finished!");
        }
    }

    /**
     * Helper method to setup the mapping
     */
    private static Mapping getMapping(final String uri) 
    throws IOException {
        final String mappingResource = uri.substring(uri.indexOf("://")+2);
        final Mapping mapping = new Mapping();
     
        final InputSource is = new InputSource(Deploy.class.getResourceAsStream(mappingResource));
        try {
            mapping.loadMapping( is );
        } catch (Exception e) {
            throw new IOException("Failed to load mapping file " + mappingResource);
        }
        return mapping;
    }
    
    /**
     * Prepare the web archive of the portlet web app
     */
    public static void prepareWebArchive(String webAppsDir, String webModule)
    throws Exception {
        System.out.println("Preparing web archive '" + webModule + "' ...");

        // get portlet xml mapping file
        Mapping mappingPortletXml = getMapping(PortletDefinitionRegistryImpl.PORTLET_MAPPING);
        // get web xml mapping file
        Mapping mappingWebXml = getMapping(PortletDefinitionRegistryImpl.WEBXML_MAPPING);

        File portletXml = new File(webAppsDir + webModule + webInfDir + "portlet.xml");
        File webXml = new File(webAppsDir + webModule + webInfDir + "web.xml");

        Unmarshaller unmarshaller = new Unmarshaller(mappingPortletXml);
        PortletApplicationDefinitionImpl portletApp =
            (PortletApplicationDefinitionImpl)unmarshaller.unmarshal(new FileReader(portletXml));

        // refill structure with necessary information
        Vector structure = new Vector();
        structure.add(webModule);
        structure.add(null);
        structure.add(null);
        portletApp.preBuild(structure);

        if (debug) {
            System.out.println(portletApp);
        }

        // now generate web part

        WebApplicationDefinitionImpl webApp = null;

        if (webXml.exists()) {
            Unmarshaller unmarshallerWeb = new Unmarshaller(mappingWebXml);
			unmarshallerWeb.setIgnoreExtraElements(true);
            webApp =
                (WebApplicationDefinitionImpl) unmarshallerWeb.unmarshal(
                    new FileReader(webXml));
        } else {
            webApp = new WebApplicationDefinitionImpl();
            DisplayNameImpl dispName = new DisplayNameImpl();
            dispName.setDisplayName(webModule);
            dispName.setLocale(Locale.ENGLISH);
            DisplayNameSetImpl dispSet = new DisplayNameSetImpl();
            dispSet.add(dispName);
            webApp.setDisplayNames(dispSet);
            DescriptionImpl desc = new DescriptionImpl();
            desc.setDescription("Automated generated Application Wrapper");
            desc.setLocale(Locale.ENGLISH);
            DescriptionSetImpl descSet = new DescriptionSetImpl();
            descSet.add(desc);
            webApp.setDescriptions(descSet);
        }

        ControllerFactory controllerFactory = new ControllerFactoryImpl();

        ServletDefinitionListCtrl servletDefinitionSetCtrl =
            (ServletDefinitionListCtrl) controllerFactory.get(
                webApp.getServletDefinitionList());
        Collection servletMappings = webApp.getServletMappings();

        Iterator portlets = portletApp.getPortletDefinitionList().iterator();
        while (portlets.hasNext()) {

            PortletDefinition portlet = (PortletDefinition) portlets.next();

            if ( debug ) {
                System.out.println("  Portlet: " + portlet.getId());
            }
            // check if already exists
            ServletDefinition servlet =
                webApp.getServletDefinitionList().get(portlet.getName());
            if (servlet != null) {
                if (!servlet
                    .getServletClass()
                    .equals("org.apache.pluto.core.PortletServlet")) {
                    System.out.println(
                        "Note: Replaced already existing the servlet with the name '"
                            + portlet.getName()
                            + "' with the wrapper servlet.");
                }
                ServletDefinitionCtrl _servletCtrl =
                    (ServletDefinitionCtrl) controllerFactory.get(servlet);
                _servletCtrl.setServletClass(
                    "org.apache.pluto.core.PortletServlet");
            } else {
                servlet =
                    servletDefinitionSetCtrl.add(
                        portlet.getName(),
                        "org.apache.pluto.core.PortletServlet");
            }

            ServletDefinitionCtrl servletCtrl =
                (ServletDefinitionCtrl) controllerFactory.get(servlet);

            DisplayNameImpl dispName = new DisplayNameImpl();
            dispName.setDisplayName(portlet.getName() + " Wrapper");
            dispName.setLocale(Locale.ENGLISH);
            DisplayNameSetImpl dispSet = new DisplayNameSetImpl();
            dispSet.add(dispName);
            servletCtrl.setDisplayNames(dispSet);
            DescriptionImpl desc = new DescriptionImpl();
            desc.setDescription("Automated generated Portlet Wrapper");
            desc.setLocale(Locale.ENGLISH);
            DescriptionSetImpl descSet = new DescriptionSetImpl();
            descSet.add(desc);
            servletCtrl.setDescriptions(descSet);
            ParameterSet parameters = servlet.getInitParameterSet();

            ParameterSetCtrl parameterSetCtrl =
                (ParameterSetCtrl) controllerFactory.get(parameters);

            Parameter parameter1 = parameters.get("portlet-class");
            if (parameter1 == null) {
                parameterSetCtrl.add(
                    "portlet-class",
                    portlet.getClassName());
            } else {
                ParameterCtrl parameterCtrl =
                    (ParameterCtrl) controllerFactory.get(parameter1);
                parameterCtrl.setValue(portlet.getClassName());

            }
            Parameter parameter2 = parameters.get("portlet-guid");
            if (parameter2 == null) {
                parameterSetCtrl.add(
                    "portlet-guid",
                    portlet.getId().toString());
            } else {
                ParameterCtrl parameterCtrl =
                    (ParameterCtrl) controllerFactory.get(parameter2);
                parameterCtrl.setValue(portlet.getId().toString());

            }

            boolean found = false;
            Iterator mappings = servletMappings.iterator();
            while (mappings.hasNext()) {
                ServletMapping servletMapping =
                    (ServletMapping) mappings.next();
                if (servletMapping
                    .getServletName()
                    .equals(portlet.getName())) {
                    found = true;
                    servletMapping.setUrlPattern(
                        "/" + portlet.getName().replace(' ', '_') + "/*");
                }
            }
            if (!found) {
                ServletMapping servletMapping =
                    new ServletMapping();
                servletMapping.setServletName(portlet.getName());
                servletMapping.setUrlPattern(
                    "/" + portlet.getName().replace(' ', '_') + "/*");
                servletMappings.add(servletMapping);
            }

            SecurityRoleRefSet servletSecurityRoleRefs =
                ((ServletDefinitionImpl)servlet).getInitSecurityRoleRefSet();

            SecurityRoleRefSetCtrl servletSecurityRoleRefSetCtrl =
                (SecurityRoleRefSetCtrl) controllerFactory.get(
                    servletSecurityRoleRefs);

            SecurityRoleSet webAppSecurityRoles = webApp.getSecurityRoles();
                
            SecurityRoleRefSet portletSecurityRoleRefs =
                portlet.getInitSecurityRoleRefSet();

            Iterator p = portletSecurityRoleRefs.iterator();

            while (p.hasNext()) {
                SecurityRoleRef portletSecurityRoleRef =
                    (SecurityRoleRef) p.next();
                
                if (	portletSecurityRoleRef.getRoleLink()== null
                    &&		
                        webAppSecurityRoles.get(portletSecurityRoleRef.getRoleName())==null
                ){
                    System.out.println(
                        "Note: The web application has no security role defined which matches the role name \""
                            + portletSecurityRoleRef.getRoleName()
                            + "\" of the security-role-ref element defined for the wrapper-servlet with the name '"
                            + portlet.getName()
                            + "'.");
                    break;						
                }
                SecurityRoleRef servletSecurityRoleRef =
                    servletSecurityRoleRefs.get(
                        portletSecurityRoleRef.getRoleName());
                if (null != servletSecurityRoleRef) {
                    System.out.println(
                        "Note: Replaced already existing element of type <security-role-ref> with value \""
                            + portletSecurityRoleRef.getRoleName()
                            + "\" for subelement of type <role-name> for the wrapper-servlet with the name '"
                            + portlet.getName()
                            + "'.");
                    servletSecurityRoleRefSetCtrl.remove(
                        servletSecurityRoleRef);
                }
                servletSecurityRoleRefSetCtrl.add(portletSecurityRoleRef);
            }

        }

        TagDefinition portletTagLib = new TagDefinition();
        Collection taglibs = webApp.getCastorTagDefinitions();
        taglibs.add(portletTagLib); 
        
        if (debug) {
            System.out.println(webApp);
        }

        OutputFormat of = new OutputFormat();
        of.setIndenting(true);
        of.setIndent(4); // 2-space indention
        of.setLineWidth(16384);
        // As large as needed to prevent linebreaks in text nodes
        of.setDoctype(WEB_PORTLET_PUBLIC_ID, WEB_PORTLET_DTD);

        FileWriter writer =
            new FileWriter(webAppsDir + webModule + 
                                           System.getProperty("file.separator") + "WEB-INF"+
                                           System.getProperty("file.separator") + "web.xml");
        XMLSerializer serializer = new XMLSerializer(writer, of);
        try {
            Marshaller marshaller = new Marshaller(serializer.asDocumentHandler());
            marshaller.setMapping(mappingWebXml);
            marshaller.marshal(webApp);
        } finally {
            writer.close();
        }

        if ( debug ) {
            System.out.println("Finished!");
        }
    }

    public static void main(String args[]) {
        String warFile;
        String webAppsDir;
        
        final Options options = new Options();

        Option o;
        o = new Option("w", true, "webapps directory");
        o.setRequired(true);
        o.setArgName("WEBAPPS_DIR");
        options.addOption(o);
        
        o = new Option("p", true, "web archive containing the portlet(s)");
        o.setRequired(true);
        o.setArgName("PORTLET_WAR");
        options.addOption(o);
        
        options.addOption("d", "debug", false, "Show debug messages.");
        
        try {
            final CommandLineParser parser = new PosixParser();
            final CommandLine cmd = parser.parse( options, args);

            // first test/turn on debug
            debug = cmd.hasOption("d");
            if (debug) {
                for(int i=0; i<args.length;i++) {
                    System.out.println( "args["+ i +"]:" + args[i]);                
                }
            }
            
            
            webAppsDir = cmd.getOptionValue("w");
            if (!webAppsDir.endsWith(File.separator))
                webAppsDir += File.separatorChar;
    
            //portalImplWebDir = cmd.getOptionValue("X");
            //if (!portalImplWebDir.endsWith(File.separator))
            //    portalImplWebDir += File.separatorChar;
    
            warFile = cmd.getOptionValue("p");
        } catch( ParseException exp ) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "deploy", options, true );
            System.exit(1);
            return;
        }

        // let's do some tests on the war file name
        String warFileName = warFile;
        if (warFileName.indexOf("/") != -1) {
            warFileName = warFileName.substring(warFileName.lastIndexOf("/") + 1);
        }
        if (warFileName.indexOf(File.separatorChar) != -1) {
            warFileName = warFileName.substring(warFileName.lastIndexOf(File.separatorChar) + 1);
        }
        if (warFileName.endsWith(".war")) {
            warFileName = warFileName.substring(0, warFileName.lastIndexOf("."));
        }

        try {
            deployArchive(webAppsDir, warFile, warFileName);

            prepareWebArchive(webAppsDir, warFileName);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            return;
        }
        System.exit(0);
    }

}
