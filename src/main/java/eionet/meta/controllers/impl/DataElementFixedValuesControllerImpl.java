package eionet.meta.controllers.impl;

import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.application.AppContextProvider;
import eionet.util.CompoundDataObject;
import eionet.meta.controllers.DataElementFixedValuesController;
import eionet.meta.application.errors.NotAWorkingCopyException;
import eionet.meta.application.errors.ResourceNotFoundException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.UserAuthorizationException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.application.errors.fixedvalues.FixedValueOwnerNotFoundException;
import eionet.meta.application.errors.fixedvalues.NotAFixedValueOwnerException;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.service.DataElementsService;
import eionet.meta.service.FixedValuesService;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@Controller
public class DataElementFixedValuesControllerImpl implements DataElementFixedValuesController {
    
    private final DataElementsService dataElementsService;
    private final FixedValuesService fixedValuesService;
    private final IDataElementDAO dataElementDao;
    private final IFixedValueDAO fixedValueDao;
    
    @Autowired
    public DataElementFixedValuesControllerImpl(
            DataElementsService dataElementsService, FixedValuesService fixedValuesService, 
            IDataElementDAO dataElementDao, IFixedValueDAO fixedValueDao) {
        this.dataElementsService = dataElementsService;
        this.fixedValuesService = fixedValuesService;
        this.dataElementDao = dataElementDao;
        this.fixedValueDao = fixedValueDao;
    }

    @Override
    public DataElement getOwnerDataElement(AppContextProvider contextProvider, int ownerDataElementId, boolean isEditRequest) 
            throws UserAuthenticationException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException, 
                   FixedValueOwnerNotEditableException, UserAuthorizationException {
        DataElement owner;
        
        try {
            owner = this.dataElementsService.getDataElement(contextProvider, ownerDataElementId, !isEditRequest);
        }
        catch (ResourceNotFoundException ex) {
            throw new FixedValueOwnerNotFoundException((Integer)ex.getResourceId());
        }
        catch (NotAWorkingCopyException ex) {
            throw new FixedValueOwnerNotEditableException(ex);
        }
        
        this.ensureFixedValueOwnership(owner);
        
        return owner;
    }
    
    @Override
    public CompoundDataObject getSingleValueModel(AppContextProvider contextProvider, int ownerDataElementId, String fixedValue, boolean isEditRequest) 
            throws UserAuthenticationException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException, 
                   FixedValueOwnerNotEditableException, UserAuthorizationException, FixedValueNotFoundException {
        DataElement ownerElement = this.getOwnerDataElement(contextProvider, ownerDataElementId, isEditRequest);
        FixedValue value = this.fixedValuesService.getFixedValue(ownerElement, fixedValue);
        CompoundDataObject result = new CompoundDataObject();
        result.put(PROPERTY_OWNER_DATA_ELEMENT, ownerElement);
        result.put(PROPERTY_FIXED_VALUE, value);
        
        return result;
    }

    @Override
    public CompoundDataObject getAllValuesModel(AppContextProvider contextProvider, int ownerDataElementId, boolean isEditRequest) 
            throws UserAuthenticationException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException, 
                   FixedValueOwnerNotEditableException, UserAuthorizationException {
        DataElement ownerElement = this.getOwnerDataElement(contextProvider, ownerDataElementId, isEditRequest);
        Collection<FixedValue> fixedValues = this.dataElementDao.getFixedValues(ownerElement.getId());
        CompoundDataObject result = new CompoundDataObject();
        result.put(PROPERTY_OWNER_DATA_ELEMENT, ownerElement);
        result.put(PROPERTY_FIXED_VALUES, fixedValues);
        
        return result;
    }

    @Override
    public void saveFixedValue(AppContextProvider contextProvider, int ownerDataElementId, String originalValue, FixedValue fixedValue) 
            throws UserAuthenticationException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, NotAFixedValueOwnerException, 
                   FixedValueOwnerNotEditableException, UserAuthorizationException, EmptyValueException, DuplicateResourceException {
        if (fixedValue == null) {
            throw new IllegalArgumentException();
        }
        
        DataElement ownerElement = this.getOwnerDataElement(contextProvider, ownerDataElementId, true);
        this.fixedValuesService.saveFixedValue(ownerElement, originalValue, fixedValue);
    }

    @Override
    public void deleteFixedValue(AppContextProvider contextProvider, int ownerDataElementId, String fixedValue) 
            throws UserAuthenticationException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException, 
                   FixedValueOwnerNotEditableException, UserAuthorizationException, FixedValueNotFoundException {
        CompoundDataObject result = this.getSingleValueModel(contextProvider, ownerDataElementId, fixedValue, true);
        FixedValue  fxv = result.get(PROPERTY_FIXED_VALUE);
        this.fixedValueDao.deleteById(fxv.getId());
    }

    @Override
    public void deleteFixedValues(AppContextProvider contextProvider, int ownerDataElementId) 
            throws UserAuthenticationException, FixedValueOwnerNotFoundException, FixedValueOwnerNotEditableException, 
                   NotAFixedValueOwnerException, UserAuthorizationException {
        DataElement ownerElement = this.getOwnerDataElement(contextProvider, ownerDataElementId, true);
        this.fixedValueDao.deleteAll(FixedValue.OwnerType.DATA_ELEMENT, ownerElement.getId());
    }
    
    private void ensureFixedValueOwnership(DataElement owner) throws NotAFixedValueOwnerException {
        DataElement.DataElementValueType valueType = DataElement.DataElementValueType.parse(owner.getType());
        
        if (valueType != DataElement.DataElementValueType.FIXED && valueType != DataElement.DataElementValueType.QUANTITIVE) {
            throw new NotAFixedValueOwnerException();
        }
    }
    
}
