/*                      status='javascript loading';
//                      alert('loading');

function test()
{
    alert("test");
}
*/

                  function Toggle(button)
                  {
//                      alert('toggle');
                      ypos = document.body.scrollTop;
//                      alert(button.firstChild.src);

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
                      
//                      alert(srcparts[0] + "_" + srcparts[1]);

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
//                      alert('javascript loaded');
