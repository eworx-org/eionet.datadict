/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.meta.dao.domain;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import eionet.util.Util;

/**
 * Data element.
 *
 * @author Juhan Voolaid
 */
public class DataElement {

    private int id;

    private String identifier;

    private String shortName;

    private String type;

    private String status;

    private Date modified;

    private String tableName;

    private String dataSetName;

    private String workingUser;

    private boolean workingCopy;

    //TODO - make a new DAO entity for T_CONCEPT_ELEMENT_VALUE
    /** Value from T_CONCEPT_ELEMENT_VALUE table. */
    private String attributeValue;

    /** Language from T_CONCEPT_ELEMENT_VALUE table. */
    private String attributeLanguage;

    /** related concept id. */
    private Integer relatedConceptId;

    /** related concept identifier. */
    private String relatedConceptIdentifier;

    /** related concept identifier. */
    private String relatedConceptLabel;


    /** attribute metadata in M_ATTRIBUTE. */
    private Map<String, List<String>> elemAttributeValues;

    /** fixed values. */
    private List<FixedValue> fixedValues;

    public String getStatusImage() {
        return Util.getStatusImage(status);
    }

    public boolean isReleased() {
        return StringUtils.equalsIgnoreCase("Released", status);
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @param shortName
     *            the shortName to set
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the modified
     */
    public Date getModified() {
        return modified;
    }

    /**
     * @param modified
     *            the modified to set
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableName
     *            the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * @return the dataSetName
     */
    public String getDataSetName() {
        return dataSetName;
    }

    /**
     * @param dataSetName
     *            the dataSetName to set
     */
    public void setDataSetName(String dataSetName) {
        this.dataSetName = dataSetName;
    }

    /**
     * @return the workingUser
     */
    public String getWorkingUser() {
        return workingUser;
    }

    /**
     * @param workingUser
     *            the workingUser to set
     */
    public void setWorkingUser(String workingUser) {
        this.workingUser = workingUser;
    }

    /**
     * @return the workingCopy
     */
    public boolean isWorkingCopy() {
        return workingCopy;
    }

    /**
     * @param workingCopy
     *            the workingCopy to set
     */
    public void setWorkingCopy(boolean workingCopy) {
        this.workingCopy = workingCopy;
    }

    /**
     * @return the attributeValue
     */
    public String getAttributeValue() {
        return attributeValue;
    }

    /**
     * @param attributeValue
     *            the attributeValue to set
     */
    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    /**
     * @return identifier of the data element
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     *
     * @param identifier
     *            attribute value to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * indicates if element is taken from an external schema.
     *
     * @return true if identifier contains colon, for example geo:lat
     */
    public boolean isExternalSchema() {
        return StringUtils.contains(identifier, ":");
    }

    /**
     * returns external namespace prefix.
     *
     * @return NS prefix. null if an internal namespace
     */
    public String getNameSpacePrefix() {
        return isExternalSchema() ? StringUtils.substringBefore(identifier, ":") : null;
    }

    public List<FixedValue> getFixedValues() {
        return fixedValues;
    }

    public void setFixedValues(List<FixedValue> fixedValues) {
        this.fixedValues = fixedValues;
    }

    public boolean isFixedValuesElement() {
        return type != null && type.equalsIgnoreCase("CH1");
    }

    public Map<String, List<String>> getElemAttributeValues() {
        return elemAttributeValues;
    }

    public void setElemAttributeValues(Map<String, List<String>> elemAttributeValues) {
        this.elemAttributeValues = elemAttributeValues;
    }

    /**
     * Returns Datatype.
     *
     * @return Datatype in M_ATTRIBUTES. If not specified, "string" is returned
     */
    public String getDatatype() {
        String dataType = "string";
        List<String> elemDatatypeAttr = elemAttributeValues != null ? elemAttributeValues.get("Datatype") : null;

        return elemDatatypeAttr != null ? elemDatatypeAttr.get(0) : dataType;
    }

    public String getAttributeLanguage() {
        return attributeLanguage;
    }

    public void setAttributeLanguage(String attributeLanguage) {
        this.attributeLanguage = attributeLanguage;
    }



    /**
     * Checks if given element is used for describing relations.
     *
     * @return true if an relation element
     */
    public boolean isRelationalElement() {
        //this DAO class is used for metadata and data element with values
        return (relatedConceptId != null && relatedConceptId != 0) || getDatatype().equals("localref");
    }

    public Integer getRelatedConceptId() {
        return relatedConceptId;
    }

    public void setRelatedConceptId(Integer relatedConceptId) {
        this.relatedConceptId = relatedConceptId;
    }

    public String getRelatedConceptIdentifier() {
        return relatedConceptIdentifier;
    }

    public void setRelatedConceptIdentifier(String relatedConceptIdentifier) {
        this.relatedConceptIdentifier = relatedConceptIdentifier;
    }

    public String getRelatedConceptLabel() {
        return relatedConceptLabel;
    }

    public void setRelatedConceptLabel(String relatedConceptLabel) {
        this.relatedConceptLabel = relatedConceptLabel;
    }

    /**
     * returns Name attribute. Short name if data element does not have name.
     * @return name in ATTRIBUTES table
     */
    public String getName() {
        if (elemAttributeValues != null) {
            if (elemAttributeValues.containsKey("Name")) {
                return elemAttributeValues.get("Name").get(0);
            }
        }

        return shortName;
    }

    /**
     * Indicates if Element values can have values in several languages.
     * false by defaule
     * @return is Language used in ATTRIBUTES table
     */
    public boolean isLanguageUsed() {
        if (elemAttributeValues != null) {
            if (elemAttributeValues.containsKey("languageUsed")) {
                return elemAttributeValues.get("languageUsed").get(0).equals("1");
            }
        }

        return false;
    }
}
