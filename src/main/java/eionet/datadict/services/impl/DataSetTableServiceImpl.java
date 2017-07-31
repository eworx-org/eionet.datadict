package eionet.datadict.services.impl;

import eionet.datadict.commons.DataDictXMLConstants;
import eionet.datadict.dal.AttributeDao;
import eionet.datadict.dal.AttributeValueDao;
import eionet.datadict.dal.DataElementDao;
import eionet.datadict.dal.DatasetTableDao;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.AttributeValue;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.model.DataElement;
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
    private final DatasetTableDataService datasetTableDataService;

    @Autowired
    public DataSetTableServiceImpl(DatasetTableDao datasetTableDao, DataElementDao dataElementDao, AttributeValueDao attributeValueDao, AttributeDao attributeDao, DatasetTableDataService datasetTableDataService) {
        this.datasetTableDao = datasetTableDao;
        this.dataElementDao = dataElementDao;
        this.attributeValueDao = attributeValueDao;
        this.attributeDao = attributeDao;
        this.datasetTableDataService = datasetTableDataService;
    }

    @Override
    public Document getDataSetTableXMLSchema(int id) throws XmlExportException, ResourceNotFoundException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        DatasetTable dataSetTable = this.datasetTableDataService.getFullDatasetTableDefinition(id);
        int datasetId = dataSetTable.getDataSet().getId();

        try {
            docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            NameTypeElementMaker elMaker = new NameTypeElementMaker(DataDictXMLConstants.XS_PREFIX + ":", doc);
            Element schemaRoot = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, DataDictXMLConstants.XS_PREFIX + ":" + DataDictXMLConstants.SCHEMA);
            schemaRoot.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                    DataDictXMLConstants.XSI_PREFIX + ":" + DataDictXMLConstants.SCHEMA_LOCATION, XMLConstants.W3C_XML_SCHEMA_NS_URI + "  " + XMLConstants.W3C_XML_SCHEMA_NS_URI + ".xsd");
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataSetTable.getCorrespondingNS().getId());
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + DataDictXMLConstants.ISO_ATTRS, DataDictXMLConstants.ISOATTRS_NAMESPACE);
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + DataDictXMLConstants.DD_ATTRS, DataDictXMLConstants.DDATTRS_NAMESPACE);
            schemaRoot.setAttribute(DataDictXMLConstants.TARGET_NAMESPACE, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataSetTable.getCorrespondingNS().getId());
            schemaRoot.setAttribute("elementFormDefault", "qualified");
            schemaRoot.setAttribute("attributeFormDefault", "unqualified");
            Element tableRootElement = elMaker.createElement(DataDictXMLConstants.ELEMENT, dataSetTable.getShortName());
            schemaRoot.appendChild(tableRootElement);
            Element dsAnnotation = elMaker.createElement(DataDictXMLConstants.ANNOTATION);
            tableRootElement.appendChild(dsAnnotation);
            Element dsDocumentation = elMaker.createElement(DataDictXMLConstants.DOCUMENTATION);
            dsDocumentation.setAttribute(XMLConstants.XML_NS_PREFIX + ":" + DataDictXMLConstants.LANGUAGE_PREFIX, DataDictXMLConstants.DEFAULT_XML_LANGUAGE);
            dsAnnotation.appendChild(dsDocumentation);

            for (Attribute dataSetTableAttribute : dataSetTable.getAttributes()) {
                AttributeValue attributeValue = attributeValueDao.getByAttributeAndOwner(dataSetTableAttribute.getId(), new DataDictEntity(dataSetTable.getId(), DataDictEntity.Entity.T)).get(0);
                Element attributeElement = elMaker.createElement(dataSetTableAttribute.getShortName().replace(" ", ""), null, dataSetTableAttribute.getNamespace().getShortName().replace("_", ""));

                if (attributeValue != null) {
                    attributeElement.appendChild(doc.createTextNode(attributeValue.getValue()));
                }
                dsDocumentation.appendChild(attributeElement);
            }
            List<Attribute> dataSetAttributes = attributeDao.getByDataDictEntity(new DataDictEntity(datasetId, DataDictEntity.Entity.DS));
            List<AttributeValue> dataSetAttributesValues = new ArrayList<AttributeValue>();
            for (Attribute dataSetAttribute : dataSetAttributes) {
                List<AttributeValue> attributeValues = attributeValueDao.getByAttributeAndOwner(dataSetAttribute.getId(), new DataDictEntity(datasetId, DataDictEntity.Entity.DS));
                dataSetAttributesValues.add(attributeValues.get(0));
                Element attributeElement = elMaker.createElement(dataSetAttribute.getShortName().replace(" ", ""), null, dataSetAttribute.getNamespace().getShortName().replace("_", ""));
                if (attributeValues.get(0) != null) {
                    attributeElement.appendChild(doc.createTextNode(attributeValues.get(0).getValue()));
                }
                dsDocumentation.appendChild(attributeElement);
            }
            Element complexType = elMaker.createElement(DataDictXMLConstants.COMPLEX_TYPE);
            tableRootElement.appendChild(complexType);
            Element sequence = elMaker.createElement(DataDictXMLConstants.SEQUENCE);
            complexType.appendChild(sequence);
            Element rowElement = elMaker.createElement(DataDictXMLConstants.ELEMENT);
            rowElement.setAttribute(DataDictXMLConstants.NAME, "Row");
            rowElement.setAttribute(DataDictXMLConstants.MIN_OCCURS, "1");
            rowElement.setAttribute(DataDictXMLConstants.MAX_OCCURS, "unbounded");
            sequence.appendChild(rowElement);
            Element rowComplexType = elMaker.createElement(DataDictXMLConstants.COMPLEX_TYPE);
            rowElement.appendChild(rowComplexType);
            Element rowSequence = elMaker.createElement(DataDictXMLConstants.SEQUENCE);
            rowComplexType.appendChild(rowSequence);
            for (DataElement dataElement : dataSetTable.getDataElements()) {
                Element tableElement = elMaker.createElement(DataDictXMLConstants.ELEMENT);
                tableElement.setAttribute(DataDictXMLConstants.REF, dataElement.getShortName());
                tableElement.setAttribute(DataDictXMLConstants.MIN_OCCURS, "1");
                tableElement.setAttribute(DataDictXMLConstants.MAX_OCCURS, "1");
                rowSequence.appendChild(tableElement);
            }

            for (DataElement dataElement : dataSetTable.getDataElements()) {
                Element xmlElement = elMaker.createElement(DataDictXMLConstants.ELEMENT, dataElement.getShortName());
                String MinSize = "";
                String MaxSize = "";
                String Datatype = "";
                String MinInclusiveValue = "";
                String MaxInclusiveValue = "";

                schemaRoot.appendChild(xmlElement);
                Element elemAnnotation = elMaker.createElement(DataDictXMLConstants.ANNOTATION);
                xmlElement.appendChild(elemAnnotation);
                Element elemDocumentation = elMaker.createElement(DataDictXMLConstants.DOCUMENTATION);
                elemDocumentation.setAttribute(XMLConstants.XML_NS_PREFIX + ":" + DataDictXMLConstants.LANGUAGE_PREFIX, DataDictXMLConstants.DEFAULT_XML_LANGUAGE);
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
                    System.out.println("Attribute ShortName :" +attribute.getShortName());
                    System.out.println("Attribute Namespace :" +attribute.getNamespace().getShortName().replace("_", ""));
                    Element attributeElement = elMaker.createElement(attribute.getShortName().replace(" ", ""), null, attribute.getNamespace().getShortName().replace("_", ""));
                    attributeElement.appendChild(doc.createTextNode(attributeValue.getValue()));
                    elemDocumentation.appendChild(attributeElement);
                }
                Element dataElementSimpleType = elMaker.createElement(DataDictXMLConstants.SIMPLE_TYPE);
                xmlElement.appendChild(dataElementSimpleType);
                Element dataElementRestriction = elMaker.createElement(DataDictXMLConstants.RESTRICTION);
                dataElementRestriction.setAttribute(DataDictXMLConstants.BASE, DataDictXMLConstants.XS_PREFIX + ":" + Datatype);
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
    public Document getDataSetTableXMLInstance(int id) throws XmlExportException, ResourceNotFoundException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        DatasetTable dataSetTable = this.getDatasetTable(id);
        try {
            docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element schemaRoot = doc.createElement(dataSetTable.getShortName());
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataSetTable.getCorrespondingNS().getId());
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + DataDictXMLConstants.XSI_PREFIX, XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
            schemaRoot.setAttribute(DataDictXMLConstants.XSI_PREFIX + ":" + DataDictXMLConstants.SCHEMA_LOCATION, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataSetTable.getCorrespondingNS().getId() + "  " + DataDictXMLConstants.TABLE_SCHEMA_LOCATION_PARTIAL_FILE_NAME + dataSetTable.getId() + DataDictXMLConstants.XSD_FILE_EXTENSION);
            List<DataElement> dataElements = this.dataElementDao.getDataElementsOfDatasetTable(dataSetTable.getId());
            String tableNS = DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataSetTable.getCorrespondingNS().getId();
            Element row = doc.createElementNS(tableNS, DataDictXMLConstants.ROW);
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

    @Override
    public DatasetTable getDatasetTable(int id) throws ResourceNotFoundException {
        DatasetTable datasetTable = this.datasetTableDao.getById(id);
        if (datasetTable != null) {
            return datasetTable;
        } else {
            throw new ResourceNotFoundException("DatasetTable with id:" + id + "does not exist");
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
            if (nameSpacePrefix != null && nameSpacePrefix.equals(DataDictXMLConstants.ISO_ATTRS)) {
                element = doc.createElementNS(DataDictXMLConstants.ISOATTRS_NAMESPACE, nameSpacePrefix + ":" + elementName);
            } else if (nameSpacePrefix != null && nameSpacePrefix.equals(DataDictXMLConstants.DD_ATTRS)) {
                element = doc.createElementNS(DataDictXMLConstants.DDATTRS_NAMESPACE, nameSpacePrefix + ":" + elementName);
            } else {
                element = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, nsPrefix + elementName);

            }
            if (nameAttrVal != null) {
                element.setAttribute(DataDictXMLConstants.NAME, nameAttrVal);
            }
            if (typeAttrVal != null) {
                element.setAttribute(DataDictXMLConstants.TYPE, typeAttrVal);
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
