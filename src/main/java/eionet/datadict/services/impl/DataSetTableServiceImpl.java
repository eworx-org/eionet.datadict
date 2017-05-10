package eionet.datadict.services.impl;

import eionet.datadict.commons.DataDictXMLConstants;
import eionet.datadict.dal.AttributeDao;
import eionet.datadict.dal.AttributeValueDao;
import eionet.datadict.dal.DataElementDao;
import eionet.datadict.dal.DatasetDao;
import eionet.datadict.dal.DatasetTableDao;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.AttributeValue;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.model.DataElement;
import eionet.datadict.model.DataSet;
import eionet.datadict.model.DatasetTable;
import eionet.datadict.model.Namespace;
import eionet.datadict.services.DataSetTableService;
import eionet.datadict.services.data.DatasetTableDataService;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Service
public class DataSetTableServiceImpl implements DataSetTableService {

    private final DatasetTableDao datasetTableDao;
    private final DataElementDao dataElementDao;
    private final AttributeValueDao attributeValueDao;
    private final AttributeDao attributeDao;
    private final DatasetDao datasetDao;
    private final DatasetTableDataService datasetTableDataService;


    @Autowired
    public DataSetTableServiceImpl(DatasetTableDao datasetTableDao, DataElementDao dataElementDao, AttributeValueDao attributeValueDao, AttributeDao attributeDao, DatasetDao datasetDao, DatasetTableDataService datasetTableDataService) {
        this.datasetTableDao = datasetTableDao;
        this.dataElementDao = dataElementDao;
        this.attributeValueDao = attributeValueDao;
        this.attributeDao = attributeDao;
        this.datasetDao = datasetDao;
        this.datasetTableDataService = datasetTableDataService;
    }

    @Override
    public Document getDataSetTableXMLSchema(int id) throws XmlExportException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        DatasetTable dataSetTable = this.datasetTableDao.getById(id);

