/*
* Copyright 1999-2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
function Format() {
}
    
Format.prototype.format = function(date) {
    if( date == null || date == undefined )
        return "-";
       return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
}
Format.prototype.parse = function(date) {
    if( date == null || date == undefined )
        return "";
       return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
}

function cron( realPath )
{
    var done = false;
    var scheduler = cocoon.getComponent(Packages.org.apache.cocoon.components.cron.JobScheduler.ROLE);
    var msg_param_key = Packages.org.apache.cocoon.components.cron.TestCronJob.PARAMETER_MESSAGE;
    var sleep_param_key = Packages.org.apache.cocoon.components.cron.TestCronJob.PARAMETER_SLEEP;
    var pipeline_param_key = Packages.org.apache.cocoon.components.cron.TestCronJob.PARAMETER_PIPELINE;
    var testjobrole = "org.apache.cocoon.components.cron.CronJob/test";
    var logsize = 15;
    var formatter = new Format();
    var jobname = "";
    var message = "I'm here";
    var sleep = "23";
    var pipeline = "samples/hello-world/hello.xml";
    var cronexpr = "";
    var intervalexpr = "";
    var atexpr = "";
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
            if( count > logsize )
            {
                lines.removeFirst();
            }
            line = rdr.readLine();
        }
        cocoon.sendPageAndWait( "cron.view", { "entries"      : list, 
                                               "log"          : lines,
                                               "formatter"    : formatter,
                                               "jobname"      : jobname,
                                               "message"      : message,
                                               "sleep"        : sleep,
                                               "pipeline"     : pipeline,                                               
                                               "cronexpr"     : cronexpr,
                                               "intervalexpr" : intervalexpr,
                                               "atexpr"       : atexpr
                                             } 
        );
        var action = cocoon.request.getParameter( "action" );
        if( action == "remove" )
        {
            var name = cocoon.request.getParameter( "name" );
            scheduler.removeJob( name );
        }
        else if( action == "add" )
        {
            jobname = cocoon.request.getParameter( "jobname" );
            message = cocoon.request.getParameter( "message" );
            sleep = cocoon.request.getParameter( "sleep" );
            pipeline = cocoon.request.getParameter( "pipeline" );
            cronexpr = cocoon.request.getParameter( "cronexpr" );
            intervalexpr = cocoon.request.getParameter( "intervalexpr" );
            atexpr = cocoon.request.getParameter( "atexpr" );
            
            var scheduletype = cocoon.request.getParameter( "cron" );
            if( scheduletype != null )
            {
                var params = new Packages.org.apache.avalon.framework.parameters.Parameters();
                params.setParameter( msg_param_key, message );
                var sleepms = sleep * 1000;
                params.setParameter( sleep_param_key, sleepms );
                params.setParameter( pipeline_param_key, pipeline );
                scheduler.addJob(jobname, testjobrole, cronexpr, false, params, null);
            }
            scheduletype = cocoon.request.getParameter( "periodic" );
            if( scheduletype != null )
            {
                var params = new Packages.org.apache.avalon.framework.parameters.Parameters();
                params.setParameter( msg_param_key, message );
                var sleepms = sleep * 1000;
                params.setParameter( sleep_param_key, sleepms );
                params.setParameter( pipeline_param_key, pipeline );                
                scheduler.addPeriodicJob(jobname, testjobrole, intervalexpr, false, params, null);
            }
            scheduletype = cocoon.request.getParameter( "at" );
            if( scheduletype != null )
            {
                var params = new Packages.org.apache.avalon.framework.parameters.Parameters();
                params.setParameter( msg_param_key, message );
                var sleepms = sleep * 1000;
                params.setParameter( sleep_param_key, sleepms );
                params.setParameter( pipeline_param_key, pipeline );                
                var date = formatter.parse( atexpr );
                scheduler.fireJobAt(date, jobname, testjobrole, params, null)
            }
            scheduletype = cocoon.request.getParameter( "immediately" );
            if( scheduletype != null )
            {
                var params = new Packages.org.apache.avalon.framework.parameters.Parameters();
                params.setParameter( msg_param_key, message );
                var sleepms = sleep * 1000;
                params.setParameter( sleep_param_key, sleepms );
                params.setParameter( pipeline_param_key, pipeline );                
                scheduler.fireJob(testjobrole, params, null)
            }
        }
        else
        {
            // do a refresh
        }
    }
}