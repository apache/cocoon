/*>$File$ -- $Id: NRGFormatter.java,v 1.2 1999-11-09 02:21:29 dirkx Exp $ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
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
 
 */
package org.apache.cocoon.formatter.image;

import java.io.*;
import java.awt.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import org.w3c.dom.*;
import com.sun.image.codec.jpeg.*;

/**
 * The Formatter for the Nestable Raster Graphics format.
 *
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.2 $ $Date: 1999-11-09 02:21:29 $
 */
public class ImageFormatter implements Formatter, Configurable, Status {

    int R=0;
    int G=1;
    int B=2;
    int A=3;
    
    private String format;
    private Hashtable codecs;
    
    public ImageFormatter() {
        this.codecs = new Hashtable(2, 0.5);
        this.codecs.put("image/jpg", "com.sun.image.codec.jpeg.
    
    
	public void init(Configurations conf) {
        format = (String) conf.get("image_format", "image/jpg");
        
		try {
            sf = (StreamFormat) sf.getClass().getField(printer).get(sf);
            sf = sf.changeLineWrap(Integer.parseInt(width));   
            sf = sf.changeIndentSpaces(Integer.parseInt(spaces));
            if (systemID != null) {
				sf = sf.changeExternalDTD(publicID, systemID);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create formatter: " + e);
        }
	}
	
    public void format(Document doc, Writer writer, Dictionary parameters) throws Exception {

        // Check for proper root element
        Element element=doc.getDocumentElement();
        if (!element.getTagName().equals("image")) {
            throw new Exception("Cannot recognize document DTD: invalid root element \"" + element.getTagName() + "\"");
        }
        
        ???????????????

        // Check wether we have a source image
        String source=element.getAttribute("source");
        BufferedImage img=null;
        Graphics2D gr=null;
        // If we have a source image, this is taken as our source.
        if (source.length()>0) {
            ImageIcon ico=new ImageIcon(source);
            if ((ico.getIconWidth()<1) | (ico.getIconHeight()<1)) {
                throw new CreationException("Cannot load image \""+source+"\"");
            }
            img=new BufferedImage(ico.getIconWidth(), ico.getIconHeight(),
                                  BufferedImage.TYPE_INT_RGB);
            gr=img.createGraphics();
            gr.drawImage(ico.getImage(),0,0,ico.getImageObserver());
        } else {
            // We don't have a source image. Build up a new getting height and
            // width from attributes
            int w=0;
            int h=0;
            try {
                String width=element.getAttribute("width");
                String height=element.getAttribute("height");
                w=Integer.parseInt(width);
                h=Integer.parseInt(height);
            } catch (NumberFormatException e) {
                throw new CreationException("Image witdth or height error");
            }
            if ((w<1) | (h<1)) {
                throw new CreationException("Image witdth or height unspecified");
            }
            img=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
            gr=img.createGraphics();
            // Now process the bgcolor attribute for background colorization
            int bg=0;
            try {
                String bgcolor=element.getAttribute("bgcolor");
                bg=Integer.parseInt(bgcolor,16);
            } catch (NumberFormatException e) {
                throw new CreationException("Background color error");
            }
            Color c=new Color(bg);
            gr.setBackground(c);
            gr.clearRect(0,0,w,h);
            // Check if we have a background to tile
            String background=element.getAttribute("background");
            if (background.length()>0) {
                // We have a background image. Load it.
                ImageIcon ico=new ImageIcon(background);
                if ((ico.getIconWidth()<1) | (ico.getIconHeight()<1)) {
                    throw new CreationException("Cannot load background \""+source+"\"");
                }
                // Tile the image.
                int x=0;
                int y=0;
                while (true) {
                    gr.drawImage(ico.getImage(),x,y,ico.getImageObserver());
                    x+=ico.getIconWidth();
                    if (x>w) {
                        x=0;
                        y+=ico.getIconHeight();
                    }
                    if (y>h) break;
                }
            }            
        }

        // Process child elements
        NodeList l=element.getChildNodes();
        for (int x=0;x<l.getLength();x++) {
            if (l.item(x).getNodeType()==Node.ELEMENT_NODE) {
                processElement((Element)l.item(x),img);
            }
        }

        // Write out image (highest quality for jpeg data)
        JPEGEncodeParam jpar=JPEGCodec.getDefaultJPEGEncodeParam(img);
        jpar.setQuality(1,true);
        JPEGImageEncoder jenc=JPEGCodec.createJPEGEncoder(out,jpar);
        jenc.encode(img);
        out.flush();
    }
    
    void processElement(Element e, BufferedImage i)
    throws CreationException {
        if (e==null) return;
        int R=0; int G=1; int B=2; int A=3;
        // Create new alpha image
        int w=i.getWidth();
        int h=i.getHeight();
        BufferedImage n=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        // Clear new alpha image
        int np[]=new int[]{0,0,0,0};
        WritableRaster nr=n.getRaster();
        for (int x=0;x<nr.getWidth();x++) for (int y=10;y<nr.getHeight();y++) {
            nr.setPixel(x,y,np);
        }
        // Check wich element is being processed.
        boolean ret=false;
        if (e.getTagName().equals("text")) ret=placeText(e,n);
        if(!ret) return;
        // Apply new image over existing
        WritableRaster ir=i.getRaster();
        int ip[]=new int[]{0,0,0,0};
        double ia=0;
        double na=1;
        for (int x=0;x<ir.getWidth();x++) for (int y=0;y<ir.getHeight();y++) {
            ir.getPixel(x,y,ip);
            nr.getPixel(x,y,np);
            if (np[A]>0) {
                na=((double)np[A]/255);
                ia=(1-na);
                ip[R]=(int)(((double)ip[R]*ia) + ((double)np[R]*na));
                ip[G]=(int)(((double)ip[G]*ia) + ((double)np[G]*na));;
                ip[B]=(int)(((double)ip[B]*ia) + ((double)np[B]*na));;
            }
            ir.setPixel(x,y,ip);
        }
    }
    
    private boolean placeText(Element e, BufferedImage i)
    throws CreationException {
        String text=e.getAttribute("text");
        String font=e.getAttribute("font");
        String ssize=e.getAttribute("size");
        String sstyle=e.getAttribute("style");
        String scolor=e.getAttribute("color");
        String sx=e.getAttribute("x");
        String sy=e.getAttribute("y");
        String halign=e.getAttribute("halign");
        String valign=e.getAttribute("valign");
        String antialiased=e.getAttribute("antialiased");
        // Check proper text
        if (text.length()<1) return(false);
        // Check size, x and y parameters
        int color,size,insx,insy;
        try {
            size=Integer.parseInt(ssize);
            insx=Integer.parseInt(sx);
            insy=Integer.parseInt(sy);
            color=Integer.parseInt(scolor,16);
        } catch (NumberFormatException ex) {
            throw new CreationException("Attribute size, x, y or color error");
        }
        // Get style
        int style=Font.PLAIN;
        if (sstyle.equals("bold")) style=Font.BOLD;
        else if (sstyle.equals("italic")) style=Font.ITALIC;
        else if (sstyle.equals("bolditalic")) style=Font.BOLD+Font.ITALIC;
        Font f=new Font(font,style,size);
        FontMetrics m=i.createGraphics().getFontMetrics(f);
        // Create temporary image
        int w=m.stringWidth(text)*2;
        int h=m.getHeight()*2;
        BufferedImage n=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        // Write out some text
        Graphics2D gr=n.createGraphics();
        gr.setColor(new Color(0x0ff000000,true));
        gr.setFont(f);
        if(!antialiased.equals("false"))
            gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
        gr.drawString(text,5,h-(h/4));
        // Resize image
        int minx=w;
        int miny=h;
        int maxx=0;
        int maxy=0;
        WritableRaster nr=n.getRaster();
        int np[]=new int[]{0,0,0,0};
        for (int x=0;x<nr.getWidth();x++) for (int y=10;y<nr.getHeight();y++) {
            nr.getPixel(x,y,np);
            if (np[A]>0) {
                if(x<minx) minx=x;
                if(x>maxx) maxx=x;
                if(y<miny) miny=y;
                if(y>maxy) maxy=y;
            }
        }
        if (halign.equals("right")) insx=insx-((maxx-minx)+1);
        if (halign.equals("center")) insx=insx-(((maxx-minx)+1)/2);
        if (valign.equals("bottom")) insy=insy-((maxy-miny)+1);
        if (valign.equals("center")) insy=insy-(((maxy-miny)+1)/2);
        WritableRaster ir=i.getRaster();
        int newx,newy;
        newy=insy;
        for (int y=miny;y<=maxy;y++) {
            newx=insx;
            if(newy>=0) {
                for (int x=minx;x<=maxx;x++) {
                    nr.getPixel(x,y,np);
                    np[R]=(color >> 16) & 0x0ff;
                    np[G]=(color >> 8) & 0x0ff;
                    np[B]=(color & 0x0ff);
                    if(newx>=0) ir.setPixel(newx,newy,np);
                    newx++;
                    if(newx>=i.getWidth()) break;
                }
            }
            newy++;
            if(newy>=i.getHeight()) break;
        }
        return(true);
    }
    
    /**
     * Returns the MIME type used by this formatter for output.
     */
    public String getMIMEType() {
        return format;
    }        
    
    public String getStatus() {
        return "NRG Image Formatter [" + format + "]";
    }
}
