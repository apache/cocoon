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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 *  A drawing ascii art pad.
 *
 * @author huber@apache.org
 * @since 18. Dezember 2002
 * @version CVS $Id: AsciiArtPad.java,v 1.5 2004/03/05 13:01:38 bdelacretaz Exp $
 */
public class AsciiArtPad {

    private int width;
    private int height;

    /**
     * List of AsciiArt elements
     */
    private List pad;

    private double xGrid;
    private double yGrid;


    /**
     *Constructor for the AsciiArtPad object
     */
    public AsciiArtPad() {
        pad = new ArrayList();
    }


    /**
     *Constructor for the AsciiArtPad object
     *
     *@param  w  Description of the Parameter
     *@param  h  Description of the Parameter
     */
    public AsciiArtPad(int w, int h) {
        width = w;
        height = h;

        pad = new ArrayList();
    }


    /**
     *  Sets the width attribute of the AsciiArtPad object
     *
     *@param  width  The new width value
     */
    public void setWidth(int width) {
        this.width = width;
    }


    /**
     *  Sets the height attribute of the AsciiArtPad object
     *
     *@param  height  The new height value
     */
    public void setHeight(int height) {
        this.height = height;
    }


    /**
     *  Sets the xGrid attribute of the AsciiArtPad object
     *
     *@param  xGrid  The new xGrid value
     */
    public void setXGrid(double xGrid) {
        this.xGrid = xGrid;
    }


    /**
     *  Sets the yGrid attribute of the AsciiArtPad object
     *
     *@param  yGrid  The new yGrid value
     */
    public void setYGrid(double yGrid) {
        this.yGrid = yGrid;
    }


    /**
     *  Gets the width attribute of the AsciiArtPad object
     *
     *@return    The width value
     */
    public int getWidth() {
        return width;
    }


    /**
     *  Gets the height attribute of the AsciiArtPad object
     *
     *@return    The height value
     */
    public int getHeight() {
        return height;
    }


    /**
     *  Gets the xGrid attribute of the AsciiArtPad object
     *
     *@return    The xGrid value
     */
    public double getXGrid() {
        return xGrid;
    }


    /**
     *  Gets the yGrid attribute of the AsciiArtPad object
     *
     *@return    The yGrid value
     */
    public double getYGrid() {
        return yGrid;
    }


    /**
     *  Add a AsciiArtElement
     *
     *@param  o  the AsciiArtElement object
     */
    public void add(Object o) {
        pad.add(o);
    }


    /**
     *  Iterator of AsciiArtPad
     *
     *@return    Iterator iterating over all AsciiArtElements
     */
    public Iterator iterator() {
        return pad.iterator();
    }


    /**
     *  An AsciiArtElement describing a line.
     *
     */
    public static class AsciiArtLine implements AsciiArtElement {
        double xStart;
        double yStart;
        double xEnd;
        double yEnd;


        /**
         *Constructor for the AsciiArtLine object
         *
         *@param  start  Description of the Parameter
         *@param  end    Description of the Parameter
         */
        public AsciiArtLine(AsciiArtCoordinate start, AsciiArtCoordinate end) {
            xStart = start.getXDouble();
            yStart = start.getYDouble();
            xEnd = end.getXDouble();
            yEnd = end.getYDouble();
        }


        /**
         *  Sets the xStart attribute of the AsciiArtLine object
         *
         *@param  xStart  The new xStart value
         */
        public void setXStart(double xStart) {
            this.xStart = xStart;
        }


        /**
         *  Sets the yStart attribute of the AsciiArtLine object
         *
         *@param  yStart  The new yStart value
         */
        public void setYStart(double yStart) {
            this.yStart = yStart;
        }


        /**
         *  Sets the xEnd attribute of the AsciiArtLine object
         *
         *@param  xEnd  The new xEnd value
         */
        public void setXEnd(double xEnd) {
            this.xEnd = xEnd;
        }


        /**
         *  Sets the yEnd attribute of the AsciiArtLine object
         *
         *@param  yEnd  The new yEnd value
         */
        public void setYEnd(double yEnd) {
            this.yEnd = yEnd;
        }


