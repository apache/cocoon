/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.generation.asciiart;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A simple AsciiArt text SVG XML generator.
 *
 * @author <a href="mailto:huber@apache.org">Bernhard Huber</a>
 * @version CVS $Id: AsciiArtSVGGenerator.java,v 1.6 2004/06/11 20:32:19 vgritsenko Exp $
 * @since Cocoon 2.1, 22 December 2002
 */
public class AsciiArtSVGGenerator
    extends AbstractGenerator
    implements CacheableProcessingComponent {

    /**
     * The input source
     */
    protected Source inputSource;

    private AttributesImpl attributes = null;
    private AsciiArtPad asciiArtPad;

    //private String PREFIX = "svg";
    private String PREFIX = "";
    private String URI = "http://www.w3.org/2000/svg";

    /** default SVG line attributes
     */
    private final String DEFAULT_LINE_ATTRIBUTE = "stroke:black; stroke-width:1.5";

    /** default SVG text attribute
     */
    private final String DEFAULT_TEXT_ATTRIBUTE = "font-size: 12; font-family:Times Roman; fill:blue;";

    private String lineAttribute = DEFAULT_LINE_ATTRIBUTE;
    private String textAttribute = DEFAULT_TEXT_ATTRIBUTE;


    final int DEFAULT_X_GRID = 10;
    final int DEFAULT_Y_GRID = 12;
    private int xGrid = DEFAULT_X_GRID;
    private int yGrid = DEFAULT_Y_GRID;

    /**
     * Setup the AsciiArtSVG generator.
     * Try to get the last modification date of the source for caching.
     *
     *@param  resolver                 Cocoon's resolver
     *@param  objectModel              Cocoon's objectModel
     *@param  src                      generator's src attribute
     *@param  par                      sitemap parameters
     *@exception  ProcessingException  setup fails
     *@exception  SAXException         sax generation fails
     *@exception  IOException          general io fails
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        try {
            this.inputSource = resolver.resolveURI(src);
        } catch (SourceException se) {
            throw SourceUtil.handle("Error during resolving of '" + src + "'.", se);
        }

        // Setup lineAttribute
        lineAttribute = par.getParameter("line-attribute", DEFAULT_LINE_ATTRIBUTE);
        // Setup textAttribute
        textAttribute = par.getParameter("text-attribute", DEFAULT_TEXT_ATTRIBUTE);

        xGrid = par.getParameterAsInteger("x-grid", DEFAULT_X_GRID);
        yGrid = par.getParameterAsInteger("y-grid", DEFAULT_Y_GRID);
    }


    /**
     * Recycle this component.
     * All instance variables are set to <code>null</code>.
     */
    public void recycle() {
        if (null != this.inputSource) {
            super.resolver.release(this.inputSource);
            this.inputSource = null;
        }
        super.recycle();
    }


    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public java.io.Serializable getKey() {
        return this.inputSource.getURI();
    }


    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        return this.inputSource.getValidity();
    }


    /**
     * Generate XML data.
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Source " + super.source +
                                  " resolved to " + this.inputSource.getURI());
            }

            // read the ascii art
            String[] asciiArt = readAsciiArt();
            // setup ascii art pad
            asciiArtPad = new AsciiArtPad();
            asciiArtPad.setXGrid(this.xGrid);
            asciiArtPad.setYGrid(this.yGrid);

            // build the ascii art
            AsciiArtPad.AsciiArtPadBuilder builder = new AsciiArtPad.AsciiArtPadBuilder(asciiArtPad);
            builder.build(asciiArt);
            attributes = new AttributesImpl();

            // start the document
            this.contentHandler.startDocument();
            this.contentHandler.startPrefixMapping(PREFIX, URI);

            // generate root element
            attributes.clear();
            // set svg attributes
            addAttribute("width", String.valueOf(asciiArtPad.getXGrid() * asciiArtPad.getWidth()));
            addAttribute("height", String.valueOf(asciiArtPad.getYGrid() * asciiArtPad.getHeight()));
            startElement("svg", attributes);

            // generate svg g, and path elements
            attributes.clear();
            // set line attributes
            addAttribute("style", this.lineAttribute);
            startElement("g", attributes);
            generateSVGLineElements();
            endElement("g");

            // generate svg g, and text elements
            attributes.clear();
            // set text attributes
            addAttribute("style", this.textAttribute);
            startElement("g", attributes);
            generateSVGTextElements();
            endElement("g");

            // end root element, document
            endElement("svg");
            this.contentHandler.endPrefixMapping(PREFIX);
            this.contentHandler.endDocument();
        } catch (SAXException e) {
            SourceUtil.handleSAXException(this.inputSource.getURI(), e);
        }
    }


    /**
     *  Read the ascii art from the input source.
     *
     *@return                  String[] describing the ascii art
     *@exception  IOException  reading the ascii art fails
     */
    protected String[] readAsciiArt() throws IOException {
        InputStream is = null;
        BufferedReader br = null;
        try {
            is = this.inputSource.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            String line;
            List lines = new ArrayList();
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            String[] asciiArt = (String[]) lines.toArray(new String[0]);
            return asciiArt;
        } catch (SourceException se) {
            throw new CascadingIOException("Cannot get input stream", se);
        } finally {
            if (is != null) {
                is.close();
            }
            if (br != null) {
                br.close();
            }
        }
    }


    /**
     *  Generate SVG path elements.
     *  The SVG path elements are generated from ascii art lines.
     *
     *@throws  SAXException  iff SAX generation fails.
     */
    protected void generateSVGLineElements() throws SAXException {
        //NumberFormat nf = NumberFormat.getInstance(Locale.US);
        //nf.setGroupingIsUsed( false );
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("##0.0##", dfs);
        Iterator i = asciiArtPad.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof AsciiArtPad.AsciiArtLine) {
                AsciiArtPad.AsciiArtLine aal = (AsciiArtPad.AsciiArtLine) o;
                double mx = aal.getXStart();
                double my = aal.getYStart();
                double lx = aal.getXEnd();
                double ly = aal.getYEnd();

                attributes.clear();
                addAttribute("d",
                        "M " + df.format(mx) + " " + df.format(my) + " " +
                        "L " + df.format(lx) + " " + df.format(ly));
                startElement("path", attributes);
                endElement("path");
            }
        }
    }


    /**
     *  Generate SVG text elements.
     *  The SVG text elements are generated from ascii art string.
     *
     *@throws  SAXException  iff SAX generation fails.
     */
    protected void generateSVGTextElements() throws SAXException {
        //NumberFormat nf = NumberFormat.getInstance(Locale.US);
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("##0.0##", dfs);
        Iterator i = asciiArtPad.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof AsciiArtPad.AsciiArtString) {
                AsciiArtPad.AsciiArtString aas = (AsciiArtPad.AsciiArtString) o;
                double x = aas.getX();
                double y = aas.getY();
                attributes.clear();
                addAttribute("x", df.format(x));
                addAttribute("y", df.format(y));
                startElement("text", attributes);
                characters(aas.getS());
                endElement("text");
            }
        }
    }


    /**
     *  SAX startElement helper
     *
     *@param  nodeName       name of the element's name
     *@param  attributes     of the node
     *@throws  SAXException  iff SAX generation fails
     */
    protected void startElement(String nodeName, Attributes attributes) throws SAXException {
        if (PREFIX.length() > 0) {
            this.contentHandler.startElement(URI, nodeName, PREFIX + ":" + nodeName, attributes);
        } else {
            this.contentHandler.startElement(URI, nodeName, nodeName, attributes);
        }
    }


    /**
     *  SAX character helper
     *
     *@param  s              Description of the Parameter
     *@throws  SAXException  iff SAX generation fails
     */
    protected void characters(String s) throws SAXException {
        if (s != null) {
            char[] stringCharacters = s.toCharArray();
            this.contentHandler.characters(stringCharacters, 0, stringCharacters.length);
        }
    }


    /**
     *  SAX endElement helper
     *
     *@param  nodeName       name of the element's name
     *@throws  SAXException  iff SAX generation fails
     */
    protected void endElement(String nodeName) throws SAXException {
        if (PREFIX.length() > 0) {
            this.contentHandler.endElement(URI, nodeName, PREFIX + ":" + nodeName);
        } else {
            this.contentHandler.endElement(URI, nodeName, nodeName);
        }
    }


    /**
     *  Adds a feature to the Attribute attribute of the MailXMLSerializer
     *  object
     *
     *@param  nodeName   name of the attriute's name
     *@param  nodeValue  value of the attribute
     */
    protected void addAttribute(String nodeName, String nodeValue) {
        attributes.addAttribute("", nodeName, nodeName, "CDATA", nodeValue);
    }
}

