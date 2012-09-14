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

package eionet.meta.dao;

import java.sql.SQLException;
import java.util.List;

import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.service.data.DataElementsFilter;
import eionet.meta.service.data.DataElementsResult;

/**
 * Data element DAO.
 *
 * @author Juhan Voolaid
 */
public interface IDataElementDAO {

    /**
     * Search data elements.
     *
     * @param filter
     * @return
     */
    DataElementsResult searchDataElements(DataElementsFilter filter);

    /**
     * Returns data element attributes.
     *
     * @return
     */
    List<Attribute> getDataElementAttributes() throws SQLException;

    /**
     * Returns data element's fixed values.
     *
     * @return
     */
    List<FixedValue> getFixedValues(int dataElementId);

    /**
     * Returns data element by id.
     *
     * @param id
     * @return
     */
    DataElement getDataElement(int id);

}
