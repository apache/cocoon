#  Copyright 2006 The Apache Software Foundation
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License
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
