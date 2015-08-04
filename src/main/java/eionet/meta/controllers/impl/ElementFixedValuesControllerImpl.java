package eionet.meta.controllers.impl;

import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.controllers.AppContextProvider;
import eionet.util.CompoundDataObject;
import eionet.meta.controllers.ElementFixedValuesController;
import eionet.meta.application.errors.MalformedIdentifierException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.UserAuthorizationException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.application.errors.fixedvalues.FixedValueOwnerNotFoundException;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.service.IDataService;
import eionet.meta.service.ServiceException;
import java.util.Collection;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@Controller
public class ElementFixedValuesControllerImpl implements ElementFixedValuesController {
    
    private final IDataService dataService;
    
    @Autowired
    public ElementFixedValuesControllerImpl(IDataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public DataElement getOwnerDataElement(AppContextProvider contextProvider, String ownerDataElementId, boolean isEditRequest) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                   FixedValueOwnerNotEditableException, UserAuthorizationException, ServiceException {
        if (!contextProvider.isUserAuthenticated()) {
            throw new UserAuthenticationException();
        }
        
        int elementId = this.convertStringIdentifier(ownerDataElementId);
        DataElement ownerElement = this.getDataElement(elementId);
        
        if (isEditRequest) {
            this.checkEditability(contextProvider, ownerElement);
        }
        
        return ownerElement;
    }
    
    @Override
    public CompoundDataObject getSingleValueModel(AppContextProvider contextProvider, String ownerDataElementId, String fixedValue, boolean isEditRequest) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException,
                   FixedValueOwnerNotEditableException, UserAuthorizationException, FixedValueNotFoundException, ServiceException {
        DataElement ownerElement = this.getOwnerDataElement(contextProvider, ownerDataElementId, isEditRequest);
        FixedValue value = this.getFixedValue(ownerElement, fixedValue);
        CompoundDataObject result = new CompoundDataObject();
        result.put(PROPERTY_OWNER_DATA_ELEMENT, ownerElement);
        result.put(PROPERTY_FIXED_VALUE, value);
        
        return result;
    }

    @Override
    public CompoundDataObject getAllValuesModel(AppContextProvider contextProvider, String ownerDataElementId, boolean isEditRequest) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException,
                   FixedValueOwnerNotEditableException, UserAuthorizationException, ServiceException {
        DataElement ownerElement = this.getOwnerDataElement(contextProvider, ownerDataElementId, isEditRequest);
        Collection<FixedValue> fixedValues = this.getFixedValues(ownerElement.getId());
        CompoundDataObject result = new CompoundDataObject();
        result.put(PROPERTY_OWNER_DATA_ELEMENT, ownerElement);
        result.put(PROPERTY_FIXED_VALUES, fixedValues);
        
        return result;
    }

    @Override
    public void saveFixedValue(AppContextProvider contextProvider, String ownerDataElementId, FixedValue fixedValue) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException,
                   FixedValueOwnerNotEditableException, UserAuthorizationException, EmptyValueException, DuplicateResourceException, ServiceException {
        if (fixedValue == null) {
            throw new IllegalArgumentException();
        }
        
        DataElement ownerElement = this.getOwnerDataElement(contextProvider, ownerDataElementId, true);
        
        if (StringUtils.isBlank(fixedValue.getValue())) {
            throw new EmptyValueException();
        }
        
        if (fixedValue.getId() == 0) {
            this.insertFixedValue(ownerElement, fixedValue);
        }
        else {
            this.updateFixedValue(ownerElement, fixedValue);
        }
    }

    @Override
    public void deleteFixedValue(AppContextProvider contextProvider, String ownerDataElementId, String fixedValue) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException,
                   FixedValueOwnerNotEditableException, UserAuthorizationException, FixedValueNotFoundException, ServiceException {
        CompoundDataObject result = this.getSingleValueModel(contextProvider, ownerDataElementId, fixedValue, true);
        FixedValue  fxv = result.get(PROPERTY_FIXED_VALUE);
        this.dataService.deleteFixedValue(fxv);
    }

    @Override
    public void deleteFixedValues(AppContextProvider contextProvider, String ownerDataElementId) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueOwnerNotEditableException, 
                   UserAuthorizationException, ServiceException {
        DataElement ownerElement = this.getOwnerDataElement(contextProvider, ownerDataElementId, true);
        this.dataService.deleteFixedValues(FixedValue.OwnerType.DATA_ELEMENT, ownerElement.getId());
    }
    
    private int convertStringIdentifier(String id) throws MalformedIdentifierException {
        try {
            return Integer.parseInt(id);
        }
        catch (NumberFormatException ex) {
            throw new MalformedIdentifierException(id, ex);
        }
    }
    
    private DataElement getDataElement(int elementId) throws FixedValueOwnerNotFoundException, ServiceException {
        if (!this.dataService.dataElementExists(elementId)) {
            throw new FixedValueOwnerNotFoundException(elementId);
        }
        
        return this.dataService.getDataElement(elementId);
    }
    
    private void checkEditability(AppContextProvider contextProvider, DataElement dataElement) 
            throws FixedValueOwnerNotEditableException, UserAuthorizationException, ServiceException {
        boolean workingCopy;
        String workingUser;
        
        if (dataElement.isCommonElement()) {
            workingCopy = dataElement.isWorkingCopy();
            workingUser = dataElement.getWorkingUser();
        }
        else {
            DataSet parentDataSet = this.dataService.getDataElementParentDataSet(dataElement.getId());
            
            if (parentDataSet == null) {
                throw new IllegalStateException();
            }
            
            workingCopy = parentDataSet.isWorkingCopy();
            workingUser = parentDataSet.getWorkingUser();
        }
        
        if (!workingCopy) {
            throw new FixedValueOwnerNotEditableException();
        }
        
        if (!ObjectUtils.equals(workingUser, contextProvider.getUserName())) {
            throw new UserAuthorizationException();
        }
    }
    
    private FixedValue getFixedValue(DataElement owner, String fixedValue) throws FixedValueNotFoundException, ServiceException {
        FixedValue fxv = this.dataService.getFixedValue(FixedValue.OwnerType.DATA_ELEMENT, owner.getId(), fixedValue);
        
        if (fxv == null) {
            throw new FixedValueNotFoundException(fixedValue);
        }
        
        return fxv;
    }
    
    private Collection<FixedValue> getFixedValues(int elementId) throws ServiceException {
        return this.dataService.getDataElementFixedValues(elementId);
    }
    
    private void insertFixedValue(DataElement ownerElement, FixedValue fixedValue) throws DuplicateResourceException, ServiceException {
        if (this.dataService.fixedValueExistsWithSameNameOwner(FixedValue.OwnerType.DATA_ELEMENT, ownerElement.getId(), fixedValue.getValue())) {
            throw new DuplicateResourceException(fixedValue.getValue());
        }
        
        fixedValue.setOwnerId(ownerElement.getId());
        fixedValue.setOwnerType(FixedValue.OwnerType.DATA_ELEMENT.toString());
        fixedValue.setDefaultValue(false);
        this.dataService.createFixedValue(fixedValue);
    }
    
    private void updateFixedValue(DataElement ownerElement, FixedValue fixedValue) 
            throws FixedValueNotFoundException, DuplicateResourceException, ServiceException {
        FixedValue fxv = this.dataService.getFixedValueById(fixedValue.getId());
        
        if (fxv == null) {
            throw new FixedValueNotFoundException(fixedValue.getValue());
        }
        
        if (!FixedValue.OwnerType.DATA_ELEMENT.isMatch(fxv.getOwnerType()) || fxv.getOwnerId() != ownerElement.getId()) {
            throw new IllegalStateException();
        }
        
        FixedValue fxvByValue = this.dataService.getFixedValue(FixedValue.OwnerType.DATA_ELEMENT, fxv.getOwnerId(), fixedValue.getValue());
        
        if (fxvByValue != null && fxv.getId() != fxvByValue.getId()) {
            throw new DuplicateResourceException(fxv.getValue());
        }
        
        fxv.setDefinition(fixedValue.getDefinition());
        fxv.setShortDescription(fixedValue.getShortDescription());
        fxv.setValue(fixedValue.getValue());
        this.dataService.updateFixedValue(fxv);
    }
}
