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
//MSIE
function f(e){
	if (e.className=="ci") {
		if (e.children(0).innerText.indexOf("\n")>0)
			fix(e,"cb");
	}
	if (e.className=="di") {
		if (e.children(0).innerText.indexOf("\n")>0)
			fix(e,"db");
	} e.id="";
}
function fix(e,cl){
	e.className=cl;
	e.style.display="block";
	j=e.parentElement.children(0);
	j.className="c";
	k=j.children(0);
	k.style.visibility="visible";
	k.href="#";
}
function ch(e) {
	mark=e.children(0).children(0);
	if (mark.innerText=="+") {
		mark.innerText="-";
		for (var i=1;i<e.children.length;i++) {
			e.children(i).style.display="block";
		}
	}
	else if (mark.innerText=="-") {
		mark.innerText="+";
		for (var i=1;i<e.children.length;i++) {
			e.children(i).style.display="none";
		}
	}
}
function ch2(e) {
	mark=e.children(0).children(0);
	contents=e.children(1);
	if (mark.innerText=="+") {
		mark.innerText="-";
		if (contents.className=="db"||contents.className=="cb") {
			contents.style.display="block";
		}
		else {
			contents.style.display="inline";
		}
	}
	else if (mark.innerText=="-") {
		mark.innerText="+";
		contents.style.display="none";
	}
}
function cl() {
	e=window.event.srcElement;
	if (e.className!="c") {
		e=e.parentElement;
		if (e.className!="c") {
			return;
		}
	}
	e=e.parentElement;
	if (e.className=="e") {
		ch(e);
	}
	if (e.className=="k") {
		ch2(e);
	}
}

//mozilla
function moz_f(){
	clean=document.getElementsByName('clean');
	for(i=0; i<clean.length;i++)
	{
		e = clean[i];
		if (e.className=="ci") {
			if (e.childNodes[1].childNodes[0].nodeValue.indexOf("\n")>0)
				moz_fix(e,"cb");
		}
		if (e.className=="di") {
			if (e.childNodes[1].nodeValue.indexOf("\n")>0)
				moz_fix(e,"db");
		}
	}
}
function moz_fix(e,cl){
	e.className=cl;
	e.style.display="block";
	j=e.parentNode.childNodes[1];
	j.className="c";
	k=j.childNodes[0];
	k.style.visibility="visible";
	k.href="#";
}
function moz_ch(e) {
	mark = e.childNodes[1].childNodes[1];

	if (mark.childNodes[0].nodeValue=="+") {
		mark.childNodes[0].nodeValue="-";
		for (var i=2;i<e.childNodes.length;i++) {
			if(e.childNodes[i].nodeName != "#text")
				e.childNodes[i].style.display="block";
		}
	}
	else if (mark.childNodes[0].nodeValue=="-") {
		mark.childNodes[0].nodeValue="+";
		for (var i=2;i<e.childNodes.length;i++) {
			if(e.childNodes[i].nodeName != "#text")
				e.childNodes[i].style.display="none";
		}
	}
}
function moz_ch2(e) {
	mark = e.childNodes[1].childNodes[0];
	contents=e.childNodes[2];
	if (mark.childNodes[0].nodeValue=="+") {
		mark.childNodes[0].nodeValue="-";
		if (contents.className=="db"||contents.className=="cb") {
			contents.style.display="block";
		}
		else {
			contents.style.display="inline";
		}
	}
	else if (mark.childNodes[0].nodeValue=="-") {
		mark.childNodes[0].nodeValue="+";
		contents.style.display="none";
	}
}

function moz_cl(evnt) {
	e=evnt.target.parentNode;

	if (e.className != "c") {
		e=e.parentNode
		if (e.className!="c") {
			return;
		}
	}
	e=e.parentNode
	if (e.className=="e") {
		moz_ch(e);
	}
	if (e.className=="k") {
		moz_ch2(e);
	}
}

function ex(){}
function h(){window.status=" ";}
if(document.all)
	document.onclick=cl;
else if(document.getElementById)
	document.onclick=moz_cl;
