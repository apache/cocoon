This is a scheduling component based on the cornerstone scheduler.
To get it running add these components to the cocoon.xconf:

<!-- This is the cornerstone scheduler -->
<component role="org.apache.avalon.cornerstone.services.scheduler.TimeScheduler" 
           class="org.apache.cocoon.components.scheduler.DefaultTimeScheduler"/>

<!-- This is the cornerstone thread manager -->
<component role="org.apache.avalon.cornerstone.services.threads.ThreadManager" 
           class="org.apache.cocoon.components.scheduler.DefaultThreadManager">
  <thread-group>
    <name>default</name>
  </thread-group>
</component>

<!-- This is an extended cocoon scheduler -->
<component role="org.apache.cocoon.components.scheduler.Scheduler" 
           class="org.apache.cocoon.components.scheduler.DefaultScheduler">
    <triggers>
        <!-- This is a sample trigger -->
        <trigger name="test"
                 target="org.apache.avalon.cornerstone.services.scheduler.Target/test">
            <timed type="periodic">
                <period>480000</period> <!-- ms, e.g. 8 minutes -->
            </timed>
        </trigger>
    </triggers>
</component>

<!-- This is a sample target, that is called as configured in the sample above -->
<component role="org.apache.avalon.cornerstone.services.scheduler.Target/test" 
           class="org.apache.cocoon.components.scheduler.TestTarget"/>