        try {
            docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            NameTypeElementMaker elMaker = new NameTypeElementMaker(DataDictXMLConstants.XS_PREFIX, doc);
            Element schemaRoot = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI,DataDictXMLConstants.XS_PREFIX + "schema");
            schemaRoot.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance",
                    "xsi:schemaLocation", "http://www.w3.org/2001/XMLSchema http://www.w3.org/2001/XMLSchema.xsd");
            schemaRoot.setAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
            schemaRoot.setAttribute("xmlns", DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataSetTable.getCorrespondingNS().getId());
            schemaRoot.setAttribute("xmlns:isoattrs",DataDictXMLConstants.ISOATTRS_NAMESPACE);
            schemaRoot.setAttribute("xmlns:ddattrs", DataDictXMLConstants.DDATTRS_NAMESPACE);
            schemaRoot.setAttribute(DataDictXMLConstants.TARGET_NAMESPACE, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataSetTable.getCorrespondingNS().getId());
            schemaRoot.setAttribute("elementFormDefault", "qualified");
            schemaRoot.setAttribute("attributeFormDefault", "unqualified");
            List<DataElement> dataElements = this.dataElementDao.getDataElementsOfDatasetTable(dataSetTable.getId());
            int datasetId = datasetTableDao.getParentDatasetId(dataSetTable.getId());
            DataSet dataSet = datasetDao.getById(datasetId);
            Element tableRootElement = elMaker.createElement("element", dataSetTable.getShortName());

            try {
                DatasetTable dsTableFull = this.datasetTableDataService.getFullDatasetTableDefinition(id);
            } catch (ResourceNotFoundException ex) {
                Logger.getLogger(DataSetTableServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            schemaRoot.appendChild(tableRootElement);
            Element dsAnnotation = elMaker.createElement(DataDictXMLConstants.ANNOTATION);
            tableRootElement.appendChild(dsAnnotation);
            Element dsDocumentation = elMaker.createElement(DataDictXMLConstants.DOCUMENTATION);
            dsDocumentation.setAttribute("xml:lang", DataDictXMLConstants.DEFAULT_XML_LANGUAGE);
            dsAnnotation.appendChild(dsDocumentation);

            List<Attribute> dataSetTableAttributes = attributeDao.getByDataDictEntity(new DataDictEntity(dataSetTable.getId(), DataDictEntity.Entity.T));
            for (Attribute dataSetTableAttribute : dataSetTableAttributes) {
                AttributeValue attributeValue = attributeValueDao.getByAttributeAndEntityId(dataSetTableAttribute.getId(), dataSetTable.getId());
                Element attributeElement = elMaker.createElement(dataSetTableAttribute.getShortName().replace(" ", ""), null, dataSetTableAttribute.getNamespace().getShortName().replace("_", ""));

                if (attributeValue != null) {
                    attributeElement.appendChild(doc.createTextNode(attributeValue.getValue()));
                }
                dsDocumentation.appendChild(attributeElement);
            }
            List<Attribute> dataSetAttributes = attributeDao.getByDataDictEntity(new DataDictEntity(datasetId, DataDictEntity.Entity.DS));
            List<AttributeValue> dataSetAttributesValues = new ArrayList<AttributeValue>();
            for (Attribute dataSetAttribute : dataSetAttributes) {
                AttributeValue attributeValue = attributeValueDao.getByAttributeAndEntityId(dataSetAttribute.getId(), datasetId);
                dataSetAttributesValues.add(attributeValue);
                Element attributeElement = elMaker.createElement(dataSetAttribute.getShortName().replace(" ", ""), null, dataSetAttribute.getNamespace().getShortName().replace("_", ""));
                if (attributeValue != null) {
                    attributeElement.appendChild(doc.createTextNode(attributeValue.getValue()));
                }
                dsDocumentation.appendChild(attributeElement);
            }

            Element complexType = elMaker.createElement(DataDictXMLConstants.COMPLEX_TYPE);

            tableRootElement.appendChild(complexType);

            Element sequence = elMaker.createElement(DataDictXMLConstants.SEQUENCE);
            complexType.appendChild(sequence);
            Element rowElement = elMaker.createElement(DataDictXMLConstants.ELEMENT);
            rowElement.setAttribute(DataDictXMLConstants.NAME, "Row");
            rowElement.setAttribute("minOccurs", "1");
            rowElement.setAttribute("maxOccurs", "unbounded");
            sequence.appendChild(rowElement);
            Element rowComplexType = elMaker.createElement(DataDictXMLConstants.COMPLEX_TYPE);
            rowElement.appendChild(rowComplexType);
            Element rowSequence = elMaker.createElement(DataDictXMLConstants.SEQUENCE);
            rowComplexType.appendChild(rowSequence);
            for (DataElement dataElement : dataElements) {
                Element tableElement = elMaker.createElement(DataDictXMLConstants.ELEMENT);
                tableElement.setAttribute(DataDictXMLConstants.REF, dataElement.getShortName());
                tableElement.setAttribute("minOccurs", "1");
                tableElement.setAttribute("maxOccurs", "1");
                rowSequence.appendChild(tableElement);
            }

            for (DataElement dataElement : dataElements) {
                Element xmlElement = elMaker.createElement("element", dataElement.getShortName());
                String MinSize = "";
                String MaxSize = "";
                String Datatype = "";
                String MinInclusiveValue = "";
                String MaxInclusiveValue = "";

                schemaRoot.appendChild(xmlElement);
                Element elemAnnotation = elMaker.createElement(DataDictXMLConstants.ANNOTATION);
                xmlElement.appendChild(elemAnnotation);
                Element elemDocumentation = elMaker.createElement(DataDictXMLConstants.DOCUMENTATION);
                elemDocumentation.setAttribute("xml:lang", DataDictXMLConstants.DEFAULT_XML_LANGUAGE);
                elemAnnotation.appendChild(elemDocumentation);
                List<AttributeValue> attributeValues = attributeValueDao.getByOwner(new DataDictEntity(dataElement.getId(), DataDictEntity.Entity.E));
                attributeValues.addAll(dataSetAttributesValues);
                for (AttributeValue attributeValue : attributeValues) {
                    Attribute attribute = attributeDao.getById(attributeValue.getAttributeId());
                    if (attribute.getShortName().equals("MinSize")) {
                        MinSize = attributeValue.getValue();
                        continue;
                    }
                    if (attribute.getShortName().equals("MaxSize")) {
                        MaxSize = attributeValue.getValue();
                        continue;
                    }
                    if (attribute.getShortName().equals("Datatype")) {
                        Datatype = attributeValue.getValue();
                        continue;
                    }
                    if (attribute.getShortName().equals("MinInclusiveValue")) {
                        MinInclusiveValue = attributeValue.getValue();
                        continue;
                    }
                    if (attribute.getShortName().equals("MaxInclusiveValue")) {
                        MaxInclusiveValue = attributeValue.getValue();
                        continue;
                    }
                    Element attributeElement = elMaker.createElement(attribute.getShortName().replace(" ", ""), null, attribute.getNamespace().getShortName().replace("_", ""));
                    attributeElement.appendChild(doc.createTextNode(attributeValue.getValue()));
                    elemDocumentation.appendChild(attributeElement);
                }
                Element dataElementSimpleType = elMaker.createElement(DataDictXMLConstants.SIMPLE_TYPE);
                xmlElement.appendChild(dataElementSimpleType);
                Element dataElementRestriction = elMaker.createElement(DataDictXMLConstants.RESTRICTION);
                dataElementRestriction.setAttribute(DataDictXMLConstants.BASE, DataDictXMLConstants.XS_PREFIX + Datatype);
                dataElementSimpleType.appendChild(dataElementRestriction);
                if (Datatype.equals("decimal")) {
                    Element totalDigitsElement = elMaker.createElement("totalDigits");
                    totalDigitsElement.setAttribute("value", MaxSize);
                    dataElementRestriction.appendChild(totalDigitsElement);
                    Element minInclusiveElement = elMaker.createElement("minInclusive");
                    minInclusiveElement.setAttribute("value", MinInclusiveValue);
                    dataElementRestriction.appendChild(minInclusiveElement);
                    Element maxInclusiveElement = elMaker.createElement("maxInclusive");
                    maxInclusiveElement.setAttribute("value", MaxInclusiveValue);
                    dataElementRestriction.appendChild(maxInclusiveElement);
                }
                if (Datatype.equals("integer")) {
                    Element totalDigitsElement = elMaker.createElement("totalDigits");
                    totalDigitsElement.setAttribute("value", MaxSize);
                    dataElementRestriction.appendChild(totalDigitsElement);
                }
                if (Datatype.equals("string")) {
                    Element minLengthElement = elMaker.createElement("minLength");
                    minLengthElement.setAttribute("value", MinSize);
                    dataElementRestriction.appendChild(minLengthElement);
                    Element maxLengthElement = elMaker.createElement("maxLength");
                    maxLengthElement.setAttribute("value", MaxSize);
                    dataElementRestriction.appendChild(maxLengthElement);

                }

            }

            doc.appendChild(schemaRoot);
            return doc;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DataSetServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new XmlExportException(ex);
        }
    }

    @Override
    public Document getDataSetTableXMLInstance(int id) throws XmlExportException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        DatasetTable dataSetTable = this.datasetTableDao.getById(id);
        try {
            docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            NameTypeElementMaker elMaker = new NameTypeElementMaker(DataDictXMLConstants.XS_PREFIX, doc);
            Element schemaRoot = doc.createElement(dataSetTable.getShortName());
            schemaRoot.setAttribute("xmlns", DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataSetTable.getCorrespondingNS().getId());
            schemaRoot.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            schemaRoot.setAttribute("xsi:schemaLocation", DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataSetTable.getCorrespondingNS().getId() + "  " + DataDictXMLConstants.TABLE_SCHEMA_LOCATION_PARTIAL_FILE_NAME + dataSetTable.getId() + DataDictXMLConstants.XSD_FILE_EXTENSION);
            List<DataElement> dataElements = this.dataElementDao.getDataElementsOfDatasetTable(dataSetTable.getId());
            String tableNS = DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataSetTable.getCorrespondingNS().getId();
            Element row = doc.createElementNS(tableNS, "Row");
            schemaRoot.appendChild(row);
            for (DataElement dataElement : dataElements) {
                Element xmlDataElement = doc.createElementNS(tableNS, dataElement.getShortName());
                xmlDataElement.appendChild(doc.createTextNode(""));
                row.appendChild(xmlDataElement);
            }
            doc.appendChild(schemaRoot);
            return doc;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DataSetTableServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new XmlExportException(ex);

        }

    }

    private static class NameTypeElementMaker {

        private String nsPrefix;
        private Document doc;

        public NameTypeElementMaker(String nsPrefix, Document doc) {
            this.nsPrefix = nsPrefix;
            this.doc = doc;
        }

        public Element createElement(String elementName, String nameAttrVal, String typeAttrVal, String nameSpacePrefix) {
            Element element;
            if (nameSpacePrefix != null && nameSpacePrefix.equals("isoattrs")) {
                element = doc.createElementNS(DataDictXMLConstants.ISOATTRS_NAMESPACE, nameSpacePrefix + ":" + elementName);
            } else if (nameSpacePrefix != null && nameSpacePrefix.equals("ddattrs")) {
                element = doc.createElementNS(DataDictXMLConstants.DDATTRS_NAMESPACE, nameSpacePrefix + ":" + elementName);
            } else {
                element = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, nsPrefix + elementName);

            }

            if (nameAttrVal != null) {
                element.setAttribute("name", nameAttrVal);
            }
            if (typeAttrVal != null) {
                element.setAttribute("type", typeAttrVal);
            }
            return element;
        }

        public Element createElement(String elementName, String nameAttrVal) {
            return createElement(elementName, nameAttrVal, null, null);
        }

        public Element createElement(String elementName, String nameAttrVal, String nameSpacePrefix) {
            return createElement(elementName, nameAttrVal, null, nameSpacePrefix);
        }

        public Element createElement(String elementName) {
            return createElement(elementName, null, null, null);
        }

    }

}
