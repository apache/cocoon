// ===================================================================
// Author: Matt Kruse <matt@mattkruse.com>
// WWW: http://www.mattkruse.com/
//
// NOTICE: You may use this code for any purpose, commercial or
// private, without any further permission from the author. You may
// remove this notice from your final code if you wish, however it is
// appreciated by the author if at least my web site address is kept.
//
// You may *NOT* re-distribute this code in any way except through its
// use. That means, you can include it in your product, or your web
// site, or any other form where the code is actually being used. You
// may not put the plain javascript up on your site for download or
// include it in your javascript libraries for download. 
// If you wish to share this code with others, please just point them
// to the URL instead.
// Please DO NOT link directly to my .js files from your site. Copy
// the files to your server and use them there. Thank you.
// ===================================================================

// HISTORY
// ------------------------------------------------------------------
// August 19, 2003: Renamed the function to get styles, and made it
//      work correctly without an object reference
// August 18, 2003: Changed showYearNavigation and 
//      showYearNavigationInput to optionally take an argument of
//      true or false
// July 31, 2003: Added text input option for year navigation.
//      Added a per-calendar CSS prefix option to optionally use 
//      different styles for different calendars.
// July 29, 2003: Fixed bug causing the Today link to be clickable 
//      even though today falls in a disabled date range.
//      Changed formatting to use pure CSS, allowing greater control
//      over look-and-feel options.
// June 11, 2003: Fixed bug causing the Today link to be unselectable
//      under certain cases when some days of week are disabled
// March 14, 2003: Added ability to disable individual dates or date
//      ranges, display as light gray and strike-through
// March 14, 2003: Removed dependency on graypixel.gif and instead 
///     use table border coloring
// March 12, 2003: Modified showCalendar() function to allow optional
//      start-date parameter
// March 11, 2003: Modified select() function to allow optional
//      start-date parameter
/* 
DESCRIPTION: This object implements a popup calendar to allow the user to
select a date, month, quarter, or year.

COMPATIBILITY: Works with Netscape 4.x, 6.x, IE 5.x on Windows. Some small
positioning errors - usually with Window positioning - occur on the 
Macintosh platform.
The calendar can be modified to work for any location in the world by 
changing which weekday is displayed as the first column, changing the month
names, and changing the column headers for each day.

USAGE:
// Create a new CalendarPopup object of type WINDOW
var cal = new CalendarPopup(); 

// Create a new CalendarPopup object of type DIV using the DIV named 'mydiv'
var cal = new CalendarPopup('mydiv'); 

// Easy method to link the popup calendar with an input box. 
cal.select(inputObject, anchorname, dateFormat);
// Same method, but passing a default date other than the field's current value
cal.select(inputObject, anchorname, dateFormat, '01/02/2000');
// This is an example call to the popup calendar from a link to populate an 
// input box. Note that to use this, date.js must also be included!!
<A HREF="#" onClick="cal.select(document.forms[0].date,'anchorname','MM/dd/yyyy'); return false;">Select</A>

// Set the type of date select to be used. By default it is 'date'.
cal.setDisplayType(type);

// When a date, month, quarter, or year is clicked, a function is called and
// passed the details. You must write this function, and tell the calendar
// popup what the function name is.
// Function to be called for 'date' select receives y, m, d
cal.setReturnFunction(functionname);
// Function to be called for 'month' select receives y, m
cal.setReturnMonthFunction(functionname);
// Function to be called for 'quarter' select receives y, q
cal.setReturnQuarterFunction(functionname);
// Function to be called for 'year' select receives y
cal.setReturnYearFunction(functionname);

// Show the calendar relative to a given anchor
cal.showCalendar(anchorname);

// Hide the calendar. The calendar is set to autoHide automatically
cal.hideCalendar();

// Set the month names to be used. Default are English month names
cal.setMonthNames("January","February","March",...);

// Set the month abbreviations to be used. Default are English month abbreviations
cal.setMonthAbbreviations("Jan","Feb","Mar",...);

// Set the text to be used above each day column. The days start with 
// sunday regardless of the value of WeekStartDay
cal.setDayHeaders("S","M","T",...);

// Set the day for the first column in the calendar grid. By default this
// is Sunday (0) but it may be changed to fit the conventions of other
// countries.
cal.setWeekStartDay(1); // week is Monday - Saturday

// Set the weekdays which should be disabled in the 'date' select popup. You can
// then allow someone to only select week end dates, or Tuedays, for example
cal.setDisabledWeekDays(0,1); // To disable selecting the 1st or 2nd days of the week

// Selectively disable individual days or date ranges. Disabled days will not
// be clickable, and show as strike-through text on current browsers.
// Date format is any format recognized by parseDate() in date.js
// Pass a single date to disable:
cal.addDisabledDates("2003-01-01");
// Pass null as the first parameter to mean "anything up to and including" the
// passed date:
cal.addDisabledDates(null, "01/02/03");
// Pass null as the second parameter to mean "including the passed date and
// anything after it:
cal.addDisabledDates("Jan 01, 2003", null);
// Pass two dates to disable all dates inbetween and including the two
cal.addDisabledDates("January 01, 2003", "Dec 31, 2003");

// When the 'year' select is displayed, set the number of years back from the 
// current year to start listing years. Default is 2.
cal.setYearSelectStartOffset(2);

// Text for the word "Today" appearing on the calendar
cal.setTodayText("Today");

// The calendar uses CSS classes for formatting. If you want your calendar to
// have unique styles, you can set the prefix that will be added to all the
// classes in the output.
// For example, normal output may have this:
//     <SPAN CLASS="cpTodayTextDisabled">Today<SPAN>
// But if you set the prefix like this:
cal.setCssPrefix("Test");
// The output will then look like:
//     <SPAN CLASS="TestcpTodayTextDisabled">Today<SPAN>
// And you can define that style somewhere in your page.

// When using Year navigation, you can make the year be an input box, so
// the user can manually change it and jump to any year
cal.showYearNavigationInput();

// Set the calendar offset to be different than the default. By default it
// will appear just below and to the right of the anchorname. So if you have
// a text box where the date will go and and anchor immediately after the
// text box, the calendar will display immediately under the text box.
cal.offsetX = 20;
cal.offsetY = 20;

NOTES:
1) Requires the functions in AnchorPosition.js and PopupWindow.js

2) Your anchor tag MUST contain both NAME and ID attributes which are the 
   same. For example:
   <A NAME="test" ID="test"> </A>

3) There must be at least a space between <A> </A> for IE5.5 to see the 
   anchor tag correctly. Do not do <A></A> with no space.

4) When a CalendarPopup object is created, a handler for 'onmouseup' is
   attached to any event handler you may have already defined. Do NOT define
   an event handler for 'onmouseup' after you define a CalendarPopup object 
   or the autoHide() will not work correctly.
   
5) The calendar popup display uses style sheets to make it look nice.

*/ 

