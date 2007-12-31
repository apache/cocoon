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
package org.apache.cocoon.reading.imageop;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.spi.ImageWriterSpi;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.reading.ResourceReader;
import org.xml.sax.SAXException;

/**
 * The <code>ImageOpReader</code> component is used to serve binary image data
 * in a sitemap pipeline. It makes use of HTTP Headers to determine if
 * the requested resource should be written to the <code>OutputStream</code>
 * or if it can signal that it hasn't changed. 
 */
final public class ImageOpReader
    extends ResourceReader
    implements Configurable, Serviceable, Disposable {

    private final static String FORMAT_DEFAULT = "png";

    private String          format;
    private ArrayList       effectsStack;
    private ServiceSelector operationSelector;
    private ServiceManager  manager;

    /**
     * Read reader configuration
     */
    public void configure(Configuration configuration) 
    throws ConfigurationException {
        super.configure( configuration );
        Configuration effects = configuration.getChild( "effects" );
        try {
            configureEffects( effects );
        } catch( ServiceException e ) {
            throw new ConfigurationException( "Unable to configure ImageOperations", e );
        }
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service( ServiceManager man )
    throws ServiceException {
        this.manager = man;
        operationSelector = (ServiceSelector) man.lookup( ImageOperation.ROLE + "Selector" );
    }
    
    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.operationSelector);
            this.operationSelector = null;
            this.manager = null;
        }
    }

    /**
     * @see org.apache.cocoon.reading.ResourceReader#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        format = par.getParameter("output-format", FORMAT_DEFAULT);
        if(getLogger().isInfoEnabled()) {
            getLogger().info( src + " --> " + format );
        }
        setupEffectsStack( par );
    }

    protected void processStream( InputStream inputStream ) 
    throws IOException, ProcessingException {
        if( effectsStack.size() > 0 ) {
            // since we create the image on the fly
            response.setHeader("Accept-Ranges", "none");

            BufferedImage image = ImageIO.read( inputStream );
            if( image == null ) {
                throw new ProcessingException( "Unable to decode the InputStream. Possibly an unknown format." );                
            }
            image = applyEffectsStack( image );

            write( image );
        } else {
            // only read the resource - no modifications requested
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("passing original resource");
            }
            super.processStream(inputStream);
        }
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key consists of the src and width and height, and the color transform
     * parameters
    */
    public Serializable getKey() {
        StringBuffer b = new StringBuffer( 200 );
        b.append( this.inputSource.getURI() );
        b.append( ':' );
        b.append( format );
        b.append( ':' );
        b.append( super.getKey() );
        b.append( ':' );
        Iterator list = effectsStack.iterator();
        while( list.hasNext() ) {
            ImageOperation op = (ImageOperation) list.next();
            b.append( op.getKey() );
            b.append( ':' );
        }
        String key = b.toString();
        b.setLength( 0 );   // Seems to be something odd (memory leak?)
                            // going on if this isn't done. (JDK1.4.2)
        return key;
    }
    
    private void configureEffects( Configuration conf )
    throws ConfigurationException, ServiceException {
        effectsStack = new ArrayList();

        Configuration[] ops = conf.getChildren( "op" );
        for( int i=0 ; i < ops.length ; i++ ) {
            String type = ops[i].getAttribute( "type" );
            String prefix = ops[i].getAttribute( "prefix", type + "-" );
            ImageOperation op = (ImageOperation) operationSelector.select( type );
            op.setPrefix( prefix );
            effectsStack.add( op );
        }
    }

    private void setupEffectsStack( Parameters params )
    throws ProcessingException {
        Iterator list = effectsStack.iterator();
        while( list.hasNext() ) {
            ImageOperation op = (ImageOperation) list.next();
            op.setup( params );
        }
    }

    private BufferedImage applyEffectsStack( BufferedImage image ) {
        if( effectsStack.size() == 0 ) {
            return image;
        }
        Iterator list = effectsStack.iterator();
        WritableRaster src = image.getRaster();
        while( list.hasNext() ) {
            ImageOperation op = (ImageOperation) list.next();
            WritableRaster r = op.apply( src );
            if(getLogger().isDebugEnabled()) {
                getLogger().debug( "In Bounds: " + r.getBounds() );
            }
            src = r.createWritableTranslatedChild( 0, 0 );
        }
        ColorModel cm = image.getColorModel();
        if (getLogger().isDebugEnabled()) {
            getLogger().debug( "Out Bounds: " + src.getBounds() );
        }
        BufferedImage newImage = new BufferedImage( cm, src, true, new Hashtable() );
        // Not sure what this should really be --------------^^^^^

        int minX = newImage.getMinX();
        int minY = newImage.getMinY();
        int width = newImage.getWidth();
        int height = newImage.getHeight();        
        if(getLogger().isInfoEnabled()) {
            getLogger().info( "Image: " + minX + ", " + minY + ", " + width + ", " + height );
        }

        return newImage;
    }

    private void write( BufferedImage image )
    throws ProcessingException, IOException {
        ImageTypeSpecifier its = ImageTypeSpecifier.createFromRenderedImage( image );
        Iterator writers = ImageIO.getImageWriters( its, format );
        ImageWriter writer = null;
        if( writers.hasNext() ) {
            writer = (ImageWriter) writers.next();
        }
        if( writer == null ) {
            throw new ProcessingException( "Unable to find a ImageWriter: " + format );
        }

        ImageWriterSpi spi = writer.getOriginatingProvider();
        String[] mimetypes = spi.getMIMETypes();
        if (getLogger().isInfoEnabled()) {
            getLogger().info( "Setting content-type: " + mimetypes[0] );
        }
        response.setHeader("Content-Type", mimetypes[0] );
        ImageOutputStream output = ImageIO.createImageOutputStream( out );
        try {
            writer.setOutput( output );
            writer.write( image );
        } finally {
            writer.dispose();
            output.close();
            out.flush();
            // Niclas Hedhman: Stream is closed in superclass.
        }
    }
/*    
    private void printRaster( WritableRaster r )
    {
        DataBuffer data = r.getDataBuffer();
        int numBanks = data.getNumBanks();
        int size = data.getSize();
        for( int i=0 ; i < size ; i++ )
        {
            long value = 0;
            for( int j=0 ; j < numBanks ; j++ )
            {
                int v = data.getElem( j, i );
                if( v < 256 )
                    value = value << 8 ;
                else
                    value = value << 16;
                value = value + v;
            }
            if(getLogger().isDebugEnabled()) {
                getLogger().debug( Long.toHexString( value ) );
            }
        }
    }
*/
}