        /**
         *  Gets the xStart attribute of the AsciiArtLine object
         *
         *@return    The xStart value
         */
        public double getXStart() {
            return xStart;
        }


        /**
         *  Gets the yStart attribute of the AsciiArtLine object
         *
         *@return    The yStart value
         */
        public double getYStart() {
            return yStart;
        }


        /**
         *  Gets the xEnd attribute of the AsciiArtLine object
         *
         *@return    The xEnd value
         */
        public double getXEnd() {
            return xEnd;
        }


        /**
         *  Gets the yEnd attribute of the AsciiArtLine object
         *
         *@return    The yEnd value
         */
        public double getYEnd() {
            return yEnd;
        }


        /**
         *  Descriptive string
         *
         *@return    String
         */
        public String toString() {
            String s =
                    "[xStart:" + String.valueOf(xStart) + "]" +
                    "[yStart:" + String.valueOf(yStart) + "]" +
                    "[xEnd:" + String.valueOf(xEnd) + "]" +
                    "[yEnd:" + String.valueOf(yEnd) + "]";
            return s;
        }
    }


    /**
     *  An AsciiArtElement describing a rectangle.
     *
     */
    public static class AsciiArtRect implements AsciiArtElement {
        double xUpperLeft;
        double yUpperLeft;
        double xLowerRight;
        double yLowerRight;


        /**
         *Constructor for the AsciiArtRect object
         *
         *@param  upperLeft   Description of the Parameter
         *@param  lowerRight  Description of the Parameter
         */
        public AsciiArtRect(AsciiArtCoordinate upperLeft, AsciiArtCoordinate lowerRight) {
            xUpperLeft = upperLeft.getXDouble();
            yUpperLeft = upperLeft.getYDouble();
            xLowerRight = lowerRight.getXDouble();
            yLowerRight = lowerRight.getYDouble();
        }


        /**
         *  Sets the xUpperLeft attribute of the AsciiArtRect object
         *
         *@param  xUpperLeft  The new xUpperLeft value
         */
        public void setXUpperLeft(double xUpperLeft) {
            this.xUpperLeft = xUpperLeft;
        }


        /**
         *  Sets the yUpperLeft attribute of the AsciiArtRect object
         *
         *@param  yUpperLeft  The new yUpperLeft value
         */
        public void setYUpperLeft(double yUpperLeft) {
            this.yUpperLeft = yUpperLeft;
        }


        /**
         *  Sets the xLowerRight attribute of the AsciiArtRect object
         *
         *@param  xLowerRight  The new xLowerRight value
         */
        public void setXLowerRight(double xLowerRight) {
            this.xLowerRight = xLowerRight;
        }


        /**
         *  Sets the yLowerRight attribute of the AsciiArtRect object
         *
         *@param  yLowerRight  The new yLowerRight value
         */
        public void setYLowerRight(double yLowerRight) {
            this.yLowerRight = yLowerRight;
        }


        /**
         *  Gets the xUpperLeft attribute of the AsciiArtRect object
         *
         *@return    The xUpperLeft value
         */
        public double getXUpperLeft() {
            return xUpperLeft;
        }


        /**
         *  Gets the yUpperLeft attribute of the AsciiArtRect object
         *
         *@return    The yUpperLeft value
         */
        public double getYUpperLeft() {
            return yUpperLeft;
        }


        /**
         *  Gets the xLowerRight attribute of the AsciiArtRect object
         *
         *@return    The xLowerRight value
         */
        public double getXLowerRight() {
            return xLowerRight;
        }


        /**
         *  Gets the yLowerRight attribute of the AsciiArtRect object
         *
         *@return    The yLowerRight value
         */
        public double getYLowerRight() {
            return yLowerRight;
        }


        /**
         *  Gets the width attribute of the AsciiArtRect object
         *
         *@return    The width value
         */
        public double getWidth() {
            return Math.abs(xUpperLeft - xLowerRight);
        }


        /**
         *  Gets the height attribute of the AsciiArtRect object
         *
         *@return    The height value
         */
        public double getHeight() {
            return Math.abs(yUpperLeft - yLowerRight);
        }


