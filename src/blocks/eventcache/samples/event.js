var role = Packages.org.apache.cocoon.caching.Cache.ROLE;

function cacheEvent() {
	var cache = cocoon.getComponent(role);
	var event = new Packages.org.apache.cocoon.caching.validity.NamedEvent(cocoon.request.event);
	var rand = Math.random() * 10000000000000000000;
	cache.processEvent(event);
	cocoon.releaseComponent(cache);
	cocoon.redirectTo("demo?pageKey=" + cocoon.request.pageKey + "&rand="+rand);
}