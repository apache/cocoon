                  function Toggle(button)
                  {
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
                      
/*
                      if (button.firstChild.src.indexOf('-op.gif')>=0)
                      {
                          button.firstChild.src= button.firstChild.src.substr(1,pos)+ 'neer.gif';
                          button.parentNode.nextSibling.style.display='';
                      }
                      else if (button.firstChild.src.indexOf('-neer.gif')>=0)
                      {
                          button.firstChild.src= button.firstChild.src.substr(1,pos)+ 'op.gif';
                          button.parentNode.nextSibling.style.display='none';
                      }
                      window.setTimeout('window.scrollTo(0,ypos);',100);
*/                      
                  }

                
                  function DoRecursive(node,newDisplay,newImgSuffix)
                  {
                    if (node.hasChildNodes())
                    {                      
                      if (node.style!=null && node.style.display!=newDisplay)
                      {
                          if (node.previousSibling && node.previousSibling.firstChild && node.previousSibling.firstChild.firstChild && node.previousSibling.firstChild.firstChild.src )
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
                      
/*
                      if (button.firstChild.src.indexOf('plus-op.gif')>=0)
                      {
                          DoRecursive(button.parentNode.nextSibling,'','plus-neer.gif');        
                      }
                      else if (button.firstChild.src.indexOf('pijl-op.gif')>=0)
                      {
                          DoRecursive(button.parentNode.nextSibling,'','pijl-neer.gif');        
                      }
                      else if (button.firstChild.src.indexOf('pijl-neer.gif')>=0)
                      {
                          DoRecursive(button.parentNode.nextSibling,'','pijl-op.gif');        
                      }
                      else
                      {
                          DoRecursive(button.parentNode.nextSibling,'none','plus-op.gif');
                      }
*/
                      event.cancelBubble=true;
                      return false;
                  }
