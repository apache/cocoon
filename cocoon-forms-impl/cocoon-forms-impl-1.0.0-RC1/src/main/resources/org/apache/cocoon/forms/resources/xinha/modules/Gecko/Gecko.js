Gecko._pluginInfo={name:"Gecko",origin:"Xinha Core",version:"$LastChangedRevision: 808 $".replace(/^[^:]*: (.*) \$$/,"$1"),developer:"The Xinha Core Developer Team",developer_url:"$HeadURL: http://svn.xinha.python-hosting.com/trunk/modules/Gecko/Gecko.js $".replace(/^[^:]*: (.*) \$$/,"$1"),sponsor:"",sponsor_url:"",license:"htmlArea"};
function Gecko(_1){
this.editor=_1;
_1.Gecko=this;
}
Gecko.prototype.onKeyPress=function(ev){
var _3=this.editor;
var s=_3.getSelection();
if(_3.isShortCut(ev)){
switch(_3.getKey(ev).toLowerCase()){
case "z":
if(_3._unLink&&_3._unlinkOnUndo){
Xinha._stopEvent(ev);
_3._unLink();
_3.updateToolbar();
return true;
}
break;
case "a":
sel=_3.getSelection();
sel.removeAllRanges();
range=_3.createRange();
range.selectNodeContents(_3._doc.body);
sel.addRange(range);
Xinha._stopEvent(ev);
return true;
break;
case "v":
if(!_3.config.htmlareaPaste){
return true;
}
break;
}
}
switch(_3.getKey(ev)){
case " ":
var _5=function(_6,_7){
var _8=_6.nextSibling;
if(typeof _7=="string"){
_7=_3._doc.createElement(_7);
}
var a=_6.parentNode.insertBefore(_7,_8);
Xinha.removeFromParent(_6);
a.appendChild(_6);
_8.data=" "+_8.data;
s.collapse(_8,1);
_3._unLink=function(){
var t=a.firstChild;
a.removeChild(t);
a.parentNode.insertBefore(t,a);
Xinha.removeFromParent(a);
_3._unLink=null;
_3._unlinkOnUndo=false;
};
_3._unlinkOnUndo=true;
return a;
};
if(_3.config.convertUrlsToLinks&&s&&s.isCollapsed&&s.anchorNode.nodeType==3&&s.anchorNode.data.length>3&&s.anchorNode.data.indexOf(".")>=0){
var _b=s.anchorNode.data.substring(0,s.anchorOffset).search(/\S{4,}$/);
if(_b==-1){
break;
}
if(_3._getFirstAncestor(s,"a")){
break;
}
var _c=s.anchorNode.data.substring(0,s.anchorOffset).replace(/^.*?(\S*)$/,"$1");
var _d=_c.match(Xinha.RE_email);
if(_d){
var _e=s.anchorNode;
var _f=_e.splitText(s.anchorOffset);
var _10=_e.splitText(_b);
_5(_10,"a").href="mailto:"+_d[0];
break;
}
RE_date=/([0-9]+\.)+/;
RE_ip=/(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)/;
var _11=_c.match(Xinha.RE_url);
if(_11){
if(RE_date.test(_c)){
if(!RE_ip.test(_c)){
break;
}
}
var _12=s.anchorNode;
var _13=_12.splitText(s.anchorOffset);
var _14=_12.splitText(_b);
_5(_14,"a").href=(_11[1]?_11[1]:"http://")+_11[2];
break;
}
}
break;
}
switch(ev.keyCode){
case 27:
if(_3._unLink){
_3._unLink();
Xinha._stopEvent(ev);
}
break;
break;
case 8:
case 46:
if(!ev.shiftKey&&this.handleBackspace()){
Xinha._stopEvent(ev);
}
default:
_3._unlinkOnUndo=false;
if(s.anchorNode&&s.anchorNode.nodeType==3){
var a=_3._getFirstAncestor(s,"a");
if(!a){
break;
}
if(!a._updateAnchTimeout){
if(s.anchorNode.data.match(Xinha.RE_email)&&a.href.match("mailto:"+s.anchorNode.data.trim())){
var _16=s.anchorNode;
var _17=function(){
a.href="mailto:"+_16.data.trim();
a._updateAnchTimeout=setTimeout(_17,250);
};
a._updateAnchTimeout=setTimeout(_17,1000);
break;
}
var m=s.anchorNode.data.match(Xinha.RE_url);
if(m&&a.href.match(s.anchorNode.data.trim())){
var _19=s.anchorNode;
var _1a=function(){
m=_19.data.match(Xinha.RE_url);
if(m){
a.href=(m[1]?m[1]:"http://")+m[2];
}
a._updateAnchTimeout=setTimeout(_1a,250);
};
a._updateAnchTimeout=setTimeout(_1a,1000);
}
}
}
break;
}
return false;
};
Gecko.prototype.handleBackspace=function(){
var _1b=this.editor;
setTimeout(function(){
var sel=_1b.getSelection();
var _1d=_1b.createRange(sel);
var SC=_1d.startContainer;
var SO=_1d.startOffset;
var EC=_1d.endContainer;
var EO=_1d.endOffset;
var _22=SC.nextSibling;
if(SC.nodeType==3){
SC=SC.parentNode;
}
if(!(/\S/.test(SC.tagName))){
var p=document.createElement("p");
while(SC.firstChild){
p.appendChild(SC.firstChild);
}
SC.parentNode.insertBefore(p,SC);
Xinha.removeFromParent(SC);
var r=_1d.cloneRange();
r.setStartBefore(_22);
r.setEndAfter(_22);
r.extractContents();
sel.removeAllRanges();
sel.addRange(r);
}
},10);
};
Gecko.prototype.inwardHtml=function(_25){
_25=_25.replace(/<(\/?)strong(\s|>|\/)/ig,"<$1b$2");
_25=_25.replace(/<(\/?)em(\s|>|\/)/ig,"<$1i$2");
_25=_25.replace(/<(\/?)del(\s|>|\/)/ig,"<$1strike$2");
return _25;
};
Gecko.prototype.outwardHtml=function(_26){
_26=_26.replace(/<script[\s]*src[\s]*=[\s]*['"]chrome:\/\/.*?["']>[\s]*<\/script>/ig,"");
return _26;
};
Gecko.prototype.onExecCommand=function(_27,UI,_29){
try{
this.editor._doc.execCommand("useCSS",false,true);
this.editor._doc.execCommand("styleWithCSS",false,false);
}
catch(ex){
}
switch(_27){
case "paste":
alert(Xinha._lc("The Paste button does not work in Mozilla based web browsers (technical security reasons). Press CTRL-V on your keyboard to paste directly."));
return true;
}
return false;
};
Gecko.prototype.onMouseDown=function(ev){
if(ev.target.tagName.toLowerCase()=="hr"){
var sel=this.editor.getSelection();
var _2c=this.editor.createRange(sel);
_2c.selectNode(ev.target);
}
};
Xinha.prototype.insertNodeAtSelection=function(_2d){
var sel=this.getSelection();
var _2f=this.createRange(sel);
sel.removeAllRanges();
_2f.deleteContents();
var _30=_2f.startContainer;
var pos=_2f.startOffset;
var _32=_2d;
switch(_30.nodeType){
case 3:
if(_2d.nodeType==3){
_30.insertData(pos,_2d.data);
_2f=this.createRange();
_2f.setEnd(_30,pos+_2d.length);
_2f.setStart(_30,pos+_2d.length);
sel.addRange(_2f);
}else{
_30=_30.splitText(pos);
if(_2d.nodeType==11){
_32=_32.firstChild;
}
_30.parentNode.insertBefore(_2d,_30);
this.selectNodeContents(_32);
this.updateToolbar();
}
break;
case 1:
if(_2d.nodeType==11){
_32=_32.firstChild;
}
_30.insertBefore(_2d,_30.childNodes[pos]);
this.selectNodeContents(_32);
this.updateToolbar();
break;
}
};
Xinha.prototype.getParentElement=function(sel){
if(typeof sel=="undefined"){
sel=this.getSelection();
}
var _34=this.createRange(sel);
try{
var p=_34.commonAncestorContainer;
if(!_34.collapsed&&_34.startContainer==_34.endContainer&&_34.startOffset-_34.endOffset<=1&&_34.startContainer.hasChildNodes()){
p=_34.startContainer.childNodes[_34.startOffset];
}
while(p.nodeType==3){
p=p.parentNode;
}
return p;
}
catch(ex){
return null;
}
};
Xinha.prototype.activeElement=function(sel){
if((sel===null)||this.selectionEmpty(sel)){
return null;
}
if(!sel.isCollapsed){
if(sel.anchorNode.childNodes.length>sel.anchorOffset&&sel.anchorNode.childNodes[sel.anchorOffset].nodeType==1){
return sel.anchorNode.childNodes[sel.anchorOffset];
}else{
if(sel.anchorNode.nodeType==1){
return sel.anchorNode;
}else{
return null;
}
}
}
return null;
};
Xinha.prototype.selectionEmpty=function(sel){
if(!sel){
return true;
}
if(typeof sel.isCollapsed!="undefined"){
return sel.isCollapsed;
}
return true;
};
Xinha.prototype.saveSelection=function(){
return this.createRange(this.getSelection()).cloneRange();
};
Xinha.prototype.restoreSelection=function(_38){
var sel=this.getSelection();
sel.removeAllRanges();
sel.addRange(_38);
};
Xinha.prototype.selectNodeContents=function(_3a,pos){
this.focusEditor();
this.forceRedraw();
var _3c;
var _3d=typeof pos=="undefined"?true:false;
var sel=this.getSelection();
_3c=this._doc.createRange();
if(_3d&&_3a.tagName&&_3a.tagName.toLowerCase().match(/table|img|input|textarea|select/)){
_3c.selectNode(_3a);
}else{
_3c.selectNodeContents(_3a);
}
sel.removeAllRanges();
sel.addRange(_3c);
};
Xinha.prototype.insertHTML=function(_3f){
var sel=this.getSelection();
var _41=this.createRange(sel);
this.focusEditor();
var _42=this._doc.createDocumentFragment();
var div=this._doc.createElement("div");
div.innerHTML=_3f;
while(div.firstChild){
_42.appendChild(div.firstChild);
}
var _44=this.insertNodeAtSelection(_42);
};
Xinha.prototype.getSelectedHTML=function(){
var sel=this.getSelection();
var _46=this.createRange(sel);
return Xinha.getHTML(_46.cloneContents(),false,this);
};
Xinha.prototype.getSelection=function(){
return this._iframe.contentWindow.getSelection();
};
Xinha.prototype.createRange=function(sel){
this.activateEditor();
if(typeof sel!="undefined"){
try{
return sel.getRangeAt(0);
}
catch(ex){
return this._doc.createRange();
}
}else{
return this._doc.createRange();
}
};
Xinha.prototype.isKeyEvent=function(_48){
return _48.type=="keypress";
};
Xinha.prototype.getKey=function(_49){
return String.fromCharCode(_49.charCode);
};
Xinha.getOuterHTML=function(_4a){
return (new XMLSerializer()).serializeToString(_4a);
};
Xinha.prototype.cc=String.fromCharCode(173);
Xinha.prototype.setCC=function(_4b){
try{
if(_4b=="textarea"){
var ta=this._textArea;
var _4d=ta.selectionStart;
var _4e=ta.value.substring(0,_4d);
var _4f=ta.value.substring(_4d,ta.value.length);
if(_4f.match(/^[^<]*>/)){
var _50=_4f.indexOf(">")+1;
ta.value=_4e+_4f.substring(0,_50)+this.cc+_4f.substring(_50,_4f.length);
}else{
ta.value=_4e+this.cc+_4f;
}
}else{
var sel=this.getSelection();
sel.getRangeAt(0).insertNode(document.createTextNode(this.cc));
}
}
catch(e){
}
};
Xinha.prototype.findCC=function(_52){
try{
var _53=(_52=="textarea")?window:this._iframe.contentWindow;
if(_53.find(this.cc)){
if(_52=="textarea"){
var ta=this._textArea;
var _55=pos=ta.selectionStart;
var end=ta.selectionEnd;
var _57=ta.scrollTop;
ta.value=ta.value.substring(0,_55)+ta.value.substring(end,ta.value.length);
ta.selectionStart=pos;
ta.selectionEnd=pos;
ta.scrollTop=_57;
ta.focus();
}else{
var sel=this.getSelection();
sel.getRangeAt(0).deleteContents();
this._iframe.contentWindow.focus();
}
}
}
catch(e){
}
};
Xinha.prototype._standardToggleBorders=Xinha.prototype._toggleBorders;
Xinha.prototype._toggleBorders=function(){
var _59=this._standardToggleBorders();
var _5a=this._doc.getElementsByTagName("TABLE");
for(var i=0;i<_5a.length;i++){
_5a[i].style.display="none";
_5a[i].style.display="table";
}
return _59;
};
Xinha.getDoctype=function(doc){
var d="";
if(doc.doctype){
d+="<!DOCTYPE "+doc.doctype.name+" PUBLIC ";
d+=doc.doctype.publicId?"\""+doc.doctype.publicId+"\"":"";
d+=doc.doctype.systemId?" \""+doc.doctype.systemId+"\"":"";
d+=">";
}
return d;
};
