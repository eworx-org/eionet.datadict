package eionet.meta;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.tee.xmlserver.*;

import eionet.util.SecurityUtil;

/**
 * Simple servlet to handle login form submit. Uses authenticator module specified by application
 * descriptor <CODE>&lt;context-param authenticator="..."&gt;</CODE> parameter to authenticate passed user (request parameters
 * <CODE>j_username</CODE> and <CODE>j_password</CODE>. If authentication fails, redirects caller to error page 
 * specified by <CODE>&lt;context-param login-error-page="..."&gt;</CODE><BR><BR>
 *
 *
 * @author  Jaanus Heinlaid
 */

public class LoginServlet extends HttpServlet {
	
	/** */
	public static final String INITPARAM_DONT_USE_CAS_LOGIN = "dont-use-cas-login";

/**
 *
 */
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        
        String username = req.getParameter("j_username");
        String password = req.getParameter("j_password");
            
        // authentication is not required if context-param "authentication"
        // is not missing and is equal to "false" (case insensitive)
        boolean auth = true;
        try{
            XDBApplication xdbapp = XDBApplication.getInstance();
            String _auth = xdbapp.getInitParameter("authentication");
            if (_auth!=null && _auth.equalsIgnoreCase("false"))
    	        auth = false;
	    }
	    catch (Exception e){}
	    
        AppUserIF user = XDBApplication.getAuthenticator();
        if (user.authenticate(username, password) == true || !auth) {
            // store the authenticated user object to current session
            SecurityUtil.allocSession(req, user);
            // close current window
            res.setContentType("text/html");
            try {
                PrintWriter out = res.getWriter();
                out.print(responseText(req));
                out.close();   
            } catch (IOException e) {
                Logger.log("Writing page to response stream failed", e);
            }
        }
        else {
            String loginError = XDBApplication.getLoginError();
            SecurityUtil.freeSession(req);
            res.sendRedirect(loginError);
        }
    }
    
    private String responseText(HttpServletRequest req){
    	
    	String target = req.getParameter("target");
    	StringBuffer buf = new StringBuffer("<html><script>");
    	if (target!=null && target.equals("blank"))
    		buf.append("window.opener.location.reload(true);");
		buf.append("window.close();</script></html>");
    	return buf.toString();
    }
}