        /**
         *  Descriptive string
         *
         *@return    String
         */
        public String toString() {
            String s =
                    "[xUpperLeft:" + String.valueOf(xUpperLeft) + "]" +
                    "[yUpperLeft:" + String.valueOf(yUpperLeft) + "]" +
                    "[xLowerRight:" + String.valueOf(xLowerRight) + "]" +
                    "[yLowerRight:" + String.valueOf(yLowerRight) + "]";
            return s;
        }
    }


    /**
     *  An AsciiArtElement describing a string of text.
     *
     */
    public static class AsciiArtString implements AsciiArtElement {
        private double x;
        private double y;
        private String s;


        /**
         *Constructor for the AsciiArtString object
         *
         *@param  s    Description of the Parameter
         *@param  aac  Description of the Parameter
         */
        public AsciiArtString(AsciiArtCoordinate aac, String s) {
            this.x = aac.getXDouble();
            this.y = aac.getYDouble();
            this.s = s;
        }


        /**
         *  Sets the x attribute of the AsciiArtString object
         *
         *@param  x  The new x value
         */
        public void setX(double x) {
            this.x = x;
        }


        /**
         *  Sets the y attribute of the AsciiArtString object
         *
         *@param  y  The new y value
         */
        public void setY(double y) {
            this.y = y;
        }


        /**
         *  Sets the s attribute of the AsciiArtString object
         *
         *@param  s  The new s value
         */
        public void setS(String s) {
            this.s = s;
        }


        /**
         *  Gets the x attribute of the AsciiArtString object
         *
         *@return    The x value
         */
        public double getX() {
            return x;
        }


        /**
         *  Gets the y attribute of the AsciiArtString object
         *
         *@return    The y value
         */
        public double getY() {
            return y;
        }


        /**
         *  Gets the s attribute of the AsciiArtString object
         *
         *@return    The s value
         */
        public String getS() {
            return s;
        }


        /**
         *  Descriptive string
         *
         *@return    String
         */
        public String toString() {
            String s =
                    "[x:" + String.valueOf(x) + "]" +
                    "[y:" + String.valueOf(y) + "]" +
                    "[s:" + String.valueOf(this.s) + "]";
            return s;
        }
    }


    /**
     *  Helper class describing a coordinate of AsciiArtPad elements.
     *
     */
    public static class AsciiArtCoordinate {
        int x, y;
        AsciiArtPad asciiArtPad;
        double tx, ty;


        /**
         *Constructor for the AsciiArtCoordinate object
         */
        public AsciiArtCoordinate() { }


        /**
         *Constructor for the AsciiArtCoordinate object
         *
         *@param  asciiArtPad  Description of the Parameter
         */
        public AsciiArtCoordinate(AsciiArtPad asciiArtPad) {
            setAsciiArtPad(asciiArtPad);
        }


        /**
         *Constructor for the AsciiArtCoordinate object
         *
         *@param  x  Description of the Parameter
         *@param  y  Description of the Parameter
         */
        public AsciiArtCoordinate(int x, int y) {
            setXY(x, y);
        }


        /**
         *  Sets the asciiArtPad attribute of the AsciiArtCoordinate object
         *
         *@param  asciiArtPad  The new asciiArtPad value
         */
        public void setAsciiArtPad(AsciiArtPad asciiArtPad) {
            this.asciiArtPad = asciiArtPad;
        }


        /**
         *  Sets the xY attribute of the AsciiArtCoordinate object
         *
         *@param  tx  The new transXY value
         *@param  ty  The new transXY value
         */
        public void setTransXY(double tx, double ty) {
            this.tx = tx;
            this.ty = ty;
        }


        /**
         *  Sets the xY attribute of the AsciiArtCoordinate object
         *
         *@param  x  The new xY value
         *@param  y  The new xY value
         */
        public void setXY(int x, int y) {
            this.x = x;
            this.y = y;
        }


        /**
         *  Gets the xDouble attribute of the AsciiArtCoordinate object
         *
         *@return    The xDouble value
         */
        public double getXDouble() {
            return x * asciiArtPad.getXGrid() + tx;
        }


