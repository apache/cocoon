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
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

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
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;
import org.xml.sax.SAXException;

/**
 * The <code>ImageOpReader</code> component is used to serve binary image data
 * in a sitemap pipeline. It makes use of HTTP Headers to determine if
 * the requested resource should be written to the <code>OutputStream</code>
 * or if it can signal that it hasn't changed.
 *
 * @cocoon.sitemap.component.documentation
 * The <code>ImageOpReader</code> component is used to serve binary image data
 * in a sitemap pipeline. It makes use of HTTP Headers to determine if
 * the requested resource should be written to the <code>OutputStream</code>
 * or if it can signal that it hasn't changed.
 * @cocoon.sitemap.component.documentation.caching Yes
 *
 * @version $Id$
 */
final public class ImageOpReader extends ResourceReader
                                 implements Configurable, Serviceable, Disposable {

    private final static String FORMAT_DEFAULT = "png";

    private String          format;
    private ArrayList       effectsStack;
    private ServiceSelector operationSelector;
    private ServiceManager  manager;
    private SourceResolver  resolver;

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
        operationSelector = (ServiceSelector) man.lookup( GenericImageOperation.ROLE + "Selector" );
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
        this.resolver = resolver;
        
        format = par.getParameter("output-format", FORMAT_DEFAULT);
        if(getLogger().isInfoEnabled()) {
            getLogger().info( src + " --> " + format );
        }
        setupEffectsStack( par, resolver );
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
            GenericImageOperation op = (GenericImageOperation) list.next();
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
            GenericImageOperation op = (GenericImageOperation) operationSelector.select( type );
            op.setPrefix( prefix );
            effectsStack.add( op );
        }
    }

    private void setupEffectsStack( Parameters params, SourceResolver resolver )
    throws ProcessingException {
        Iterator list = effectsStack.iterator();
        while( list.hasNext() ) {
            GenericImageOperation op = (GenericImageOperation) list.next();
            op.setup( params );
        }
    }

    private BufferedImage applyEffectsStack( BufferedImage image ) throws ProcessingException {
        if( effectsStack.size() == 0 ) {
            return image;
        }
        Iterator list = effectsStack.iterator();
        WritableRaster src = image.getRaster();
        BufferedImage newImage = null;
        
        while( list.hasNext() ) {
            GenericImageOperation op = (GenericImageOperation) list.next();
            WritableRaster r;
            if (op instanceof ImageOperation) {
                r = ((ImageOperation) op).apply( src );
                
	            if(getLogger().isDebugEnabled()) {
	                getLogger().debug( "In Bounds: " + r.getBounds() );
	            }
	            src = r.createWritableTranslatedChild( 0, 0 );
	            
            } else if (op instanceof CombineImagesOperation) {
                CombineImagesOperation cio = (CombineImagesOperation) op;
                String uri = cio.getOverlayURI();
                Source secondImageSource;
                try {
                    secondImageSource = resolver.resolveURI(uri);
                } catch (MalformedURLException e) {
                    throw new ProcessingException("URI for second image of combine image operation has a malformed URL: " + uri, e);
                } catch (IOException e) {
                    throw new ProcessingException("Source '" + uri + "' for combine image operation cannot be resolved.", e);
                }
                BufferedImage secondImage;
                try {
                    secondImage = ImageIO.read(secondImageSource.getInputStream());
                } catch (SourceNotFoundException e) {
                    throw new ProcessingException("Source '" + uri + "' for combine image operation cannot be found.", e);
                } catch (IOException e) {
                    throw new ProcessingException("Source '" + uri + "' for combine image operation cannot be read.", e);
                }
                
                newImage = cio.combine(image, secondImage);
                image = newImage;
                src = image.getRaster();
            } else {
                continue;
            }
        }
        if (newImage == null) {        	
            ColorModel cm = image.getColorModel();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug( "Out Bounds: " + src.getBounds() );
            }
            // Note: use cm.isAlphaPremultiplied() as third parameter (isRasterPremultiplied)
            // because this avoids the possible non-working call to cm.coerceData() (eg. with png
            // images) - see the code of BufferedImage
            newImage = new BufferedImage(cm, src, cm.isAlphaPremultiplied(), new Hashtable());
        }

        if (getLogger().isInfoEnabled()) {
            int minX = newImage.getMinX();
            int minY = newImage.getMinY();
            int width = newImage.getWidth();
            int height = newImage.getHeight();        
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

}
