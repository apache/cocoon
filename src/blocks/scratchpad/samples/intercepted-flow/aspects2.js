function interception* {
    before(): {
        cocoon.log.info( "before:interception*" ); 
    }
    stopExecution(): {
        cocoon.log.info( "stopExecution:interception*" );          
    }      
}