// CONSTRUCTOR for the CalendarPopup Object
function CalendarPopup() {
	var c;
	if (arguments.length>0) {
		c = new PopupWindow(arguments[0]);
		}
	else {
		c = new PopupWindow();
		c.setSize(150,175);
		}
	c.offsetX = -152;
	c.offsetY = 25;
	c.autoHide();
	// Calendar-specific properties
	c.monthNames = new Array("January","February","March","April","May","June","July","August","September","October","November","December");
	c.monthAbbreviations = new Array("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec");
	c.dayHeaders = new Array("S","M","T","W","T","F","S");
	c.returnFunction = "CP_tmpReturnFunction";
	c.returnMonthFunction = "CP_tmpReturnMonthFunction";
	c.returnQuarterFunction = "CP_tmpReturnQuarterFunction";
	c.returnYearFunction = "CP_tmpReturnYearFunction";
	c.weekStartDay = 0;
	c.isShowYearNavigation = false;
	c.displayType = "date";
	c.disabledWeekDays = new Object();
	c.disabledDatesExpression = "";
	c.yearSelectStartOffset = 2;
	c.currentDate = null;
	c.todayText="Today";
	c.cssPrefix="";
	c.isShowYearNavigationInput=false;
	window.CP_targetInput = null;
	window.CP_dateFormat = "MM/dd/yyyy";
	// Method mappings
	c.setReturnFunction = CP_setReturnFunction;
	c.setReturnMonthFunction = CP_setReturnMonthFunction;
	c.setReturnQuarterFunction = CP_setReturnQuarterFunction;
	c.setReturnYearFunction = CP_setReturnYearFunction;
	c.setMonthNames = CP_setMonthNames;
	c.setMonthAbbreviations = CP_setMonthAbbreviations;
	c.setDayHeaders = CP_setDayHeaders;
	c.setWeekStartDay = CP_setWeekStartDay;
	c.setDisplayType = CP_setDisplayType;
	c.setDisabledWeekDays = CP_setDisabledWeekDays;
	c.addDisabledDates = CP_addDisabledDates;
	c.setYearSelectStartOffset = CP_setYearSelectStartOffset;
	c.setTodayText = CP_setTodayText;
	c.showYearNavigation = CP_showYearNavigation;
	c.showCalendar = CP_showCalendar;
	c.hideCalendar = CP_hideCalendar;
	c.getStyles = getCalendarStyles;
	c.refreshCalendar = CP_refreshCalendar;
	c.getCalendar = CP_getCalendar;
	c.select = CP_select;
	c.setCssPrefix = CP_setCssPrefix;
	c.showYearNavigationInput = CP_showYearNavigationInput
	// Return the object
	return c;
	}

