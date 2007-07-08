/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.generation;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.sitemap.DisposableSitemapComponent;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.xml.XMLConsumer;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * This generator generates - using Apache POI - a Gnumeric compliant XML
 * Document from a Microsoft Excel Workbook.
 *
 * <h3>Bean Definition</h3>
 * &lt;map:generator type="xls" src="org.apache.cocoon.generation.HSSFGenerator"&gt;
 *   &lt;uri&gt;http://www.gnome.org/gnumeric/v7&lt;/uri&gt;
 *   &lt;prefix&gt;gmr&lt;/prefix&gt;
 *   &lt;formatting&gt;false&lt;/formatting&gt;
 * &lt;/map:generator&gt;
 *
 * <h3>Sitemap Use</h3>
 * &lt;map:generate type="xls" src="spreadsheet.xls"/&gt;
 *
 * <p>You can set the parameter <code>formatting</code> to <code>true</code>
 * in order to receive not only the data but also the formatting information
 * of the workbook.</p>
 *
 * @version $Id$
 */
public class HSSFGenerator extends AbstractLogEnabled implements Generator, DisposableSitemapComponent {

    public static final String NAMESPACE_PREFIX = "gmr";
    public static final String NAMESPACE_URI = "http://www.gnome.org/gnumeric/v7";
    private static final boolean FORMATTING = false;

    private static final String CONF_NAMESPACE_URI = "uri";
    private static final String CONF_NAMESPACE_PREFIX = "prefix";
    private static final String CONF_FORMATTING = "formatting";

    private String uri = NAMESPACE_URI;
    private String prefix = NAMESPACE_PREFIX;
    private boolean formatting = FORMATTING;
    private final AttributesImpl attr;

    protected Source inputSource;

    /** The consumer. */
    protected XMLConsumer consumer;

    /** The source resolver. */
    protected SourceResolver resolver;

    public HSSFGenerator() {
        this.attr = new AttributesImpl();
    }

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        this.uri = par.getParameter(CONF_NAMESPACE_URI, this.uri);
        this.prefix = par.getParameter(CONF_NAMESPACE_PREFIX, this.prefix);
        this.formatting = par.getParameterAsBoolean(CONF_FORMATTING, this.formatting);
        this.resolver = resolver;
        
