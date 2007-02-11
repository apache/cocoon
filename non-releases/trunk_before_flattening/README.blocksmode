Info about the Cocoon "blocks mode".

From http://marc.theaimsgroup.com/?l=xml-cocoon-dev&m=113645993019371&w=2

The block mode is currently in a state of flux. You could use Cocoon in blocks 
mode a month ago by running "./cocoon.sh blocks". That worked by making the root 
Processor pluggable in the CocoonServlet and using the BlocksManager instead 
of Cocoon.

To make that possible, I had to have a rather complicated initalization sequence 
for the blocks. After the NG discussions I decided to simplify the architecture 
by refactoring the block architecture so that the BlocksManager becomes a top 
level servlet instead. And so that the ServiceManager and Avalon context 
creation happen locally at the block level instead of globaly.

This is ongoing work and currently the blockmode is only usable from the test 
cases.
