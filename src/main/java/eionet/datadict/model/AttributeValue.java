package eionet.datadict.model;

public class AttributeValue {
    
    private Integer attributeId;
    private String value;
    private DataDictEntity parentEntity;

    public Integer getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public DataDictEntity getParentEntity() {
        return parentEntity;
    }

    public void setParentEntity(DataDictEntity parentEntity) {
        this.parentEntity = parentEntity;
    }

    
    
}
