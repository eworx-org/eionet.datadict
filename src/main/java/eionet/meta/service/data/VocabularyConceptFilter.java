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

package eionet.meta.service.data;

import java.util.List;

/**
 * Vocabulary concept search filter.
 *
 * @author Juhan Voolaid
 */
public class VocabularyConceptFilter extends PagedRequest {

    /** Vocabulary folder id. */
    private int vocabularyFolderId;

    /** Text search value. */
    private String text;

    /** Identifier exact search value. */
    private String identifier;

    /** Definition exact search value. */
    private String definition;

    /** Label exact search value. */
    private String label;

    /** If ture, the identifier sorting is numeric. */
    private boolean numericIdentifierSorting;

    /** Concept id's that don't get returned. */
    private List<Integer> excludedIds;

    /** Concept id's that get returned. */
    private List<Integer> includedIds;

    /**
     * @return the vocabularyFolderId
     */
    public int getVocabularyFolderId() {
        return vocabularyFolderId;
    }

    /**
     * @param vocabularyFolderId
     *            the vocabularyFolderId to set
     */
    public void setVocabularyFolderId(int vocabularyFolderId) {
        this.vocabularyFolderId = vocabularyFolderId;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text
     *            the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier
     *            the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the definition
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * @param definition
     *            the definition to set
     */
    public void setDefinition(String definition) {
        this.definition = definition;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     *            the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the numericIdentifierSorting
     */
    public boolean isNumericIdentifierSorting() {
        return numericIdentifierSorting;
    }

    /**
     * @param numericIdentifierSorting
     *            the numericIdentifierSorting to set
     */
    public void setNumericIdentifierSorting(boolean numericIdentifierSorting) {
        this.numericIdentifierSorting = numericIdentifierSorting;
    }

    /**
     * @return the excludedIds
     */
    public List<Integer> getExcludedIds() {
        return excludedIds;
    }

    /**
     * @param excludedIds
     *            the excludedIds to set
     */
    public void setExcludedIds(List<Integer> excludedIds) {
        this.excludedIds = excludedIds;
    }

    /**
     * @return the includedIds
     */
    public List<Integer> getIncludedIds() {
        return includedIds;
    }

    /**
     * @param includedIds
     *            the includedIds to set
     */
    public void setIncludedIds(List<Integer> includedIds) {
        this.includedIds = includedIds;
    }

}
