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

package org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements;

import org.apache.cocoon.components.elementprocessor.types.NumericConverter;
import org.apache.cocoon.components.elementprocessor.types.NumericResult;
import org.apache.cocoon.components.elementprocessor.types.Validator;

import java.io.IOException;

/**
 * No-op implementation of ElementProcessor to handle the "Frame" tag
 *
 * This element has a small number of Attributes and no content.
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: EPFrame.java,v 1.4 2004/01/31 08:50:39 antonio Exp $
 */
public class EPFrame extends BaseElementProcessor {
    private String _label;
    private String _object_bound;
    private Offsets _object_offset;
    private Anchors _object_anchor_type;
    private NumericResult _direction;
    private static final String _label_attribute = "Label";
    private static final String _object_bound_attribute = "ObjectBound";
    private static final String _object_offset_attribute = "ObjectOffset";
    private static final String _object_anchor_type_attribute =
        "ObjectAnchorType";
    private static final String _direction_attribute = "Direction";
    private static final Validator _direction_validator = new Validator() {
        public IOException validate(final Number number) {
            return Direction.isValid(number.intValue()) ? null
                : new IOException("\"" + number + "\" is not a legal value");
        }
    };

    /**
     * constructor
     */
    public EPFrame() {
        super(null);
        _label = null;
        _object_bound = null;
        _object_offset = null;
        _object_anchor_type = null;
        _direction = null;
    }

    /**
     * @return label
     * @exception IOException
     */
    public String getLabel() throws IOException {
        if (_label == null) {
            _label = getValue(_label_attribute);
            if (_label == null) {
                throw new IOException(
                    "missing " + _label_attribute + " attribute");
            }
        }
        return _label;
    }

    /**
     * @return object_bound
     * @exception IOException
     */
    public String getObjectBound() throws IOException {
        if (_object_bound == null) {
            _object_bound = getValue(_object_bound_attribute);
            if (_object_bound == null) {
                throw new IOException(
                    "missing " + _object_bound_attribute + " attribute");
            }
        }
        return _object_bound;
    }

    /**
     * @return offsets
     * @exception IOException
     */
    public Offsets getOffsets() throws IOException {
        if (_object_offset == null) {
            _object_offset = new Offsets(getValue(_object_offset_attribute));
        }
        return _object_offset;
    }

    /**
     * @return anchors
     * @exception IOException
     */
    public Anchors getAnchors() throws IOException {
        if (_object_anchor_type == null) {
            _object_anchor_type =
                new Anchors(getValue(_object_anchor_type_attribute));
        }
        return _object_anchor_type;
    }

    /**
     * @return direction as a public member of Direction
     * @exception IOException
     */
    public int getDirection() throws IOException {
        if (_direction == null) {
            _direction =
                NumericConverter.extractInteger(
                    getValue(_direction_attribute),
                    _direction_validator);
        }
        return _direction.intValue();
    }
} // end public class EPFrame
