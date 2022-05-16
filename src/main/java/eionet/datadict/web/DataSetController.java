package eionet.datadict.web;

import eionet.datadict.errors.BadRequestException;
import eionet.datadict.errors.IllegalParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import eionet.datadict.services.DataSetService;
import eionet.datadict.services.DataSetTableService;
import eionet.meta.DDUser;
import eionet.util.SecurityUtil;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.slf4j.MDC;
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
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.*;
import eionet.meta.outservices.OutService;
/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Controller
@RequestMapping(value = "/dataset")
public class DataSetController {

    private final DataSetService dataSetService;
    private final DataSetTableService dataSetTableService;
    private OutService outService;
    private static final Logger LOGGER = Logger.getLogger(DataSetController.class);
    private static final String GENERIC_DD_ERROR_PAGE_URL = "/error.action?type=INTERNAL_SERVER_ERROR&message=";
    private static final String SCHEMA_DATASET_TABLE_FILE_NAME_PREFIX = "schema-tbl-";
    private static final String SCHEMA_DATASET_FILE_NAME_PREFIX = "schema-dst-";
    private static final String DATASET_INSTANCE_FILE_NAME = "dataset-instance";

    @Autowired
    public DataSetController(DataSetService dataSetService, DataSetTableService dataSetTableService, OutService outService) {
        this.dataSetService = dataSetService;
        this.dataSetTableService = dataSetTableService;
        this.outService = outService;
    }

