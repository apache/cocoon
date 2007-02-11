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
package org.apache.cocoon.components.thread;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.easymock.MockControl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;


/**
 * A {@link TestCase}with convenience methods to ease creation of Avalon mock
 * classes.
 *
 * @author <a href="mailto:giacomo.at.apache.org">Giacomo Pati </a>
 * @version $Id$
 */
public class AbstractTestCase
    extends TestCase
{
    //~ Instance fields --------------------------------------------------------

    /**
     * The {@link List}of {@link MockControl}s creted by the
     * <code>create...Control</code> methods
     */
    private List m_controls;

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructor
     *
     * @param name
     */
    public AbstractTestCase( String name )
    {
        super( name );
    }

    /**
     * Constructor
     */
    public AbstractTestCase(  )
    {
        super(  );
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Create an empty list for {@link MockControl}s created by
     * <code>create...Control</code> methods
     *
     * @throws Exception
     */
    protected void setUp(  )
        throws Exception
    {
        super.setUp(  );
        m_controls = new ArrayList(  );
    }

    /**
     * Create a mock {@link Configuration}instance that has a boolean value
     *
     * @param value The value to return
     * @param defaultValue The value accepted as the default value
     *
     * @return A mock <code>Configuration</code>
     */
    protected Configuration createBooleanConfigMock( final boolean value,
                                                     final boolean defaultValue )
    {
        final MockControl valueConfigControl =
            createStrictControl( Configuration.class );
        final Configuration valueConfig =
            (Configuration)valueConfigControl.getMock(  );
        valueConfig.getValueAsBoolean( defaultValue );
        valueConfigControl.setReturnValue( value );
        valueConfigControl.replay(  );

        return valueConfig;
    }

    /**
     * Create a mock {@link Configuration}instance that has a boolean value
     *
     * @param value The value to return
     *
     * @return A mock <code>Configuration</code>
     *
     * @throws ConfigurationException
     */
    protected Configuration createBooleanConfigMock( final boolean value )
        throws ConfigurationException
    {
        final MockControl valueConfigControl =
            createStrictControl( Configuration.class );
        final Configuration valueConfig =
            (Configuration)valueConfigControl.getMock(  );
        valueConfig.getValueAsBoolean(  );
        valueConfigControl.setReturnValue( value );
        valueConfigControl.replay(  );

        return valueConfig;
    }

    /**
     * Create a {@link Configuration}instance that has a child
     *
     * @param name The value accepted as the name for the child
     * @param value The value to return
     *
     * @return A mock <code>Configuration</code>
     */
    protected Configuration createChildConfigMock( final String name,
                                                   final Configuration value )
    {
        final MockControl childConfigControl =
            createStrictControl( Configuration.class );
        final Configuration childConfig =
            (Configuration)childConfigControl.getMock(  );
        childConfig.getChild( name );
        childConfigControl.setReturnValue( value );
        childConfigControl.replay(  );

        return childConfig;
    }

    /**
     * Create a {@link Configuration}instance that has a boolean value
     *
     * @param name The value accepted as the name for the children
     * @param value The value to return
     *
     * @return A mock <code>Configuration</code>
     */
    protected Configuration createChildrenConfigMock( final String name,
                                                      final Configuration [] value )
    {
        final MockControl childrenConfigControl =
            createStrictControl( Configuration.class );
        final Configuration childrenConfig =
            (Configuration)childrenConfigControl.getMock(  );
        childrenConfig.getChildren( name );
        childrenConfigControl.setReturnValue( value );
        childrenConfigControl.replay(  );

        return childrenConfig;
    }

    /**
     * Create a {@link Configuration}instance that has a int value
     *
     * @param value The value to return
     * @param defaultValue The value accepted as the default value
     *
     * @return A mock <code>Configuration</code>
     */
    protected Configuration createIntegerConfigMock( final int value,
                                                     final int defaultValue )
    {
        final MockControl valueConfigControl =
            createStrictControl( Configuration.class );
        final Configuration valueConfig =
            (Configuration)valueConfigControl.getMock(  );
        valueConfig.getValueAsInteger( defaultValue );
        valueConfigControl.setReturnValue( value );
        valueConfigControl.replay(  );

        return valueConfig;
    }

    /**
     * Create a {@link Configuration}instance that has a int value
     *
     * @param value The value to return
     *
     * @return A mock <code>Configuration</code>
     *
     * @throws ConfigurationException
     */
    protected Configuration createIntegerConfigMock( final int value )
        throws ConfigurationException
    {
        final MockControl valueConfigControl =
            createStrictControl( Configuration.class );
        final Configuration valueConfig =
            (Configuration)valueConfigControl.getMock(  );
        valueConfig.getValueAsInteger(  );
        valueConfigControl.setReturnValue( value );
        valueConfigControl.replay(  );

        return valueConfig;
    }

    /**
     * Create a {@link Configuration}instance that has a long value
     *
     * @param value The value to return
     * @param defaultValue The value accepted as the default value
     *
     * @return A mock <code>Configuration</code>
     */
    protected Configuration createLongConfigMock( final long value,
                                                  final long defaultValue )
    {
        final MockControl valueConfigControl =
            createStrictControl( Configuration.class );
        final Configuration valueConfig =
            (Configuration)valueConfigControl.getMock(  );
        valueConfig.getValueAsLong( defaultValue );
        valueConfigControl.setReturnValue( value );
        valueConfigControl.replay(  );

        return valueConfig;
    }

    /**
     * Create a {@link Configuration}instance that has a long value
     *
     * @param value The value to return
     *
     * @return A mock <code>Configuration</code>
     *
     * @throws ConfigurationException
     */
    protected Configuration createLongConfigMock( final long value )
        throws ConfigurationException
    {
        final MockControl valueConfigControl =
            createStrictControl( Configuration.class );
        final Configuration valueConfig =
            (Configuration)valueConfigControl.getMock(  );
        valueConfig.getValueAsLong(  );
        valueConfigControl.setReturnValue( value );
        valueConfigControl.replay(  );

        return valueConfig;
    }

    /**
     * Create a strict mock control
     *
     * @param clazz The interface class the mock object should represent
     *
     * @return The mock instance
     */
    protected MockControl createStrictControl( final Class clazz )
    {
        final MockControl control = MockControl.createStrictControl( clazz );
        m_controls.add( control );

        return control;
    }

    /**
     * Create a {@link Configuration}instance that has a string value
     *
     * @param value The value to return
     * @param defaultValue The value accepted as the default value
     *
     * @return A mock <code>Configuration</code>
     */
    protected Configuration createValueConfigMock( final String value,
                                                   final String defaultValue )
    {
        final MockControl valueConfigControl =
            createStrictControl( Configuration.class );
        final Configuration valueConfig =
            (Configuration)valueConfigControl.getMock(  );
        valueConfig.getValue( defaultValue );
        valueConfigControl.setReturnValue( value );
        valueConfigControl.replay(  );

        return valueConfig;
    }

    /**
     * Create a {@link Configuration}instance that has a string value
     *
     * @param value The value to return
     *
     * @return A mock <code>Configuration</code>
     *
     * @throws ConfigurationException
     */
    protected Configuration createValueConfigMock( final String value )
        throws ConfigurationException
    {
        final MockControl valueConfigControl =
            createStrictControl( Configuration.class );
        final Configuration valueConfig =
            (Configuration)valueConfigControl.getMock(  );
        valueConfig.getValue(  );
        valueConfigControl.setReturnValue( value );
        valueConfigControl.replay(  );

        return valueConfig;
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown(  )
        throws Exception
    {
        super.tearDown();
        m_controls = null;
    }

    /**
     * Verify all <code>MockCOntrol</code>s
     */
    protected void verify(  )
    {
        for( Iterator i = m_controls.iterator(  ); i.hasNext(  ); )
        {
            final MockControl control = (MockControl)i.next(  );
            control.verify(  );
        }
    }
}