        /**
         *  Gets the yDouble attribute of the AsciiArtCoordinate object
         *
         *@return    The yDouble value
         */
        public double getYDouble() {
            return y * asciiArtPad.getYGrid() + ty;
        }
    }


    /**
     *  Helper class containing the ascii text data,
     *  acting as input of an AsciiArtPad
     *
     */
    public static class AsciiArt {
        private String[] s;
        private int w;
        private int h;


        /**
         *Constructor for the AsciiArt object
         *
         *@param  s  Description of the Parameter
         */
        public AsciiArt(String[] s) {
            this.s = s;
            int length = s.length;
            h = length;
            w = 0;
            for (int i = 0; i < length; i++) {
                String line = s[i];
                if (line != null && line.length() > w) {
                    w = line.length();
                }
            }
        }


        /**
         *  Gets the w attribute of the AsciiArt object
         *
         *@return    The w value
         */
        public int getW() {
            return w;
        }


        /**
         *  Gets the h attribute of the AsciiArt object
         *
         *@return    The h value
         */
        public int getH() {
            return h;
        }


        /**
         *  Gets the row attribute of the AsciiArt object
         *
         *@param  r  Description of the Parameter
         *@return    The row value
         */
        public String getRow(int r) {
            String row = this.s[r];
            return row;
        }


        /**
         *  Gets the column attribute of the AsciiArt object
         *
         *@param  c  Description of the Parameter
         *@return    The column value
         */
        public String getColumn(int c) {
            StringBuffer column = new StringBuffer();

            final String EMPTY_CHAR = " ";
            for (int i = 0; i < s.length; i++) {
                if (s[i] != null && c < s[i].length()) {
                    column.append(s[i].charAt(c));
                } else {
                    column.append(EMPTY_CHAR);
                }
            }
            return column.toString();
        }
    }


    /**
     *  Builder of AsciiArtElements from an AsciiArt input.
     *
     */
    public static class AsciiArtPadBuilder {
        private AsciiArtPad asciiArtPad;
        private AsciiArt aa;

        final String EDGE_GROUP = "[+\\\\/]";
        final String HLINE_GROUP = "[\\-~=+]";
        final String VLINE_GROUP = "[|+]";

        final String STRING_SUFFIX_GROUP = "[^\\-|~=\\/+ \\\\]";
        //final String STRING_PREFIX_GROUP = "[a-zA-Z0-9_\\*;\\.#]";
        final String STRING_PREFIX_GROUP = STRING_SUFFIX_GROUP;


        /**
         *Constructor for the AsciiArtPadBuilder object
         *
         *@param  asciiArtPad  Description of the Parameter
         */
        public AsciiArtPadBuilder(AsciiArtPad asciiArtPad) {
            this.asciiArtPad = asciiArtPad;
        }


        /**
         *  Build AsciiArtElement from an asciiArt
         *
         *@param  asciiArt  Description of the Parameter
         */
        public void build(String[] asciiArt) {
            aa = new AsciiArt(asciiArt);
            asciiArtPad.setWidth(aa.getW());
            asciiArtPad.setHeight(aa.getH());

            // find asciiArt patterns
            findRectPattern();
            findCornerPattern();
            findLinePattern();
            findStringPattern();
        }


        /**
         *  Find rectangles in the AsciiArt.
         *  not implemented yet.
         */
        protected void findRectPattern() {
        }


        /**
         *  Find corners in the AsciiArt
         */
        protected void findCornerPattern() {
            AsciiArtCoordinate aacStart = new AsciiArtCoordinate(this.asciiArtPad);
            aacStart.setTransXY(0, asciiArtPad.getYGrid() / 2);
            AsciiArtCoordinate aacEnd = new AsciiArtCoordinate(this.asciiArtPad);
            aacEnd.setTransXY(0, asciiArtPad.getYGrid() / 2);

            // hor line
            try {
                final RE reCorner = new RE(EDGE_GROUP);
                for (int r = 0; r < aa.getH(); r++) {
                    String row = aa.getRow(r);
                    int startIndex = 0;
                    while (reCorner.match(row, startIndex)) {
                        String s = reCorner.getParen(0);
                        int mStart = reCorner.getParenStart(0);
                        int mEnd = reCorner.getParenEnd(0);

                        if (s.equals("\\")) {
                            aacStart.setXY(mStart, r - 1);
                            aacEnd.setXY(mStart + 1, r);
                        } else if (s.equals("/")) {
                            aacStart.setXY(mStart + 1, r - 1);
                            aacEnd.setXY(mStart, r);
                        } else {
                            aacStart.setXY(mStart, r);
                            aacEnd.setXY(mStart, r);
                        }
                        AsciiArtLine aal = new AsciiArtLine(aacStart, aacEnd);
                        this.asciiArtPad.add(aal);

                        if (startIndex >= mEnd) {
                            break;
                        }
                        startIndex = mEnd;
                    }
                }
            } catch (RESyntaxException rese) {
                rese.printStackTrace();
            }

        }