    @RequestMapping(value = "/{id}/schema", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public void getDataSetSchema(@PathVariable int id, HttpServletResponse response) throws ResourceNotFoundException, ServletException, IOException, TransformerConfigurationException, TransformerException, XmlExportException {

        Document xml = this.dataSetService.getDataSetXMLSchema(id);
        String fileName = "schema-dst-".concat(String.valueOf(id)).concat(".xsd");
        response.setContentType("application/xml");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
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

    /**
     * Responsible for retrieving the schema files for DatasetTables of a
     * Dataset. The request must contain as a PathVariable the table schema file
     * name like this: schema-tbl-12345.xsd The format of the fileName of the
     * schema file is automatically generated by the method
     * :{@link eionet.datadict.web.DataSetController#getDataSetSchema(int, javax.servlet.http.HttpServletResponse)}
     * *
     */
    @RequestMapping(value = "/{id}/{variable:.+}")
    @ResponseBody
    public void getDataSetOrDataSetTableSchemaOrInstanceByFileName(@PathVariable int id, @PathVariable String variable, HttpServletResponse response) throws XmlExportException, IOException, ResourceNotFoundException, TransformerConfigurationException, TransformerException, BadRequestException {
        Document xml = null;
        Pattern TableFileNamePattern = Pattern.compile("\\b" + SCHEMA_DATASET_TABLE_FILE_NAME_PREFIX + "\\d+.xsd");
        Pattern DataSetFileNamePattern = Pattern.compile("\\b" + SCHEMA_DATASET_FILE_NAME_PREFIX + "\\d+.xsd");
        Pattern DataSetInstanceFileNamePattern = Pattern.compile("\\b" + DATASET_INSTANCE_FILE_NAME + ".xml");
        Pattern DatasetTableInstanceFileNamePattern = Pattern.compile("table-(.*?)-instance.xml");
        Matcher DatasetTableInstanceFileNamePatternMatcher = DatasetTableInstanceFileNamePattern.matcher(variable);

        if (DataSetFileNamePattern.matcher(variable).matches()) {
            int dataSetId = Integer.parseInt(variable.replace(SCHEMA_DATASET_FILE_NAME_PREFIX, "").replace(".xsd", "").trim());
            String fileName = SCHEMA_DATASET_FILE_NAME_PREFIX.concat(String.valueOf(dataSetId)).concat(".xsd");
            xml = this.dataSetService.getDataSetXMLSchema(id);
            response.setContentType("application/xml");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        } else if (TableFileNamePattern.matcher(variable).matches()) {
            int tableId = Integer.parseInt(variable.replace(SCHEMA_DATASET_TABLE_FILE_NAME_PREFIX, "").replace(".xsd", "").trim());
            xml = this.dataSetTableService.getDataSetTableXMLSchema(tableId);
            String fileName = SCHEMA_DATASET_TABLE_FILE_NAME_PREFIX.concat(String.valueOf(tableId)).concat(".xsd");
            response.setContentType("application/xml");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        } else if (DataSetInstanceFileNamePattern.matcher(variable).matches()) {
            xml = this.dataSetService.getDataSetXMLInstance(id);
            String fileName = "dataset-instance.xml";
            response.setContentType("application/xml");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        } else if (DatasetTableInstanceFileNamePatternMatcher.find()) {
            String tableId = DatasetTableInstanceFileNamePatternMatcher.group(1);
            xml = this.dataSetTableService.getDataSetTableXMLInstance(Integer.parseInt(tableId));
            String fileName = "table-" + tableId + "-instance.xml";
            response.setContentType("application/xml");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        } else {
            throw new BadRequestException("Schema File Name:" + variable + " is incorrect format.");
        }
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

    @RequestMapping(value = "/{id}/instance", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public void getDataSetInstance(@PathVariable int id, HttpServletResponse response) throws ResourceNotFoundException, ServletException, IOException, TransformerConfigurationException, TransformerException, XmlExportException {

        Document xml = this.dataSetService.getDataSetXMLInstance(id);
        String fileName = "dataset-instance.xml";
        response.setContentType("application/xml");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
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
    
    @RequestMapping(value = "/rdf/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String getDatasetRDFExport(@PathVariable int id,HttpServletResponse response) throws XmlExportException, ResourceNotFoundException, IOException, TransformerConfigurationException, TransformerException {
       
        Model rdfModel = this.dataSetService.getDatasetRdf(id);
        StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        rdfModel.write(sw);
        return sw.toString();
    }

    
    @RequestMapping(value = "{id}/updateDispCreateLinks/{dispDownloadLinkType}/{value}")
    public ResponseEntity<?> updateDisplayDownloadLinks(HttpServletResponse response,@PathVariable int id,@PathVariable String dispDownloadLinkType,@PathVariable String value,
            HttpServletRequest request) throws IOException {
        Thread.currentThread().setName("UPDATE-DATASET-DISPLAY-DOWNLOAD-LINKS");
        MDC.put("sessionId", request.getSession().getId().substring(0,16));
        DDUser user = SecurityUtil.getUser(request);
        if (user != null && user.hasPermission("/datasets", "u")) {
            try {
                this.dataSetService.updateDatasetDisplayDownloadLinks(id,dispDownloadLinkType,value);
                return new ResponseEntity<>(HttpStatus.OK);

            } catch (IllegalParameterException e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            response.sendRedirect(SecurityUtil.getLoginURL(request));
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public void HandleResourceNotFoundException(Exception exception, HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOGGER.log(Level.ERROR, null, exception);
        response.sendRedirect(request.getContextPath() + GENERIC_DD_ERROR_PAGE_URL + URLEncoder.encode(exception.getMessage(), "UTF-8"));
    }

    @ExceptionHandler(BadRequestException.class)
    public void HandleBadRequestException(Exception exception, HttpServletRequest request, HttpServletResponse response) throws IOException {
       
        response.sendRedirect(request.getContextPath() + GENERIC_DD_ERROR_PAGE_URL +URLEncoder.encode(exception.getMessage(), "UTF-8"));
    }

    @ExceptionHandler({IOException.class, TransformerConfigurationException.class, TransformerException.class, XmlExportException.class, DOMException.class})
    public void HandleFatalExceptions(Exception exception, HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOGGER.log(Level.ERROR, null, exception);
        response.sendRedirect(request.getContextPath() + GENERIC_DD_ERROR_PAGE_URL  +URLEncoder.encode(exception.getMessage(), "UTF-8"));
    }

    @RequestMapping(value = "releaseInfo/{type}/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Hashtable getDatasetReleaseInfo(@PathVariable String type, @PathVariable String id) throws Exception {
        Hashtable releaseInfo = outService.getDatasetWithReleaseInfo(type, id);
        return releaseInfo;
    }

}
