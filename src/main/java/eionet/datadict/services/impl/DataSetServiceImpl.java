package eionet.datadict.services.impl;

import eionet.datadict.commons.DataDictXMLConstants;
import eionet.datadict.commons.util.XMLUtils;
import eionet.datadict.dal.AttributeDao;
import eionet.datadict.dal.AttributeValueDao;
import eionet.datadict.dal.DataElementDao;
import eionet.datadict.model.DataElement;

import eionet.datadict.dal.DatasetTableDao;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.AttributeValue;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.model.DataSet;
import eionet.datadict.model.DatasetTable;
import eionet.datadict.services.DataSetService;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import eionet.datadict.model.Namespace;
import eionet.datadict.services.data.AttributeDataService;
import eionet.datadict.services.data.DataSetDataService;
import eionet.meta.dao.domain.VocabularyConcept;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Service
public class DataSetServiceImpl implements DataSetService {

    private final DataSetDataService dataSetDataService;
    private final DatasetTableDao datasetTableDao;
    private final AttributeValueDao attributeValueDao;
    private final AttributeDao attributeDao;
    private final DataElementDao dataElementDao;
    private final AttributeDataService attributeDataService;

    @Autowired
    public DataSetServiceImpl(DataSetDataService dataSetDataService, DatasetTableDao datasetTableDao, AttributeValueDao attributeValueDao, AttributeDao attributeDao, DataElementDao dataElementDao, AttributeDataService attributeDataService) {
        this.dataSetDataService = dataSetDataService;
        this.datasetTableDao = datasetTableDao;
        this.attributeValueDao = attributeValueDao;
        this.attributeDao = attributeDao;
        this.dataElementDao = dataElementDao;
        this.attributeDataService = attributeDataService;
    }

    @Override
    public Document getDataSetXMLSchema(int id) throws XmlExportException, ResourceNotFoundException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        DataSet dataset = dataSetDataService.getDatasetWithoutRelations(id);
        List<DatasetTable> dsTables = datasetTableDao.getAllByDatasetId(dataset.getId());
        List<AttributeValue> attributeValues = attributeValueDao.getByOwner(new DataDictEntity(dataset.getId(), DataDictEntity.Entity.DS));