        /**
         *  Find lines in the AsciiArt
         */
        protected void findLinePattern() {
            AsciiArtCoordinate aacStart = new AsciiArtCoordinate(this.asciiArtPad);
            aacStart.setTransXY(0, asciiArtPad.getYGrid() / 2);
            AsciiArtCoordinate aacEnd = new AsciiArtCoordinate(this.asciiArtPad);
            aacEnd.setTransXY(0, asciiArtPad.getYGrid() / 2);

            // hor line
            try {
                final RE reHorLine = new RE(HLINE_GROUP + "+");
                for (int r = 0; r < aa.getH(); r++) {
                    String row = aa.getRow(r);
                    int startIndex = 0;
                    while (reHorLine.match(row, startIndex)) {
                        int mStart = reHorLine.getParenStart(0);
                        int mEnd = reHorLine.getParenEnd(0);

                        aacStart.setXY(mStart, r);
                        aacEnd.setXY(mEnd - 1, r);
                        AsciiArtLine aal = new AsciiArtLine(aacStart, aacEnd);
                        this.asciiArtPad.add(aal);

                        if (startIndex >= mEnd) {
                            break;
                        }
                        startIndex = mEnd;
                    }
                }
            } catch (RESyntaxException rese) {
                rese.printStackTrace();
            }

            // ver line
            try {
                RE reVerLine = new RE(VLINE_GROUP + "+");
                for (int c = 0; c < aa.getW(); c++) {
                    String col = aa.getColumn(c);
                    int startIndex = 0;
                    while (reVerLine.match(col, startIndex)) {
                        int mStart = reVerLine.getParenStart(0);
                        int mEnd = reVerLine.getParenEnd(0);

                        aacStart.setXY(c, mStart);
                        aacEnd.setXY(c, mEnd - 1);
                        AsciiArtLine aal = new AsciiArtLine(aacStart, aacEnd);
                        this.asciiArtPad.add(aal);

                        if (startIndex >= mEnd) {
                            break;
                        }
                        startIndex = mEnd;
                    }
                }
            } catch (RESyntaxException rese) {
                rese.printStackTrace();
            }
        }


        /**
         *  Find string text in the AsciiArt.
         */
        protected void findStringPattern() {
            AsciiArtCoordinate aacStart = new AsciiArtCoordinate(this.asciiArtPad);
            aacStart.setTransXY(0, 3 * asciiArtPad.getYGrid() / 4);
            // string
            try {
                final RE reString = new RE(STRING_PREFIX_GROUP + STRING_SUFFIX_GROUP + "*");
                for (int r = 0; r < aa.getH(); r++) {
                    String row = aa.getRow(r);
                    int startIndex = 0;
                    while (reString.match(row, startIndex)) {
                        String s = reString.getParen(0);
                        int mStart = reString.getParenStart(0);
                        int mEnd = reString.getParenEnd(0);

                        aacStart.setXY(mStart, r);
                        AsciiArtString aas = new AsciiArtString(aacStart, s);
                        this.asciiArtPad.add(aas);

                        if (startIndex >= mEnd) {
                            break;
                        }
                        startIndex = mEnd;
                    }
                }
            } catch (RESyntaxException rese) {
                rese.printStackTrace();
            }
        }

    }


    /**
     *  Marker interface of objects addable to the AsciiArtPad
     *
     */
    public static interface AsciiArtElement {
    }
}