        try {
            this.inputSource = this.resolver.resolveURI(src);
        } catch (SourceException se) {
            throw SourceUtil.handle("Error resolving '" + src + "'.", se);
        }
    }

    /**
     * @see org.apache.cocoon.sitemap.DisposableSitemapComponent#dispose()
     */
    public void dispose() {
        if (this.inputSource != null) {
            this.resolver.release(this.inputSource);
            this.inputSource = null;
        }
        this.resolver = null;
        this.consumer = null;
    }

    /**
     * Generate XML data.
     */
    public void generate() throws SAXException, IOException {
        HSSFWorkbook workbook =
                new HSSFWorkbook(this.inputSource.getInputStream());
        writeXML(workbook);
    }


    /**
     * Writes out the workbook data as XML, without formatting information
     */
    private void writeXML(HSSFWorkbook workbook) throws SAXException {
        this.consumer.startDocument();
        start("Workbook");
        start("SheetNameIndex");
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            start("SheetName");
            data(workbook.getSheetName(i));
            end("SheetName");
        }
        end("SheetNameIndex");
        start("Sheets");
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            HSSFSheet sheet = workbook.getSheetAt(i);
            start("Sheet");
            start("Name");
            data(workbook.getSheetName(i));
            end("Name");
            start("MaxCol");
            data(Integer.toString(getMaxCol(sheet)));
            end("MaxCol");
            start("MaxRow");
            data(Integer.toString(sheet.getLastRowNum()));
            end("MaxRow");
            if (formatting) {
                writeStyles(workbook, sheet);
            }

            start("Cells");
            final Iterator rows = sheet.rowIterator();
            while (rows.hasNext()) {
                final HSSFRow row = (HSSFRow) rows.next();
                final Iterator cells = row.cellIterator();
                while (cells.hasNext()) {
                    final HSSFCell cell = (HSSFCell) cells.next();
                    attribute("Row", Integer.toString(row.getRowNum()));
                    attribute("Col", Short.toString(cell.getCellNum()));
                    attribute("ValueType", getValueType(cell.getCellType()));
                    start("Cell");
                    data(getValue(cell));
                    end("Cell");
                }
            }
            end("Cells");

            end("Sheet");
        }
        end("Sheets");
        end("Workbook");
        this.consumer.endDocument();
    }

    /**
     * Returns the max column index of the given sheet
     * @param sheet
     * @return the max column index
     */
    private int getMaxCol(HSSFSheet sheet) {
        int max = -1;
        HSSFRow row = null;
        Iterator rows = sheet.rowIterator();
        while (rows.hasNext()) {
            row = (HSSFRow) rows.next();
            int lastNum = row.getLastCellNum();
            if (lastNum > max) {
                max = lastNum;
            }
        }
        return max;
    }

    /**
     * Returns the Gnumeric cell type.
     * @param cellType	POI cell type
     * @return the Gnumeric cell type.
     */
    private String getValueType(int cellType) {
        switch (cellType) {
            case HSSFCell.CELL_TYPE_BLANK:
                return "10";
            case HSSFCell.CELL_TYPE_BOOLEAN:
                return "20";
            case HSSFCell.CELL_TYPE_NUMERIC:
                return "40";
            case HSSFCell.CELL_TYPE_ERROR:
                return "50";
            case HSSFCell.CELL_TYPE_FORMULA:
            case HSSFCell.CELL_TYPE_STRING:
            default:
                return "60";
        }
    }

    /**
     * Returns the cell value.
     * @param cell	POI cell
     * @return the cell value
     */
    private String getValue(HSSFCell cell) {
        switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_BLANK:
                return "";
            case HSSFCell.CELL_TYPE_BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case HSSFCell.CELL_TYPE_NUMERIC:
                return Double.toString(cell.getNumericCellValue());
            case HSSFCell.CELL_TYPE_ERROR:
                return "#ERR" + cell.getErrorCellValue();
            case HSSFCell.CELL_TYPE_FORMULA:
            case HSSFCell.CELL_TYPE_STRING:
            default:
                return cell.getStringCellValue();
        }
    }

    /**
     * Writes out the workbook data as XML, with formatting information
     */
    private void writeStyles(HSSFWorkbook workbook, HSSFSheet sheet)
    throws SAXException {
        start("Styles");
        HSSFRow row = null;
        HSSFCell cell = null;
        Iterator cells = null;
        Iterator rows = sheet.rowIterator();
        while (rows.hasNext()) {
            row = (HSSFRow) rows.next();
            cells = row.cellIterator();
            while (cells.hasNext()) {
                cell = (HSSFCell) cells.next();
                attribute("startRow", Integer.toString(row.getRowNum()));
                attribute("endRow", Integer.toString(row.getRowNum()));
                attribute("startCol", Short.toString(cell.getCellNum()));
                attribute("endCol", Short.toString(cell.getCellNum()));
                start("StyleRegion");
                HSSFCellStyle style = cell.getCellStyle();
                attribute("HAlign", Integer.toString(style.getAlignment()));
                attribute("VAlign", Integer.toString(style.getVerticalAlignment()));
                attribute("WrapText", ((style.getWrapText()) ? "1" : "0"));
                attribute("Orient", Integer.toString(style.getRotation()));
                attribute("Indent", Integer.toString(style.getIndention()));
                attribute("Locked", ((style.getLocked()) ? "1" : "0"));
                attribute("Hidden", ((style.getHidden()) ? "1" : "0"));
                attribute("Fore", workbook.getCustomPalette().getColor(style.getFillForegroundColor()).getHexString());
                attribute("Back", workbook.getCustomPalette().getColor(style.getFillBackgroundColor()).getHexString());
                attribute("PatternColor", Integer.toString(style.getFillPattern())); // TODO
                attribute("Format", "General"); // TODO
                start("Style");
                HSSFFont font = workbook.getFontAt(style.getFontIndex());
                attribute("Unit", Short.toString(font.getFontHeightInPoints()));
                attribute("Bold", Short.toString(font.getBoldweight()));
                attribute("Italic", ((font.getItalic()) ? "1" : "0"));
                attribute("Unterline", Integer.toString(font.getUnderline()));
                attribute("StrikeThrough", ((font.getStrikeout()) ? "1" : "0"));
                start("Font");
                data(font.getFontName());
                end("Font");
                end("Style");
                end("StyleRegion");
            }
        }
        end("Styles");
    }

    //
    // Utility methods
    //

    /**
     * Adds an attribute with the given name and value.
     */
    private void attribute(String name, String value) {
        attr.addAttribute("", name, name, "CDATA", value);
    }

    /**
     * Starts an element with the given local name.
     * @param name	local name of the element
     * @throws SAXException
     */
    private void start(String name) throws SAXException {
        consumer.startElement(uri, name, prefix + ":" + name, attr);
        attr.clear();
    }

    /**
     * Ends the given element.
     * @param name local name of the element
     * @throws SAXException
     */
    private void end(String name) throws SAXException {
        consumer.endElement(uri, name, prefix + ":" + name);
    }

    /**
     * Writes the given element data.
     * @param data
     * @throws SAXException
     */
    private void data(String data) throws SAXException {
        consumer.characters(data.toCharArray(), 0, data.length());
    }

    /**
     * @see org.apache.cocoon.xml.XMLProducer#setConsumer(org.apache.cocoon.xml.XMLConsumer)
     */
    public void setConsumer(XMLConsumer consumer) {
        this.consumer = consumer;
    }

    /**
     * Set the value
     * 
     * @param resolver
     *            the resolver to set
     */
    public void setResolver(SourceResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @param prefix the prefix to set
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @return the formatting
     */
    public boolean isFormatting() {
        return formatting;
    }

    /**
     * @param formatting the formatting to set
     */
    public void setFormatting(boolean formatting) {
        this.formatting = formatting;
    }
}
