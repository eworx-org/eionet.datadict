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
 * The Original Code is Data Dictionary
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * TripleDev
 */

package eionet.meta.imp;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVReader;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.StandardGenericStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.DataElementsFilter;
import eionet.util.Pair;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.Util;
import eionet.util.VocabularyCSVOutputHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Includes code for parsing and handling CSV lines.
 *
 * @author enver
 */
 //@Configurable
public class VocabularyCSVImportHandler extends VocabularyImportBaseHandler {

    /** Static logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyCSVImportHandler.class);

    /**
     * CSV file reader.
     */
    private Reader content;
    /**
     * Elements filter to be used in search.
     */
    private final DataElementsFilter elementsFilter;

    /** The map containing identifier:notation pairs of concepts present in DD's own status vocabulary in the database. */
    private Map<String, String> statusVocabularyEntries;

    /** */
    private static final String DD_OWN_STATUS_VOCABULARY_URI = VocabularyFolder.OWN_VOCABULARIES_FOLDER_URI + "/"
            + Props.getRequiredProperty(PropsIF.DD_OWN_STATUS_VOCABULARY_IDENTIFIER);

    /**
     * @param folderContextRoot
     *            base uri for vocabulary.
     * @param concepts
     *            concepts of vocabulary
     * @param boundElements
     *            bound elements to vocabulary
     * @param content
     *            reader to read file contents
     */
    public VocabularyCSVImportHandler(String folderContextRoot, List<VocabularyConcept> concepts,
            Map<String, Integer> boundElements, Reader content) {
        super(folderContextRoot, concepts, boundElements);
        this.content = content;
        this.elementsFilter = new DataElementsFilter();
        this.elementsFilter.setRegStatus("Released");
        this.elementsFilter.setElementType(DataElementsFilter.COMMON_ELEMENT_TYPE);
        this.elementsFilter.setIncludeHistoricVersions(false);
        this.elementsFilter.setExactIdentifierMatch(true);
    }