// Temporary default functions to be called when items clicked, so no error is thrown
function CP_tmpReturnFunction(y,m,d) { 
	if (window.CP_targetInput!=null) {
		var dt = new Date(y,m-1,d,0,0,0);
		window.CP_targetInput.value = formatDate(dt,window.CP_dateFormat);
		}
	else {
		alert('Use setReturnFunction() to define which function will get the clicked results!'); 
		}
	}
function CP_tmpReturnMonthFunction(y,m) { 
	alert('Use setReturnMonthFunction() to define which function will get the clicked results!\nYou clicked: year='+y+' , month='+m); 
	}
function CP_tmpReturnQuarterFunction(y,q) { 
	alert('Use setReturnQuarterFunction() to define which function will get the clicked results!\nYou clicked: year='+y+' , quarter='+q); 
	}
function CP_tmpReturnYearFunction(y) { 
	alert('Use setReturnYearFunction() to define which function will get the clicked results!\nYou clicked: year='+y); 
	}

// Set the name of the functions to call to get the clicked item
function CP_setReturnFunction(name) { this.returnFunction = name; }
function CP_setReturnMonthFunction(name) { this.returnMonthFunction = name; }
function CP_setReturnQuarterFunction(name) { this.returnQuarterFunction = name; }
function CP_setReturnYearFunction(name) { this.returnYearFunction = name; }

// Over-ride the built-in month names
function CP_setMonthNames() {
	for (var i=0; i<arguments.length; i++) { this.monthNames[i] = arguments[i]; }
	}

// Over-ride the built-in month abbreviations
function CP_setMonthAbbreviations() {
	for (var i=0; i<arguments.length; i++) { this.monthAbbreviations[i] = arguments[i]; }
	}

// Over-ride the built-in column headers for each day
function CP_setDayHeaders() {
	for (var i=0; i<arguments.length; i++) { this.dayHeaders[i] = arguments[i]; }
	}

// Set the day of the week (0-7) that the calendar display starts on
// This is for countries other than the US whose calendar displays start on Monday(1), for example
function CP_setWeekStartDay(day) { this.weekStartDay = day; }

// Show next/last year navigation links
function CP_showYearNavigation() { this.isShowYearNavigation = (arguments.length>0)?arguments[0]:true; }

// Which type of calendar to display
function CP_setDisplayType(type) {
	if (type!="date"&&type!="week-end"&&type!="month"&&type!="quarter"&&type!="year") { alert("Invalid display type! Must be one of: date,week-end,month,quarter,year"); return false; }
	this.displayType=type;
	}

// How many years back to start by default for year display
function CP_setYearSelectStartOffset(num) { this.yearSelectStartOffset=num; }

// Set which weekdays should not be clickable
function CP_setDisabledWeekDays() {
	this.disabledWeekDays = new Object();
	for (var i=0; i<arguments.length; i++) { this.disabledWeekDays[arguments[i]] = true; }
	}
	
