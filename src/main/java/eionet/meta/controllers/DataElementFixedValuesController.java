package eionet.meta.controllers;

import eionet.meta.application.AppContextProvider;
import eionet.util.CompoundDataObject;
import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.MalformedIdentifierException;
import eionet.meta.application.errors.UserAuthorizationException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.application.errors.fixedvalues.FixedValueOwnerNotFoundException;
import eionet.meta.application.errors.fixedvalues.NotAFixedValueOwnerException;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.FixedValue;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface DataElementFixedValuesController {
    
    DataElement getOwnerDataElement(AppContextProvider contextProvider, String ownerDataElementId, boolean isEditRequest)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                   NotAFixedValueOwnerException, FixedValueOwnerNotEditableException, UserAuthorizationException;
    
    CompoundDataObject getSingleValueModel(AppContextProvider contextProvider, String ownerDataElementId, String fixedValue, boolean isEditRequest)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                   FixedValueNotFoundException, NotAFixedValueOwnerException, FixedValueOwnerNotEditableException, UserAuthorizationException;
    
    CompoundDataObject getAllValuesModel(AppContextProvider contextProvider, String ownerDataElementId, boolean isEditRequest)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException,
                   NotAFixedValueOwnerException, FixedValueOwnerNotEditableException, UserAuthorizationException;
    
    void saveFixedValue(AppContextProvider contextProvider, String ownerDataElementId, String originalValue, FixedValue fixedValue)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException,
                   FixedValueNotFoundException, FixedValueOwnerNotEditableException, UserAuthorizationException, 
                   NotAFixedValueOwnerException, DuplicateResourceException, EmptyValueException;
    
    void deleteFixedValue(AppContextProvider contextProvider, String ownerDataElementId, String fixedValue)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                   NotAFixedValueOwnerException, FixedValueNotFoundException, FixedValueOwnerNotEditableException, UserAuthorizationException;
    
    void deleteFixedValues(AppContextProvider contextProvider, String ownerDataElementId)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException,
                   NotAFixedValueOwnerException, FixedValueOwnerNotEditableException, UserAuthorizationException;
    
    public static final String PROPERTY_OWNER_DATA_ELEMENT = "owner";
    public static final String PROPERTY_FIXED_VALUE = "fixedValue";
    public static final String PROPERTY_FIXED_VALUES = "fixedValues";
    
    public static class FixedValueOwnerNotEditableException extends Exception {

        public FixedValueOwnerNotEditableException() { }
        
        public FixedValueOwnerNotEditableException(Throwable cause) {
            super(cause);
        }
        
    }
    
}
