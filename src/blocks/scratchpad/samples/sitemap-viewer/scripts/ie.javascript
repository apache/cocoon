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

function Toggle(button)
{
    ypos = document.body.scrollTop;

    src = button.firstChild.src.split('/');
    srcparts = src[src.length-1].split('_');
    if (srcparts[1]=='op.gif')
    {
        button.firstChild.src= srcparts[0] + '_neer.gif';
        button.parentNode.nextSibling.style.display='';
    }
    else if (srcparts[1]=='neer.gif')
    {
        button.firstChild.src= srcparts[0] + '_op.gif';
        button.parentNode.nextSibling.style.display='none';
    }
    
}


function DoRecursive(node,newDisplay,newImgSuffix)
{
  if (node.hasChildNodes())
  {                      
    if (node.style!=null && node.style.display!=newDisplay)
    {
        if (node.previousSibling && node.previousSibling.firstChild && node.previousSibling.firstChild.firstChild && node.previousSibling.firstChild.firstChild.src && node.previousSibling.firstChild.firstChild.src.indexOf('_pre') <0 )
        {
            node.style.display=newDisplay;

            src = node.previousSibling.firstChild.firstChild.src.split('/');
            srcparts = src[src.length-1].split('_');

            node.previousSibling.firstChild.firstChild.src=srcparts[0]+newImgSuffix;
        }
    }
    DoRecursive(node.firstChild,newDisplay,newImgSuffix);
  }
  if (node.nextSibling)
    DoRecursive(node.nextSibling,newDisplay,newImgSuffix);
}



function ToggleAll(button)
{
    src = button.firstChild.src.split('/');
    srcparts = src[src.length-1].split('_');
    
    if (srcparts[1]=='op.gif')
    {
        DoRecursive(button.parentNode.nextSibling,'','_neer.gif');        
    }
    else if (srcparts[1]=='neer.gif')
    {
        DoRecursive(button.parentNode.nextSibling,'none','_op.gif');        
    }
    
    event.cancelBubble=true;
    return false;
}