// Disable individual dates or ranges
// Builds an internal logical test which is run via eval() for efficiency
function CP_addDisabledDates(start, end) {
	if (arguments.length==1) { end=start; }
	if (start==null && end==null) { return; }
	if (this.disabledDatesExpression!="") { this.disabledDatesExpression+= "||"; }
	if (start!=null) { start = parseDate(start); start=""+start.getFullYear()+LZ(start.getMonth()+1)+LZ(start.getDate());}
	if (end!=null) { end=parseDate(end); end=""+end.getFullYear()+LZ(end.getMonth()+1)+LZ(end.getDate());}
	if (start==null) { this.disabledDatesExpression+="(ds<="+end+")"; }
	else if (end  ==null) { this.disabledDatesExpression+="(ds>="+start+")"; }
	else { this.disabledDatesExpression+="(ds>="+start+"&&ds<="+end+")"; }
	}
	
// Set the text to use for the "Today" link
function CP_setTodayText(text) {
	this.todayText = text;
	}

// Set the prefix to be added to all CSS classes when writing output
function CP_setCssPrefix(val) { 
	this.cssPrefix = val; 
	}

// Show the year navigation as an input box that can be manually changed
function CP_showYearNavigationInput() { this.isShowYearNavigationInput = (arguments.length>0)?arguments[0]:true; }


// Hide a calendar object
function CP_hideCalendar() {
	if (arguments.length > 0) { window.popupWindowObjects[arguments[0]].hidePopup(); }
	else { this.hidePopup(); }
	}

// Refresh the contents of the calendar display
function CP_refreshCalendar(index) {
	var calObject = window.popupWindowObjects[index];
	if (arguments.length>1) { 
		calObject.populate(calObject.getCalendar(arguments[1],arguments[2],arguments[3],arguments[4],arguments[5]));
		}
	else {
		calObject.populate(calObject.getCalendar());
		}
	calObject.refresh();
	}

// Populate the calendar and display it
function CP_showCalendar(anchorname) {
	if (arguments.length>1) {
		if (arguments[1]==null||arguments[1]=="") {
			this.currentDate=new Date();
			}
		else {
			this.currentDate=new Date(parseDate(arguments[1]));
			}
		}
	this.populate(this.getCalendar());
	this.showPopup(anchorname);
	}

// Simple method to interface popup calendar with a text-entry box
function CP_select(inputobj, linkname, format) {
	var selectedDate=(arguments.length>3)?arguments[3]:null;
	if (!window.getDateFromFormat) {
		alert("calendar.select: To use this method you must also include 'date.js' for date formatting");
		return;
		}
	if (this.displayType!="date"&&this.displayType!="week-end") {
		alert("calendar.select: This function can only be used with displayType 'date' or 'week-end'");
		return;
		}
	if (inputobj.type!="text" && inputobj.type!="hidden" && inputobj.type!="textarea") { 
		alert("calendar.select: Input object passed is not a valid form input object"); 
		window.CP_targetInput=null;
		return;
		}
	window.CP_targetInput = inputobj;
	this.currentDate=null;
	var time=0;
	if (selectedDate!=null) {
		time = getDateFromFormat(selectedDate,format)
		}
	else if (inputobj.value!="") {
		time = getDateFromFormat(inputobj.value,format);
		}
	if (selectedDate!=null || inputobj.value!="") {
		if (time==0) { this.currentDate=null; }
		else { this.currentDate=new Date(time); }
		}
	window.CP_dateFormat = format;
	this.showCalendar(linkname);
	}
	
