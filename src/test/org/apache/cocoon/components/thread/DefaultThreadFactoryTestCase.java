package org.apache.cocoon.components.thread;

import junit.framework.TestCase;

public class DefaultThreadFactoryTestCase
    extends TestCase
{
    public final void testSetDaemon()
    {
        final DefaultThreadFactory factory = new DefaultThreadFactory();
        factory.setDaemon(false);
        final Thread thread = factory.newThread(new DummyRunnable() );
        assertEquals( "daemon mode", false, thread.isDaemon() );
    }

    public final void testIsDaemon()
    {
        final DefaultThreadFactory factory = new DefaultThreadFactory();
        factory.setDaemon(false);
        assertEquals( "daemon mode", false, factory.isDaemon() );
    }

    public final void testSetPriority()
    {
        final DefaultThreadFactory factory = new DefaultThreadFactory();
        factory.setPriority( Thread.MAX_PRIORITY );
        final Thread thread = factory.newThread(new DummyRunnable() );
        assertEquals( "priority", Thread.MAX_PRIORITY, thread.getPriority() );
    }

    public final void testGetPriority()
    {
        final DefaultThreadFactory factory = new DefaultThreadFactory();
        factory.setPriority( Thread.MAX_PRIORITY );
        assertEquals( "priority", Thread.MAX_PRIORITY, factory.getPriority() );
    }

    public final void testNewThread()
    {
        final DefaultThreadFactory factory = new DefaultThreadFactory();
        factory.setDaemon(true);
        factory.setPriority( Thread.MIN_PRIORITY );
        final Thread thread = factory.newThread(new DummyRunnable() );
        assertEquals( "new thread daemon mode", true, thread.isDaemon() );
        assertEquals( "new thread priority", Thread.MIN_PRIORITY, thread.getPriority() );
        assertEquals( "factory daemon mode", factory.isDaemon(), thread.isDaemon() );
        assertEquals( "factory priority", factory.getPriority(), thread.getPriority() );
    }
    
    private static class DummyRunnable implements Runnable
    {
        public void run()
        {
            // nothing
        }
    }
}
