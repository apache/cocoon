function call*() {
    before(): {
        cocoon.log.info( "before:call*" );
    }
    after(): {
        cocoon.log.info( "after:call*" );
    }    
}

function testSendPageAndWait() {
    continueExecution(): {
        cocoon.log.info( "continueExecution:testSendPageAndWait" );          
    }   
    stopExecution(): {
        cocoon.log.info( "stopExecution:testSendPageAndWait" );          
    }       
}