// Get style block needed to display the calendar correctly
function getCalendarStyles() {
	var result = "";
	var p = "";
	if (this!=null && typeof(this.cssPrefix)!="undefined" && this.cssPrefix!=null && this.cssPrefix!="") { p=this.cssPrefix; }
	result += "<STYLE>\n";
	result += "."+p+"cpYearNavigation,."+p+"cpMonthNavigation { background-color:#C0C0C0; text-align:center; vertical-align:center; text-decoration:none; color:#000000; font-weight:bold; }\n";
	result += "."+p+"cpDayColumnHeader, ."+p+"cpYearNavigation,."+p+"cpMonthNavigation,."+p+"cpCurrentMonthDate,."+p+"cpCurrentMonthDateDisabled,."+p+"cpOtherMonthDate,."+p+"cpOtherMonthDateDisabled,."+p+"cpCurrentDate,."+p+"cpCurrentDateDisabled,."+p+"cpTodayText,."+p+"cpTodayTextDisabled,."+p+"cpText { font-family:arial; font-size:8pt; }\n";
	result += "TD."+p+"cpDayColumnHeader { text-align:right; border:solid thin #C0C0C0;border-width:0 0 1 0; }\n";
	result += "."+p+"cpCurrentMonthDate, ."+p+"cpOtherMonthDate, ."+p+"cpCurrentDate  { text-align:right; text-decoration:none; }\n";
	result += "."+p+"cpCurrentMonthDateDisabled, ."+p+"cpOtherMonthDateDisabled, ."+p+"cpCurrentDateDisabled { color:#D0D0D0; text-align:right; text-decoration:line-through; }\n";
	result += "."+p+"cpCurrentMonthDate, .cpCurrentDate { color:#000000; }\n";
	result += "."+p+"cpOtherMonthDate { color:#808080; }\n";
	result += "TD."+p+"cpCurrentDate { color:white; background-color: #C0C0C0; border-width:1; border:solid thin #800000; }\n";
	result += "TD."+p+"cpCurrentDateDisabled { border-width:1; border:solid thin #FFAAAA; }\n";
	result += "TD."+p+"cpTodayText, TD."+p+"cpTodayTextDisabled { border:solid thin #C0C0C0; border-width:1 0 0 0;}\n";
	result += "A."+p+"cpTodayText, SPAN."+p+"cpTodayTextDisabled { height:20px; }\n";
	result += "A."+p+"cpTodayText { color:black; }\n";
	result += "."+p+"cpTodayTextDisabled { color:#D0D0D0; }\n";
	result += "."+p+"cpBorder { border:solid thin #808080; }\n";
	result += "</STYLE>\n";
	return result;
	}

