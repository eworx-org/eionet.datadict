Net=1;
if ((navigator.appName.substring(0,5) == "Netsc"
  && navigator.appVersion.charAt(0) > 2)
  || (navigator.appName.substring(0,5) == "Micro"
  && navigator.appVersion.charAt(0) > 3)) {
 Net=0;

 over = new Image;
 out = new Image;
 gammel = new Image;

 over.src = "images/on.gif";
 out.src = "images/off.gif";
 
 gTarget = 'img1';
}

var browser = document.all ? 'E' : 'N';

function login() {
	window.open("login.html","login","height=200,width=300,status=no,toolbar=no,scrollbars=no,resizable=no,menubar=no,location=no");
}
function logout() {
	window.open("logout.html","login","height=200,width=300,status=no,toolbar=no,scrollbars=no,resizable=no,menubar=no,location=no");
}

function form_changed(form_name){
	document.forms[form_name].elements["changed"].value="1";		
}
function getDDVersionName(){
	return "Version 2.1";
}

function popNovr(link) {
	// it is VERY IMOPRTANT to give the popup window a name (even if its an empty string), because setting it to
	// null will load into the opener if the opener itself has also been opened by another window!!!
	var newWin = window.open(link,"","status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=no");
}
function pop(link) {
	// It is VERY IMOPRTANT to set the window's event return value to FALSE, because this will override the browser's
	// default behaviour which would ruin the whole thing. It is equally important to do it as the first thing here in
	// this method, because doing it later would not be recognized by Netscape.
	window.event.returnValue = false;

	// it is VERY IMOPRTANT to give the popup window a name (even if its an empty string), because setting it to
	// null will load into the opener if the opener itself has also been opened by another window!!!
	var newWin = window.open(link,"","status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=no");
}