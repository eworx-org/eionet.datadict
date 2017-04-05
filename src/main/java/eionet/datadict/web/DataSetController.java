package eionet.datadict.web;

import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import eionet.datadict.services.DataSetService;
import java.io.IOException;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Controller
@RequestMapping(value = "/datasets")
public class DataSetController {

    private final DataSetService dataSetService;

   
    @Autowired
    public DataSetController( DataSetService dataSetService) {
        this.dataSetService = dataSetService;
    }

    @RequestMapping(value = "/testmvc", method = RequestMethod.GET)
    @ResponseBody
    public String testMVCINDD() {
        return "it works";
    }

    @RequestMapping(value = "/schema/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public void getDataSetSchema(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws ResourceNotFoundException, ServletException, EmptyParameterException, IOException, TransformerConfigurationException, TransformerException, XmlExportException {

        if (id == null) {
            throw new EmptyParameterException((" schema id"));
        }
        Document xml = this.dataSetService.getDataSetXMLSchema(id);
        String fileName = "schema-dst-".concat(id).concat(".xsd");
        response.setContentType("application/xml");
        response.setHeader("Content-Disposition", "attachment;filename="+fileName);
        ServletOutputStream outStream = response.getOutputStream();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource source = new DOMSource(xml);
        StreamResult result = new StreamResult(outStream);
        transformer.transform(source, result);
        outStream.flush();
        outStream.close();
    }
    
    
      @ExceptionHandler(EmptyParameterException.class)
    public ResponseEntity<HashMap<String,String>> HandleEmptyParameterException(Exception exception) {
        exception.printStackTrace();
        HashMap<String,String> errorResult = new HashMap<String,String>();
        errorResult.put("error message",exception.getMessage());
        return new ResponseEntity<HashMap<String,String>>(errorResult,HttpStatus.BAD_REQUEST);
    }
    
      @ExceptionHandler({IOException.class, TransformerConfigurationException.class,TransformerException.class,XmlExportException.class})
    public ResponseEntity<HashMap<String,String>> HandleFatalExceptions(Exception exception) {
        exception.printStackTrace();
        HashMap<String,String> errorResult = new HashMap<String,String>();
        errorResult.put("error message",exception.getMessage());
        return new ResponseEntity<HashMap<String,String>>(errorResult,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

