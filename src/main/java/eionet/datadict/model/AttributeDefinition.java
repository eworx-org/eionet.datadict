/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.datadict.model;

import eionet.datadict.model.enums.Enumerations.AttributeDataType;
import eionet.datadict.model.enums.Enumerations.AttributeDisplayMultiple;
import eionet.datadict.model.enums.Enumerations.AttributeDisplayType;
import eionet.datadict.model.enums.Enumerations.Inherit;
import eionet.datadict.model.enums.Enumerations.Obligation;
import java.util.List;

/**
 *
 * @author eworx-alk
 */
public class AttributeDefinition {

    private int id;
    private int displayOrder;
    private int displayWhen;
    private int displayWidth;
    private int displayHeight;       
    private boolean languageUsed;

    private String rdfPropertyName;
    private String name;
    private String definition;
    private String shortName;

    private AttributeDisplayType displayType;
    private AttributeDisplayMultiple displayMultiple;
    private AttributeDataType datatype;
    private Obligation obligationLevel;
    private Inherit inherit;

    private Namespace namespace;
    private RdfNamespace rdfNamespace;
    private List<Attribute> attributes;
    
    
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setObligationLevel(Obligation obligationLevel) {
        this.obligationLevel = obligationLevel;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void setDisplayType(AttributeDisplayType displayType) {
        this.displayType = displayType;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setDisplayWhen(int displayWhen) {
        this.displayWhen = displayWhen;
    }

    public void setDisplayWidth(int displayWidth) {
        this.displayWidth = displayWidth;
    }

    public void setDisplayHeight(int displayHeight) {
        this.displayHeight = displayHeight;
    }

    public void setDisplayMultiple(AttributeDisplayMultiple displayMultiple) {
        this.displayMultiple = displayMultiple;
    }

    public void setInherit(Inherit inherit) {
        this.inherit = inherit;
    }

    public void setLanguageUsed(boolean languageUsed) {
        this.languageUsed = languageUsed;
    }

    public void setDatatype(AttributeDataType datatype) {
        this.datatype = datatype;
    }

    public void setRdfPropertyName(String rdfPropertyName) {
        this.rdfPropertyName = rdfPropertyName;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    public Obligation getObligationLevel() {
        return obligationLevel;
    }

    public String getDefinition() {
        return definition;
    }

    public String getShortName() {
        return shortName;
    }

    public AttributeDisplayType getDisplayType() {
        return displayType;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public int getDisplayWhen() {
        return displayWhen;
    }

    public int getDisplayWidth() {
        return displayWidth;
    }
    
    public int getDisplayHeight() {
        return displayHeight;
    }

    public AttributeDisplayMultiple getDisplayMultiple() {
        return displayMultiple;
    }

    public Inherit getInherit() {
        return inherit;
    }

    public boolean isLanguageUsed() {
        return languageUsed;
    }

    public AttributeDataType getDatatype() {
        return datatype;
    }

    public String getRdfPropertyName() {
        return rdfPropertyName;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

     public RdfNamespace getRdfNamespace() {
        return rdfNamespace;
    }

    public void setRdfNameSpace(RdfNamespace rdfNameSpace) {
        this.rdfNamespace = rdfNameSpace;
    }
    
    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }
    
    public void setUnknownNames () {
        if (this.name == null) {
            this.name = "unknown";
        }
        if (this.shortName == null) {
            this.shortName = "unknown";
        }
    }
}
