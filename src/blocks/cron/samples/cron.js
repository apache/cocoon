function cron( realPath )
{
	var done = false;
	var scheduler = cocoon.getComponent(Packages.org.apache.cocoon.components.cron.JobScheduler.ROLE);
	while( ! done )
	{
		var jobnames = scheduler.getJobNames();
		var list = [];
		for( i = 0; i < jobnames.length; i++ ) 
		{
			list[ i ] = scheduler.getJobSchedulerEntry( jobnames[ i ] );
		}
		
		var fileName = realPath + "/WEB-INF/logs/cron.log";
		var rdr = new java.io.BufferedReader( java.io.FileReader( fileName ) );
		var count = 0;
		var lines = new java.util.LinkedList();
		var line = rdr.readLine();
		while( line != null )
		{
			count++;
			lines.addLast( line );
			if( count > 20 )
			{
				lines.removeFirst();
			}
			line = rdr.readLine();
		}
		print( "sending page" );
		cocoon.sendPageAndWait( "cron.view", { "entries" : list, "log" : lines } );
		print( "back again" );
		var action = cocoon.request.getParameter( "action" );
		print( "action=" + action );
		if( action == "remove" )
		{
			print( "going to remove job " + name );
			var name = cocoon.request.getParameter( "name" );
			scheduler.removeJob( name );
			print( "job " + name + " removed" );
		}
	}
}