        try {
            docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element schemaRoot = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, DataDictXMLConstants.XS_PREFIX + ":" + DataDictXMLConstants.SCHEMA);
            schemaRoot.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                    DataDictXMLConstants.XSI_PREFIX + ":" + DataDictXMLConstants.SCHEMA_LOCATION, XMLConstants.W3C_XML_SCHEMA_NS_URI + " " + XMLConstants.W3C_XML_SCHEMA_NS_URI + ".xsd");
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + DataDictXMLConstants.XS_PREFIX, XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + DataDictXMLConstants.DATASETS, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + DataDictXMLConstants.DATASETS_NAMESPACE_ID);
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + DataDictXMLConstants.ISO_ATTRS, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + DataDictXMLConstants.ISOATTRS_NAMESPACE_ID);
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + DataDictXMLConstants.DD_ATTRS, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + DataDictXMLConstants.DDATTRS_NAMESPACE_ID);
            schemaRoot.setAttribute(DataDictXMLConstants.TARGET_NAMESPACE, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataset.getCorrespondingNS().getId());
            schemaRoot.setAttribute("elementFormDefault", "qualified");
            schemaRoot.setAttribute("attributeFormDefault", "unqualified");
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + DataDictXMLConstants.DD_PREFIX + dataset.getCorrespondingNS().getId(), DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataset.getCorrespondingNS().getId());

            for (DatasetTable dsTable : dsTables) {
                schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + DataDictXMLConstants.DD_PREFIX + dsTable.getCorrespondingNS().getId(), DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dsTable.getCorrespondingNS().getId());
                Element importElement = doc.createElement(DataDictXMLConstants.XS_PREFIX + ":" + DataDictXMLConstants.IMPORT);
                importElement.setAttribute(DataDictXMLConstants.NAMESPACE, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dsTable.getCorrespondingNS().getId());
                importElement.setAttribute(DataDictXMLConstants.SCHEMA_LOCATION, DataDictXMLConstants.TABLE_SCHEMA_LOCATION_PARTIAL_FILE_NAME + dsTable.getId() + DataDictXMLConstants.XSD_FILE_EXTENSION);
                schemaRoot.appendChild(importElement);
            }
            Element element = doc.createElement(DataDictXMLConstants.XS_PREFIX + ":" + DataDictXMLConstants.ELEMENT);
            element.setAttribute(DataDictXMLConstants.NAME, dataset.getIdentifier());
            schemaRoot.appendChild(element);
            Element annotation = doc.createElement(DataDictXMLConstants.XS_PREFIX + ":" + DataDictXMLConstants.ANNOTATION);
            element.appendChild(annotation);
            Element documentation = doc.createElement(DataDictXMLConstants.XS_PREFIX + ":" + DataDictXMLConstants.DOCUMENTATION);
            documentation.setAttribute(XMLConstants.XML_NS_PREFIX + ":" + DataDictXMLConstants.LANGUAGE_PREFIX, DataDictXMLConstants.DEFAULT_XML_LANGUAGE);
            annotation.appendChild(documentation);
            for (AttributeValue attributeValue : attributeValues) {
                Attribute attribute = attributeDao.getById(attributeValue.getAttributeId());
                /**
                 * *
                 * if (attr.getDisplayType().equals("vocabulary")){
                 * List<VocabularyConcept> vocs =
                 * searchEngine.getAttributeVocabularyConcepts(Integer.parseInt(attr.getID()),
                 * attributeValuesOwner, "0"); if(vocs != null){ if
                 * (attr.getValues() != null)
                 * attr.getValues().removeAllElements(); for (VocabularyConcept
                 * concept : vocs) { attr.setValue(concept.getLabel()); } } }
                 *
                 */
                if (attribute != null && attribute.getDisplayType().equals(Attribute.DisplayType.VOCABULARY)) {

                    List<VocabularyConcept> concepts = this.attributeDataService.getVocabularyConceptsAsAttributeValues(attribute.getId(), new DataDictEntity(dataset.getId(), DataDictEntity.Entity.DS), Attribute.ValueInheritanceMode.NONE);
                    for (VocabularyConcept concept : concepts) {
                        attributeValue.setValue(concept.getLabel());
                        Element attributeElement = doc.createElement(attribute.getNamespace().getShortName().concat(":").replace("_", "").concat(attribute.getShortName()).replace(" ", ""));
                        attributeElement.appendChild(doc.createTextNode(attributeValue.getValue()));
                        documentation.appendChild(attributeElement);
                    }
                } else if (attribute != null) {
                    Element attributeElement = doc.createElement(attribute.getNamespace().getShortName().concat(":").replace("_", "").concat(attribute.getShortName()).replace(" ", ""));
                    attributeElement.appendChild(doc.createTextNode(attributeValue.getValue()));
                    documentation.appendChild(attributeElement);
                }
            }
            Element complexType = doc.createElement(DataDictXMLConstants.XS_PREFIX + ":" + DataDictXMLConstants.COMPLEX_TYPE);
            element.appendChild(complexType);
            Element sequence = doc.createElement(DataDictXMLConstants.XS_PREFIX + ":" + DataDictXMLConstants.SEQUENCE);
            complexType.appendChild(sequence);
            for (DatasetTable dsTable : dsTables) {
                Element tableElement = doc.createElement(DataDictXMLConstants.XS_PREFIX + ":" + DataDictXMLConstants.ELEMENT);
                tableElement.setAttribute(DataDictXMLConstants.REF, DataDictXMLConstants.DD_PREFIX.concat(dsTable.getCorrespondingNS().getId().toString()).concat(":").concat(XMLUtils.replaceAllIlegalXMLCharacters(dsTable.getIdentifier())));
                tableElement.setAttribute(DataDictXMLConstants.MIN_OCCURS, "1");
                tableElement.setAttribute(DataDictXMLConstants.MAX_OCCURS, "1");
                sequence.appendChild(tableElement);
            }
            doc.appendChild(schemaRoot);
            return doc;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DataSetServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new XmlExportException(ex);
        } catch (EmptyParameterException ex) {
            Logger.getLogger(DataSetServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new XmlExportException(ex);

        }
    }

    protected String getNamespacePrefix(Namespace ns) {
        return ns == null ? "dd" : "dd" + ns.getId();
    }

    @Override
    public Document getDataSetXMLInstance(int id) throws XmlExportException, ResourceNotFoundException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            DataSet dataset = this.dataSetDataService.getDatasetWithoutRelations(id);
            List<DatasetTable> dsTables = datasetTableDao.getAllByDatasetId(dataset.getId());
            docFactory.setNamespaceAware(true);
            docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element schemaRoot = doc.createElement(dataset.getIdentifier().replace(" ", ""));
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataset.getCorrespondingNS().getId());
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + DataDictXMLConstants.XSI_PREFIX, XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
            schemaRoot.setAttribute(DataDictXMLConstants.XSI_PREFIX + ":" + DataDictXMLConstants.SCHEMA_LOCATION, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataset.getCorrespondingNS().getId() + "  "
                    + DataDictXMLConstants.APP_CONTEXT + "/" + DataDictXMLConstants.SCHEMAS_API_V2_PREFIX + "/" + DataDictXMLConstants.DATASET + "/" + dataset.getId() + "/" + DataDictXMLConstants.DATASET_SCHEMA_LOCATION_PARTIAL_FILE_NAME + dataset.getId() + DataDictXMLConstants.XSD_FILE_EXTENSION);

            for (DatasetTable dsTable : dsTables) {
                String tableNS = DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dsTable.getCorrespondingNS().getId();
                Element tableElement = doc.createElementNS(tableNS, dsTable.getIdentifier().replace(":", "-"));
                Element row = doc.createElementNS(tableNS, DataDictXMLConstants.ROW);
                row.removeAttribute(XMLConstants.XMLNS_ATTRIBUTE);
                tableElement.appendChild(row);
                schemaRoot.appendChild(tableElement);
                List<DataElement> dataElements = this.dataElementDao.getDataElementsOfDatasetTable(dsTable.getId());
                for (DataElement dataElement : dataElements) {
                    if (dataElement != null && dataElement.getIdentifier()!= null) {
                        try {
                            Element xmlDataElement = doc.createElementNS(tableNS, XMLUtils.replaceAllIlegalXMLCharacters(dataElement.getIdentifier()));
                            xmlDataElement.appendChild(doc.createTextNode(""));
                            row.appendChild(xmlDataElement);
                        } catch (Exception e) {
                            throw new XmlExportException();
                        }

                    }

                }
            }
            doc.appendChild(schemaRoot);
            return doc;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DataSetServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new XmlExportException("Error while parsing XML File", ex);
        }
    }

    @Override
    public Document getDataSetXMLInstanceWithNS(int id) throws XmlExportException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
