Pipeline Implementation
-----------------------
The Implementation uses the following parameters:

cache-key : the key of the cache entry
cache-expires : the time frame the cache is valid in seconds
purge-cache : when set to true the cache is purged

Action
------
Deletes one entry with the given key from the cache
cache-role: the role of the cache
cache-key : the key of the cache entry


Caching Source Implementation
-----------------------------

Configuration in the cocoon.xconf:

<source-factories>
 ...

<component-instance class="org.apache.cocoon.components.source.impl.CachingSourceFactory" name="cached">
</component-instance>
<component-instance class="org.apache.cocoon.components.source.impl.CachingSourceFactory" name="acached">
  <parameter name="async" value="true"/>
</component-instance>
</source-factories>

Synced caching
--------------

Caches another source for a given period of time. Usage:

cached://EXPIRES@URL or cached://EXPIRES@CACHE_KEY@URL

If the cached content expires, the content is fetched new on the next request.

Example: cached://60@http://www.s-und-n.de

The source http://www.s-und-n.de is cached for 60 seconds using the key
http://www.s-und-n.de.

Example: cached://60@main@http://www.s-und-n.de.

The source http://www.s-und-n.de is cached for 60 seconds using the key main.

Async caching
-------------

Example: acached://60@http://www.s-und-n.de

The source is refreshed in background every 60 seconds and
is always streamed using the cached version.


Configuration
=============

The new cron scheduler from the cron block is used for scheduling.

Configuration in cocoon.xconf:

<!-- This is a sample target, that is called as configured in the sample above -->
<component role="org.apache.avalon.cornerstone.services.scheduler.Target/update" 
           class="org.apache.cocoon.components.source.impl.UpdateTarget"/>

<component class="org.apache.cocoon.components.source.impl.RefresherImpl" role="org.apache.cocoon.components.source.impl.Refresher">
  <parameter name="scheduler-target" value="org.apache.avalon.cornerstone.services.scheduler.Target/update"/>
  <parameter name="write-interval" value="3600"/>
  <parameter name="write-target" value="org.apache.cocoon.components.source.impl.Refresher"/>
  <parameter name="write-source" value="context://targets.xml"/>
</component>

The last component, the Refresher, persists the configuration of the scheduler
to an xml file. This file is read on startup and the registered sources are
fetched in the background.