// Return a string containing all the calendar code to be displayed
function CP_getCalendar() {
	var now = new Date();
	// Reference to window
	if (this.type == "WINDOW") { var windowref = "window.opener."; }
	else { var windowref = ""; }
	var result = "";
	// If POPUP, write entire HTML document
	if (this.type == "WINDOW") {
		result += "<HTML><HEAD><TITLE>Calendar</TITLE>"+this.getStyles()+"</HEAD><BODY MARGINWIDTH=0 MARGINHEIGHT=0 TOPMARGIN=0 RIGHTMARGIN=0 LEFTMARGIN=0>\n";
		result += '<CENTER><TABLE WIDTH=100% BORDER=0 BORDERWIDTH=0 CELLSPACING=0 CELLPADDING=0>\n';
		}
	else {
		result += '<TABLE CLASS="'+this.cssPrefix+'cpBorder" WIDTH=144 BORDER=1 BORDERWIDTH=1 CELLSPACING=0 CELLPADDING=1>\n';
		result += '<TR><TD ALIGN=CENTER>\n';
		result += '<CENTER>\n';
		}
	// Code for DATE display (default)
	// -------------------------------
	if (this.displayType=="date" || this.displayType=="week-end") {
		if (this.currentDate==null) { this.currentDate = now; }
		if (arguments.length > 0) { var month = arguments[0]; }
			else { var month = this.currentDate.getMonth()+1; }
		if (arguments.length > 1 && arguments[1]>0 && arguments[1]-0==arguments[1]) { var year = arguments[1]; }
			else { var year = this.currentDate.getFullYear(); }
		var daysinmonth= new Array(0,31,28,31,30,31,30,31,31,30,31,30,31);
		if ( ( (year%4 == 0)&&(year%100 != 0) ) || (year%400 == 0) ) {
			daysinmonth[2] = 29;
			}
		var current_month = new Date(year,month-1,1);
		var display_year = year;
		var display_month = month;
		var display_date = 1;
		var weekday= current_month.getDay();
		var offset = 0;
		
		offset = (weekday >= this.weekStartDay) ? weekday-this.weekStartDay : 7-this.weekStartDay+weekday ;
		if (offset > 0) {
			display_month--;
			if (display_month < 1) { display_month = 12; display_year--; }
			display_date = daysinmonth[display_month]-offset+1;
			}
		var next_month = month+1;
		var next_month_year = year;
		if (next_month > 12) { next_month=1; next_month_year++; }
		var last_month = month-1;
		var last_month_year = year;
		if (last_month < 1) { last_month=12; last_month_year--; }
		var date_class;
		if (this.type!="WINDOW") {
			result += "<TABLE WIDTH=144 BORDER=0 BORDERWIDTH=0 CELLSPACING=0 CELLPADDING=0>";
			}
		result += '<TR>\n';
		var refresh = 'javascript:'+windowref+'CP_refreshCalendar';
		if (this.isShowYearNavigation) {
			result += '<TD CLASS="'+this.cssPrefix+'cpMonthNavigation" WIDTH="10"><A CLASS="'+this.cssPrefix+'cpMonthNavigation" HREF="'+refresh+'('+this.index+','+last_month+','+last_month_year+');">&lt;</A></TD>';
			result += '<TD CLASS="'+this.cssPrefix+'cpMonthNavigation" WIDTH="58"><SPAN CLASS="'+this.cssPrefix+'cpMonthNavigation">'+this.monthNames[month-1]+'</SPAN></TD>';
			result += '<TD CLASS="'+this.cssPrefix+'cpMonthNavigation" WIDTH="10"><A CLASS="'+this.cssPrefix+'cpMonthNavigation" HREF="'+refresh+'('+this.index+','+next_month+','+next_month_year+');">&gt;</A></TD>';
			result += '<TD CLASS="'+this.cssPrefix+'cpMonthNavigation" WIDTH="10">&nbsp;</TD>';
			result += '<TD CLASS="'+this.cssPrefix+'cpYearNavigation" WIDTH="10"><A CLASS="'+this.cssPrefix+'cpYearNavigation" HREF="'+refresh+'('+this.index+','+month+','+(year-1)+');">&lt;</A></TD>';
			if (this.isShowYearNavigationInput) {
				result += '<TD CLASS="'+this.cssPrefix+'cpYearNavigation" WIDTH="36"><INPUT NAME="cpYear" CLASS="'+this.cssPrefix+'cpYearNavigation" SIZE="4" MAXLENGTH="4" VALUE="'+year+'" onBlur="'+refresh+'('+this.index+','+month+',this.value-0);"></TD>';
				}
			else {
				result += '<TD CLASS="'+this.cssPrefix+'cpYearNavigation" WIDTH="36"><SPAN CLASS="'+this.cssPrefix+'cpYearNavigation">'+year+'</SPAN></TD>';
				}
			result += '<TD CLASS="'+this.cssPrefix+'cpYearNavigation" WIDTH="10"><A CLASS="'+this.cssPrefix+'cpYearNavigation" HREF="'+refresh+'('+this.index+','+month+','+(year+1)+');">&gt;</A></TD>';
			}
		else {
			result += '<TD CLASS="'+this.cssPrefix+'cpMonthNavigation" WIDTH="22"><A CLASS="'+this.cssPrefix+'cpMonthNavigation" HREF="'+refresh+'('+this.index+','+last_month+','+last_month_year+');">&lt;&lt;</A></TD>\n';
			result += '<TD CLASS="'+this.cssPrefix+'cpMonthNavigation" WIDTH="100"><SPAN CLASS="'+this.cssPrefix+'cpMonthNavigation">'+this.monthNames[month-1]+' '+year+'</SPAN></TD>\n';
			result += '<TD CLASS="'+this.cssPrefix+'cpMonthNavigation" WIDTH="22"><A CLASS="'+this.cssPrefix+'cpMonthNavigation" HREF="'+refresh+'('+this.index+','+next_month+','+next_month_year+');">&gt;&gt;</A></TD>\n';
			}
		result += '</TR></TABLE>\n';
		result += '<TABLE WIDTH=120 BORDER=0 CELLSPACING=0 CELLPADDING=1 ALIGN=CENTER>\n';
		result += '<TR>\n';
		for (var j=0; j<7; j++) {

			result += '<TD CLASS="'+this.cssPrefix+'cpDayColumnHeader" WIDTH="14%"><SPAN CLASS="'+this.cssPrefix+'cpDayColumnHeader">'+this.dayHeaders[(this.weekStartDay+j)%7]+'</TD>\n';
			}
		result += '</TR>\n';
		for (var row=1; row<=6; row++) {
			result += '<TR>\n';
			for (var col=1; col<=7; col++) {
				var disabled=false;
				if (this.disabledDatesExpression!="") {
					var ds=""+display_year+LZ(display_month)+LZ(display_date);
					eval("disabled=("+this.disabledDatesExpression+")");
					}
				var dateClass = "";
				if ((display_month == this.currentDate.getMonth()+1) && (display_date==this.currentDate.getDate()) && (display_year==this.currentDate.getFullYear())) {
					dateClass = "cpCurrentDate";
					}
				else if (display_month == month) {
					dateClass = "cpCurrentMonthDate";
					}
				else {
					dateClass = "cpOtherMonthDate";
					}
				if (disabled || this.disabledWeekDays[col-1]) {
					result += '	<TD CLASS="'+this.cssPrefix+dateClass+'"><SPAN CLASS="'+this.cssPrefix+dateClass+'Disabled">'+display_date+'</SPAN></TD>\n';
					}
				else {
					var selected_date = display_date;
					var selected_month = display_month;
					var selected_year = display_year;
					if (this.displayType=="week-end") {
						var d = new Date(selected_year,selected_month-1,selected_date,0,0,0,0);
						d.setDate(d.getDate() + (7-col));
						selected_year = d.getYear();
						if (selected_year < 1000) { selected_year += 1900; }
						selected_month = d.getMonth()+1;
						selected_date = d.getDate();
						}
					result += '	<TD CLASS="'+this.cssPrefix+dateClass+'"><A HREF="javascript:'+windowref+this.returnFunction+'('+selected_year+','+selected_month+','+selected_date+');'+windowref+'CP_hideCalendar(\''+this.index+'\');" CLASS="'+this.cssPrefix+dateClass+'">'+display_date+'</A></TD>\n';
					}
				display_date++;
				if (display_date > daysinmonth[display_month]) {
					display_date=1;
					display_month++;
					}
				if (display_month > 12) {
					display_month=1;
					display_year++;
					}
				}
			result += '</TR>';
			}
		var current_weekday = now.getDay() - this.weekStartDay;
		if (current_weekday < 0) {
			current_weekday += 7;
			}
		result += '<TR>\n';
		result += '	<TD COLSPAN=7 ALIGN=CENTER CLASS="'+this.cssPrefix+'cpTodayText">\n';
		if (this.disabledDatesExpression!="") {
			var ds=""+now.getFullYear()+LZ(now.getMonth()+1)+LZ(now.getDate());
			eval("disabled=("+this.disabledDatesExpression+")");
			}
		if (disabled || this.disabledWeekDays[current_weekday+1]) {
			result += '		<SPAN CLASS="'+this.cssPrefix+'cpTodayTextDisabled">'+this.todayText+'</SPAN>\n';
			}
		else {
			result += '		<A CLASS="'+this.cssPrefix+'cpTodayText" HREF="javascript:'+windowref+this.returnFunction+'(\''+now.getFullYear()+'\',\''+(now.getMonth()+1)+'\',\''+now.getDate()+'\');'+windowref+'CP_hideCalendar(\''+this.index+'\');">'+this.todayText+'</A>\n';
			}
		result += '		<BR>\n';
		result += '	</TD></TR></TABLE></CENTER></TD></TR></TABLE>\n';
	}

	// Code common for MONTH, QUARTER, YEAR
	// ------------------------------------
	if (this.displayType=="month" || this.displayType=="quarter" || this.displayType=="year") {
		if (arguments.length > 0) { var year = arguments[0]; }
		else { 
			if (this.displayType=="year") {	var year = now.getFullYear()-this.yearSelectStartOffset; }
			else { var year = now.getFullYear(); }
			}
		if (this.displayType!="year" && this.isShowYearNavigation) {
			result += "<TABLE WIDTH=144 BORDER=0 BORDERWIDTH=0 CELLSPACING=0 CELLPADDING=0>";
			result += '<TR>\n';
			result += '	<TD CLASS="'+this.cssPrefix+'cpYearNavigation" WIDTH="22"><A CLASS="'+this.cssPrefix+'cpYearNavigation" HREF="javascript:'+windowref+'CP_refreshCalendar('+this.index+','+(year-1)+');">&lt;&lt;</A></TD>\n';
			result += '	<TD CLASS="'+this.cssPrefix+'cpYearNavigation" WIDTH="100">'+year+'</TD>\n';
			result += '	<TD CLASS="'+this.cssPrefix+'cpYearNavigation" WIDTH="22"><A CLASS="'+this.cssPrefix+'cpYearNavigation" HREF="javascript:'+windowref+'CP_refreshCalendar('+this.index+','+(year+1)+');">&gt;&gt;</A></TD>\n';
			result += '</TR></TABLE>\n';
			}
		}
		
	// Code for MONTH display 
	// ----------------------
	if (this.displayType=="month") {
		// If POPUP, write entire HTML document
		result += '<TABLE WIDTH=120 BORDER=0 CELLSPACING=1 CELLPADDING=0 ALIGN=CENTER>\n';
		for (var i=0; i<4; i++) {
			result += '<TR>';
			for (var j=0; j<3; j++) {
				var monthindex = ((i*3)+j);
				result += '<TD WIDTH=33% ALIGN=CENTER><A CLASS="'+this.cssPrefix+'cpText" HREF="javascript:'+windowref+this.returnMonthFunction+'('+year+','+(monthindex+1)+');'+windowref+'CP_hideCalendar(\''+this.index+'\');" CLASS="'+date_class+'">'+this.monthAbbreviations[monthindex]+'</A></TD>';
				}
			result += '</TR>';
			}
		result += '</TABLE></CENTER></TD></TR></TABLE>\n';
		}
	
	// Code for QUARTER display
	// ------------------------
	if (this.displayType=="quarter") {
		result += '<BR><TABLE WIDTH=120 BORDER=1 CELLSPACING=0 CELLPADDING=0 ALIGN=CENTER>\n';
		for (var i=0; i<2; i++) {
			result += '<TR>';
			for (var j=0; j<2; j++) {
				var quarter = ((i*2)+j+1);
				result += '<TD WIDTH=50% ALIGN=CENTER><BR><A CLASS="'+this.cssPrefix+'cpText" HREF="javascript:'+windowref+this.returnQuarterFunction+'('+year+','+quarter+');'+windowref+'CP_hideCalendar(\''+this.index+'\');" CLASS="'+date_class+'">Q'+quarter+'</A><BR><BR></TD>';
				}
			result += '</TR>';
			}
		result += '</TABLE></CENTER></TD></TR></TABLE>\n';
		}

	// Code for YEAR display
	// ---------------------
	if (this.displayType=="year") {
		var yearColumnSize = 4;
		result += "<TABLE WIDTH=144 BORDER=0 BORDERWIDTH=0 CELLSPACING=0 CELLPADDING=0>";
		result += '<TR>\n';
		result += '	<TD CLASS="'+this.cssPrefix+'cpYearNavigation" WIDTH="50%"><A CLASS="cpYearNavigation" HREF="javascript:'+windowref+'CP_refreshCalendar('+this.index+','+(year-(yearColumnSize*2))+');">&lt;&lt;</A></TD>\n';
		result += '	<TD CLASS="'+this.cssPrefix+'cpYearNavigation" WIDTH="50%"><A CLASS="cpYearNavigation" HREF="javascript:'+windowref+'CP_refreshCalendar('+this.index+','+(year+(yearColumnSize*2))+');">&gt;&gt;</A></TD>\n';
		result += '</TR></TABLE>\n';
		result += '<TABLE WIDTH=120 BORDER=0 CELLSPACING=1 CELLPADDING=0 ALIGN=CENTER>\n';
		for (var i=0; i<yearColumnSize; i++) {
			for (var j=0; j<2; j++) {
				var currentyear = year+(j*yearColumnSize)+i;
				result += '<TD WIDTH=50% ALIGN=CENTER><A CLASS="'+this.cssPrefix+'cpText" HREF="javascript:'+windowref+this.returnYearFunction+'('+currentyear+');'+windowref+'CP_hideCalendar(\''+this.index+'\');" CLASS="'+date_class+'">'+currentyear+'</A></TD>';
				}
			result += '</TR>';
			}
		result += '</TABLE></CENTER></TD></TR></TABLE>\n';
		}
	// Common
	if (this.type == "WINDOW") {
		result += "</BODY></HTML>\n";
		}
	return result;
	}
