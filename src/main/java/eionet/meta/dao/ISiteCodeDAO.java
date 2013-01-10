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
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Enriko Käsper
 */

package eionet.meta.dao;

import eionet.meta.service.data.SiteCodeFilter;
import eionet.meta.service.data.SiteCodeResult;

/**
 * Site code DAO interface.
 *
 * @author Enriko Käsper
 */
public interface ISiteCodeDAO {

    /**
     * Search site codes from database using SiteCodeFilter.
     * @param filter
     * @return SiteCodeResult object with found rows.
     */
    public SiteCodeResult searchSiteCodes(SiteCodeFilter filter);

}