    /**
     * In this method, beans are generated (either created or updated) according to values in CSV file.
     *
     * @throws eionet.meta.service.ServiceException
     *             if there is the input is invalid
     */
    public void generateUpdatedBeans() throws ServiceException {
        // content.
        CSVReader reader = new CSVReader(this.content);

        try {
            String[] header = reader.readNext();

            // first check if headers contains fix columns
            String[] fixedHeaders = new String[VocabularyCSVOutputHelper.CONCEPT_ENTRIES_COUNT];
            VocabularyCSVOutputHelper.addFixedEntryHeaders(fixedHeaders);

            // compare if it has URI
            boolean isEqual =
                    StringUtils.equalsIgnoreCase(header[VocabularyCSVOutputHelper.URI_INDEX],
                            fixedHeaders[VocabularyCSVOutputHelper.URI_INDEX]);

            if (!isEqual) {
                reader.close();
                throw new ServiceException("Missing header! CSV file should start with header: '"
                        + fixedHeaders[VocabularyCSVOutputHelper.URI_INDEX] + "'");
            }

            List<String> fixedHeadersList =
                    new ArrayList<String>(Arrays.asList(Arrays.copyOf(fixedHeaders,
                            VocabularyCSVOutputHelper.CONCEPT_ENTRIES_COUNT)));
            // remove uri from header
            fixedHeadersList.remove(VocabularyCSVOutputHelper.URI_INDEX);
            Map<String, Integer> fixedHeaderIndices = new HashMap<String, Integer>();
            for (int i = VocabularyCSVOutputHelper.URI_INDEX + 1; i < header.length; i++) {
                String elementHeader = StringUtils.trimToNull(header[i]);
                if (StringUtils.isBlank(elementHeader)) {
                    throw new ServiceException("Header for column (" + (i + 1) + ") is empty!");
                }

                int headerIndex = -1;
                boolean headerFound = false;
                for (headerIndex = 0; headerIndex < fixedHeadersList.size(); headerIndex++) {
                    if (StringUtils.equalsIgnoreCase(elementHeader, fixedHeadersList.get(headerIndex))) {
                        headerFound = true;
                        break;
                    }
                }

                // if it is a fixed header value (concept property), add to map and continue
                if (headerFound) {
                    String headerValue = fixedHeadersList.remove(headerIndex);
                    fixedHeaderIndices.put(headerValue, i);
                    continue;
                }

                // it is not a concept attribute and but a data element identifier
                // if there is language appended, split it
                String[] tempStrArray = elementHeader.split("[@]");
                if (tempStrArray.length == 2) {
                    elementHeader = tempStrArray[0];
                }

                // if bound elements do not contain header already, add it (if possible)
                if (!this.boundElementsIds.containsKey(elementHeader)) {
                    // search for data element
                    this.elementsFilter.setIdentifier(elementHeader);
                    List<DataElement> dataElements  = this.dataService.searchDataElements(this.elementsFilter);
                    // if there is one and only one element check if header and identifer exactly matches!
                    if (dataElements.size() < 1) {
                        throw new ServiceException("Cannot find any data element for column: " + elementHeader
                                + ". Please bind element manually then upload CSV.");
                    } else if (dataElements.size()> 1) {
                        throw new ServiceException("Cannot find single data element for column: " + elementHeader
                                + ". Search returns: " + dataElements.size()
                                + " elements. Please bind element manually then upload CSV.");
                    } else {
                        DataElement elem = dataElements.get(0);
                        if (StringUtils.equals(elementHeader, elem.getIdentifier())) {
                            // found it, add to list and map
                            this.boundElementsIds.put(elementHeader, elem.getId());
                            this.newBoundElement.add(elem);
                        } else {
                            throw new ServiceException("Found data element did not EXACTLY match with column: " + elementHeader
                                    + ", found: " + elem.getIdentifier());
                        }
                    }
                }
            } // end of for loop iterating on headers

            String[] lineParams;
            // first row is header so start from 2
            for (int rowNumber = 2; (lineParams = reader.readNext()) != null; rowNumber++) {
                if (lineParams.length != header.length) {
                    StringBuilder message = new StringBuilder();
                    message.append("Row (").append(rowNumber).append(") ");
                    message.append("did not have same number of columns with header, it was skipped.");
                    message.append(" It should have have same number of columns (empty or filled).");
                    this.logMessages.add(message.toString());
                    continue;
                }

                // do line processing
                String uri = lineParams[VocabularyCSVOutputHelper.URI_INDEX];
                if (StringUtils.isEmpty(uri)) {
                    this.logMessages.add("Row (" + rowNumber + ") was skipped (Base URI was empty).");
                    continue;
                } else if (StringUtils.startsWith(uri, "//")) {
                    this.logMessages.add("Row (" + rowNumber
                            + ") was skipped (Concept was excluded by user from update operation).");
                    continue;
                } else if (!StringUtils.startsWith(uri, this.folderContextRoot)) {
                    this.logMessages.add("Row (" + rowNumber + ") was skipped (Base URI did not match with Vocabulary).");
                    continue;
                }

                String conceptIdentifier = uri.replace(this.folderContextRoot, "");
                if (StringUtils.contains(conceptIdentifier, "/") || !Util.isValidIdentifier(conceptIdentifier)) {
                    this.logMessages.add("Row (" + rowNumber + ") did not contain a valid concept identifier.");
                    continue;
                }

                // now we have a valid row
                Pair<VocabularyConcept, Boolean> foundConceptWithFlag = findOrCreateConcept(conceptIdentifier);

                // if vocabulary concept duplicated with another row, importer will ignore it not to repeat
                if (foundConceptWithFlag == null || foundConceptWithFlag.getRight()) {
                    this.logMessages.add("Row (" + rowNumber + ") duplicated with a previous concept, it was skipped.");
                    continue;
                }

                VocabularyConcept lastFoundConcept = foundConceptWithFlag.getLeft();
                // vocabulary concept found or created
                this.toBeUpdatedConcepts.add(lastFoundConcept);

                Integer conceptPropertyIndex = null;
                // check label
                conceptPropertyIndex = fixedHeaderIndices.get(fixedHeaders[VocabularyCSVOutputHelper.LABEL_INDEX]);
                if (conceptPropertyIndex != null) {
                    lastFoundConcept.setLabel(StringUtils.trimToNull(lineParams[conceptPropertyIndex]));
                }

                // check definition
                conceptPropertyIndex = fixedHeaderIndices.get(fixedHeaders[VocabularyCSVOutputHelper.DEFINITION_INDEX]);
                if (conceptPropertyIndex != null) {
                    lastFoundConcept.setDefinition(StringUtils.trimToNull(lineParams[conceptPropertyIndex]));
                }

                // check notation
                conceptPropertyIndex = fixedHeaderIndices.get(fixedHeaders[VocabularyCSVOutputHelper.NOTATION_INDEX]);
                if (conceptPropertyIndex != null) {
                    lastFoundConcept.setNotation(StringUtils.trimToNull(lineParams[conceptPropertyIndex]));
                }

                conceptPropertyIndex = fixedHeaderIndices.get(fixedHeaders[VocabularyCSVOutputHelper.STATUS_INDEX]);
                if (conceptPropertyIndex != null) {
                    setConceptStatus(lastFoundConcept, StringUtils.trimToNull(lineParams[conceptPropertyIndex]));
                }

                // TODO: update - with merging flexible csv import
                // check start date
                // ignore status and accepteddate changes

                // now it is time iterate on rest of the columns, here is the tricky part
                List<DataElement> elementsOfConcept = null;
                List<DataElement> elementsOfConceptByLang = null;
                String prevHeader = null;
                String prevLang = null;
                for (int k = VocabularyCSVOutputHelper.URI_INDEX + 1; k < lineParams.length; k++) {
                    if (StringUtils.isEmpty(lineParams[k])) {
                        // value is empty, no need to proceed
                        continue;
                    }

                    if (fixedHeaderIndices.containsValue(k)) {
                        // concept property, already handled
                        continue;
                    }

                    String elementHeader = header[k];
                    String lang = null;
                    String[] tempStrArray = elementHeader.split("[@]");
                    if (tempStrArray.length == 2) {
                        elementHeader = tempStrArray[0];
                        lang = tempStrArray[1];
                    }

                    if (!StringUtils.equals(elementHeader, prevHeader)) {
                        elementsOfConcept = getDataElementValuesByName(elementHeader, lastFoundConcept.getElementAttributes());
                        if (elementsOfConcept == null) {
                            elementsOfConcept = new ArrayList<DataElement>();
                            lastFoundConcept.getElementAttributes().add(elementsOfConcept);
                        }
                    }

                    if (!StringUtils.equals(elementHeader, prevHeader) || !StringUtils.equals(lang, prevLang)) {
                        elementsOfConceptByLang =
                                getDataElementValuesByNameAndLang(elementHeader, lang, lastFoundConcept.getElementAttributes());
                    }

                    prevLang = lang;
                    prevHeader = elementHeader;

                    VocabularyConcept foundRelatedConcept = null;
                    if (Util.isValidUri(lineParams[k])) {
                        foundRelatedConcept = findRelatedConcept(lineParams[k]);
                    }

                    // check for pre-existence of the VCE by attribute value or related concept id
                    Integer relatedId = null;
                    if (foundRelatedConcept != null) {
                        relatedId = foundRelatedConcept.getId();
                    }
                    boolean returnFromThisPoint = false;
                    for (DataElement elemByLang : elementsOfConceptByLang) {
                        String elementValueByLang = elemByLang.getAttributeValue();
                        if (StringUtils.equals(lineParams[k], elementValueByLang)) {
                            // vocabulary concept element already in database, no need to continue, return
                            returnFromThisPoint = true;
                            break;
                        }

                        if (relatedId != null) {
                            Integer relatedConceptId = elemByLang.getRelatedConceptId();
                            if (relatedConceptId != null && relatedConceptId.intValue() == relatedId.intValue()) {
                                // vocabulary concept element already in database, no need to continue, return
                                returnFromThisPoint = true;
                                break;
                            }
                        }
                    }
                    // check if an existing VCE found or not
                    if (returnFromThisPoint) {
                        continue;
                    }

                    // create VCE
                    DataElement elem = new DataElement();
                    elementsOfConcept.add(elem);
                    elem.setAttributeLanguage(lang);
                    elem.setIdentifier(elementHeader);
                    elem.setId(this.boundElementsIds.get(elementHeader));
                    // check if there is a found related concept
                    if (foundRelatedConcept != null) {
                        elem.setRelatedConceptIdentifier(foundRelatedConcept.getIdentifier());
                        int id = foundRelatedConcept.getId();
                        elem.setRelatedConceptId(id);
                        elem.setAttributeValue(null);
                        if (id < 0) {
                            addToElementsReferringNotCreatedConcepts(id, elem);
                        }
                    } else {
                        elem.setAttributeValue(lineParams[k]);
                        elem.setRelatedConceptId(null);
                    }
                } // end of for loop iterating on rest of the columns (for data elements)
            } // end of row iterator (while loop on rows)
            processUnseenConceptsForRelatedElements();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Set given status to the given concept.
     *
     * @param concept
     * @param status
     * @throws ServiceException
     */
    private void setConceptStatus(VocabularyConcept concept, String status) throws ServiceException {

        if (concept == null || StringUtils.isBlank(status)) {
            return;
        }

        if (statusVocabularyEntries == null) {
            loadStatusVocabularyEntries();
        }

        boolean isLiteralValue = !Util.isURL(status);
        if (!isLiteralValue) {
            if (status.startsWith(DD_OWN_STATUS_VOCABULARY_URI + "/")) {

                String statusIdentifier = StringUtils.substringAfter(status, DD_OWN_STATUS_VOCABULARY_URI + "/");
                if (statusVocabularyEntries.keySet().contains(statusIdentifier)) {
                    StandardGenericStatus statusEnum = StandardGenericStatus.fromIdentifier(statusIdentifier);
                    if (statusEnum != null) {
                        concept.setStatus(statusEnum);
                    }
                }
            }
        } else {
            for (Entry<String, String> entry : statusVocabularyEntries.entrySet()) {
                if (StringUtils.equals(entry.getValue(), status)) {
                    StandardGenericStatus statusValue = StandardGenericStatus.fromIdentifier(entry.getKey());
                    if (statusValue != null) {
                        concept.setStatus(statusValue);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Loads {@link #statusVocabularyEntries}, see JavaDoc there.
     *
     * @throws ServiceException
     */
    private void loadStatusVocabularyEntries() throws ServiceException {

        String ownVocabulariesFolderName = Props.getRequiredProperty(PropsIF.DD_OWN_VOCABULARIES_FOLDER_NAME);
        String ownStatusVocabularyIdentifier = Props.getRequiredProperty(PropsIF.DD_OWN_STATUS_VOCABULARY_IDENTIFIER);

        statusVocabularyEntries = new HashMap<String, String>();

        LOGGER.debug(String.format("Trying to find DD's own vocabulary of status (folder = %s, vocabulary=%s)", ownVocabulariesFolderName,
                ownStatusVocabularyIdentifier));

        VocabularyFolder statusVoc = null;
        try {
            statusVoc = vocabularyService.getVocabularyWithConcepts(ownStatusVocabularyIdentifier, ownVocabulariesFolderName);
        } catch (Exception e) {
            LOGGER.info("Could not find DD's own status vocabulary: " + e.toString());
        }
        if (statusVoc != null) {

            List<VocabularyConcept> statusConcepts = statusVoc.getConcepts();
            for (VocabularyConcept statusConcept : statusConcepts) {
                this.statusVocabularyEntries.put(statusConcept.getIdentifier(), statusConcept.getNotation());
            }

            LOGGER.debug("Found DD's own vocabulary of status, found these identifier:notation pairs: " + statusVocabularyEntries);
        } else {
            LOGGER.warn("Did not find DD's own vocabulary of status!");
        }
    }
}
