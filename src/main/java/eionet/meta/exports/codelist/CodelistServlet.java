package eionet.meta.exports.codelist;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eionet.meta.DDUser;
import eionet.util.SecurityUtil;
import eionet.util.Util;
import javax.servlet.ServletConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 * 
 * changed by Lena Kargioti, eka@eworx.gr on Sept 2015
 *
 */
public class CodelistServlet extends HttpServlet {
    
    @Autowired
    private CodeValueHandlerProvider codeValueHandlerProvider;

    private WebApplicationContext springContext;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        springContext = WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
        final AutowireCapableBeanFactory beanFactory = springContext.getAutowireCapableBeanFactory();
        beanFactory.autowireBean(this);
    }
    /*
     *  (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void service(HttpServletRequest req, HttpServletResponse res)
                                throws ServletException, IOException {

        PrintWriter writer = null;
        OutputStreamWriter osw = null;

        try {
            //guard(req);
            // get the object ID
            String id = req.getParameter("id");
            if (Util.isEmpty(id)) throw new Exception("Missing object id!");
            // get the object type
            String type = req.getParameter("type");
            if (Util.isEmpty(type)) throw new Exception("Missing object type!");
            // get codelist format
            String format = req.getParameter("format");
            if (Util.isEmpty(format)) {
                format = "csv";
            }

            //set export type
            Codelist.ExportType exportType = Codelist.ExportType.UNKNOWN;
            String filename = "codelist_"+id+"_"+type;
            // set response content type
            if (format.equals("csv")){
                exportType = Codelist.ExportType.CSV;
                res.setContentType("text/csv; charset=UTF-8");
                res.setHeader("Content-Disposition", "attachment; filename="+filename+".csv");
            }
            else if (format.equals("xml")){
                exportType = Codelist.ExportType.XML;
                res.setContentType("text/xml; charset=UTF-8");
                res.setHeader("Content-Disposition", "attachment; filename="+filename+".xml");
            }
            else
                throw new Exception("Unknown codelist format requested: " + format);

            // prepare output stream and writer
            osw = new OutputStreamWriter(res.getOutputStream(), "UTF-8");
            writer = new PrintWriter(osw);

            // construct codelist writer
            Codelist codelist = new Codelist(exportType, codeValueHandlerProvider);

            // write & flush
            String listStr = codelist.write(id, type);
            
            writer.write(listStr);
            writer.flush();
            osw.flush();
            writer.close();
            osw.close();
            
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new ServletException(e.toString());
        }
    }

    /**
     *
     * @param req
     * @throws Exception
     */
    private void guard(HttpServletRequest req) throws Exception {

        DDUser user = SecurityUtil.getUser(req);
        if (user == null) throw new Exception("Not logged in!");

        if (!SecurityUtil.hasPerm(user.getUserName(), "/", "xmli"))
            throw new Exception("Not permitted!");
    }
}
