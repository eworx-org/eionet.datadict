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

package eionet.meta.dao.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import eionet.meta.dao.IDataElementDAO;
import eionet.util.Util;
import org.apache.commons.lang.StringUtils;
import org.displaytag.properties.SortOrderEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import eionet.meta.dao.ISiteCodeDAO;
import eionet.meta.dao.domain.SiteCodeStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyType;
import eionet.meta.service.data.SiteCode;
import eionet.meta.service.data.SiteCodeFilter;
import eionet.meta.service.data.SiteCodeResult;

/**
 * Site Code DAO implementation.
 *
 * @author Enriko Käsper
 */
@Repository
public class SiteCodeDAOImpl extends GeneralDAOImpl implements ISiteCodeDAO {

    /**
     * {@inheritDoc}
     */
    /** Data element DAO. */
    @Autowired
    private IDataElementDAO dataElementDao;

    @Override
    public SiteCodeResult searchSiteCodes(SiteCodeFilter filter) {

        /*Map<String, Object> params = new HashMap<String, Object>();
        String sql = getSiteCodesSql(filter, params);

        //TODO change sc to bound elements

        List<SiteCode> resultList = getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<SiteCode>() {
            @Override
            public SiteCode mapRow(ResultSet rs, int rowNum) throws SQLException {
                SiteCode sc = new SiteCode();
                sc.setId(rs.getInt("vc.VOCABULARY_CONCEPT_ID"));
                sc.setIdentifier(rs.getString("vc.IDENTIFIER"));
                sc.setLabel(rs.getString("vc.LABEL"));
                sc.setDefinition(rs.getString("vc.DEFINITION"));
                sc.setNotation(rs.getString("vc.NOTATION"));
                sc.setSiteCodeStatus(SiteCodeStatus.valueOf(rs.getString("sc.STATUS")));
                sc.setCountryCode(rs.getString("sc.CC_ISO2"));
                sc.setDateCreated(rs.getTimestamp("sc.DATE_CREATED"));
                sc.setUserCreated(rs.getString("sc.USER_CREATED"));
                sc.setDateAllocated(rs.getTimestamp("sc.DATE_ALLOCATED"));
                sc.setUserAllocated(rs.getString("sc.USER_ALLOCATED"));
                sc.setInitialSiteName(rs.getString("sc.INITIAL_SITE_NAME"));
                sc.setYearsDeleted(rs.getString("sc.YEARS_DELETED"));
                sc.setYearsDisappeared(rs.getString("sc.YEARS_DISAPPEARED"));
                return sc;
            }
        });

        String totalSql = "SELECT FOUND_ROWS()";
        int totalItems = getJdbcTemplate().queryForObject(totalSql,Integer.class);

        SiteCodeResult result = new SiteCodeResult(resultList, totalItems, filter);

        return result;*/

        List<String> elementIdentifiers = new ArrayList<String>();
        elementIdentifiers.add("sitecodes_CC_ISO2");
        elementIdentifiers.add("sitecodes_INITIAL_SITE_NAME");
        elementIdentifiers.add("sitecodes_STATUS");
        elementIdentifiers.add("sitecodes_DATE_ALLOCATED");
        elementIdentifiers.add("sitecodes_USER_ALLOCATED");
        elementIdentifiers.add("sitecodes_USER_CREATED");
        elementIdentifiers.add("sitecodes_DATE_CREATED");
        elementIdentifiers.add("sitecodes_YEARS_DELETED");
        elementIdentifiers.add("sitecodes_YEARS_DISSAPEARED");

        Map<String, Integer> elementMap = dataElementDao.getMultipleCommonDataElementIds(elementIdentifiers);
        StringBuilder sqlForConceptIds = new StringBuilder();
        sqlForConceptIds.append("select distinct VOCABULARY_CONCEPT_ID from VOCABULARY_CONCEPT_ELEMENT where DATAELEM_ID in (:dataElemIds)");

        Map<String, Object> paramsForElementIds = new HashMap<String, Object>();
        paramsForElementIds.put("dataElemIds", elementMap.values());

        List<Integer> vocabularyConceptIds = getNamedParameterJdbcTemplate().query(sqlForConceptIds.toString(), paramsForElementIds, new RowMapper<Integer>() {
            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return Integer.parseInt( rs.getString("VOCABULARY_CONCEPT_ID") );
            }
        });

        StringBuilder sqlForSiteCodeInfo = new StringBuilder();
        sqlForSiteCodeInfo.append("select vce.*, vc.* from VOCABULARY_CONCEPT_ELEMENT vce inner join VOCABULARY_CONCEPT vc on " +
                " vce.VOCABULARY_CONCEPT_ID = vc.VOCABULARY_CONCEPT_ID where vce.DATAELEM_ID in (:dataElemIds) and vce.VOCABULARY_CONCEPT_ID in (:vocabularyConceptIds)");
        Map<String, Object> paramsForSiteCodeInfo = new HashMap<String, Object>();
        paramsForSiteCodeInfo.put("dataElemIds", elementMap.values());
        paramsForSiteCodeInfo.put("vocabularyConceptIds", vocabularyConceptIds);

        List<SiteCode> siteCodeList = getNamedParameterJdbcTemplate().query(sqlForSiteCodeInfo.toString(), paramsForSiteCodeInfo, new RowMapper<SiteCode>() {
            @Override
            public SiteCode mapRow(ResultSet rs, int rowNum) throws SQLException {
                SiteCode sc = new SiteCode();
                //TODO fix below
                sc.setId(rs.getInt("vc.VOCABULARY_CONCEPT_ID"));
                sc.setIdentifier(rs.getString("vc.IDENTIFIER"));
                sc.setLabel(rs.getString("vc.LABEL"));
                sc.setDefinition(rs.getString("vc.DEFINITION"));
                sc.setNotation(rs.getString("vc.NOTATION"));

                Integer dataElemId = rs.getInt("vce.DATAELEM_ID");

                for ( Map.Entry<String, Integer> entry : elementMap.entrySet()) {
                    Set <String> elementIdentifierSet = Util.getKeysByValue(elementMap, entry.getValue());
                    if(elementIdentifierSet.iterator().next().equals("sitecodes_CC_ISO2")){
                        sc.setCountryCode(rs.getString("vce.ELEMENT_VALUE"));
                    }
                    else if(elementIdentifierSet.iterator().next().equals("sitecodes_INITIAL_SITE_NAME")){
                        sc.setInitialSiteName(rs.getString("vce.ELEMENT_VALUE"));
                    }
                    else if(elementIdentifierSet.iterator().next().equals("sitecodes_STATUS")){
                        sc.setSiteCodeStatus(SiteCodeStatus.valueOf(rs.getString("vce.ELEMENT_VALUE")));
                    }
                    else if(elementIdentifierSet.iterator().next().equals("sitecodes_DATE_ALLOCATED")){
                        sc.setDateAllocated(rs.getTimestamp("vce.ELEMENT_VALUE"));
                    }
                    else if(elementIdentifierSet.iterator().next().equals("sitecodes_USER_ALLOCATED")){
                        sc.setUserAllocated(rs.getString("vce.ELEMENT_VALUE"));
                    }
                    else if(elementIdentifierSet.iterator().next().equals("sitecodes_USER_CREATED")){
                        sc.setUserCreated(rs.getString("vce.ELEMENT_VALUE"));
                    }
                    else if(elementIdentifierSet.iterator().next().equals("sitecodes_DATE_CREATED")){
                        sc.setDateCreated(rs.getTimestamp("vce.ELEMENT_VALUE"));
                    }
                    else if(elementIdentifierSet.iterator().next().equals("sitecodes_YEARS_DELETED")){
                        sc.setYearsDeleted(rs.getString("vce.ELEMENT_VALUE"));
                    }
                    else if(elementIdentifierSet.iterator().next().equals("sitecodes_YEARS_DISSAPEARED")){
                        sc.setYearsDisappeared(rs.getString("vce.ELEMENT_VALUE"));
                    }
                }
                return sc;
            }
        });

        String totalSql = "SELECT FOUND_ROWS()";
        int totalItems = getJdbcTemplate().queryForObject(totalSql,Integer.class);

        SiteCodeResult result = new SiteCodeResult(siteCodeList, totalItems, filter);

        return result;
    }

    /**
     * Returns SiteCode search SQL and also populates the parameters map.
     *
     * @param filter filtering
     * @param params params map
     * @return
     */
    private String getSiteCodesSql(SiteCodeFilter filter, Map<String, Object> params) {

        //TODO instead of T_SITE_CODE use the bound elements
        StringBuilder sql = new StringBuilder();
        sql.append("select SQL_CALC_FOUND_ROWS sc.VOCABULARY_CONCEPT_ID, sc.STATUS, sc.CC_ISO2, "
                + "sc.DATE_CREATED, sc.USER_CREATED, vc.VOCABULARY_CONCEPT_ID, vc.IDENTIFIER, vc.LABEL, "
                + "vc.DEFINITION, vc.NOTATION, sc.DATE_ALLOCATED, sc.USER_ALLOCATED, sc.INITIAL_SITE_NAME, "
                + "sc.YEARS_DELETED, sc.YEARS_DISAPPEARED ");
        sql.append("from T_SITE_CODE sc, VOCABULARY_CONCEPT vc where sc.VOCABULARY_CONCEPT_ID=vc.VOCABULARY_CONCEPT_ID ");

        if (StringUtils.isNotEmpty(filter.getSiteName())) {
            params.put("text", "%" + filter.getSiteName() + "%");
            sql.append("and vc.LABEL like :text ");
        }
        if (StringUtils.isNotEmpty(filter.getIdentifier())) {
            params.put("identifier", filter.getIdentifier());
            sql.append("and vc.IDENTIFIER like :identifier ");
        }
        if (StringUtils.isNotEmpty(filter.getUserAllocated())) {
            params.put("userAllocated", filter.getUserAllocated());
            sql.append("and sc.USER_ALLOCATED like :userAllocated ");
        }
        if (filter.getDateAllocated() != null) {
            params.put("dateAllocated", filter.getDateAllocated());
            sql.append("and sc.DATE_ALLOCATED = :dateAllocated ");
        }
        if (filter.getStatus() != null) {
            params.put("status", filter.getStatus().toString());
            sql.append("and sc.STATUS = :status ");
        } else if (filter.isAllocatedUsedStatuses()) {
            params.put("statuses", Arrays.asList(SiteCodeFilter.ALLOCATED_USED_STATUSES));
            sql.append("and sc.STATUS IN (:statuses) ");
        }
        if (filter.getCountryCode() != null) {
            params.put("countryCode", filter.getCountryCode());
            sql.append("and sc.CC_ISO2 = :countryCode ");
        }

        // sorting
        if (StringUtils.isNotEmpty(filter.getSortProperty())) {
            if (filter.getSortProperty().equals("identifier")) {
                sql.append("order by IDENTIFIER + 0");
            } else {
                sql.append("order by " + filter.getSortProperty());
            }
            if (SortOrderEnum.ASCENDING.equals(filter.getSortOrder())) {
                sql.append(" ASC ");
            } else {
                sql.append(" DESC ");
            }
        } else {
            sql.append("order by IDENTIFIER + 0 ");
        }
        if (filter.isUsePaging()) {
            sql.append("LIMIT ").append(filter.getOffset()).append(",").append(filter.getPageSize());
        }

        return sql.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertUserAndDateCreatedForSiteCodes(List<VocabularyConcept> vocabularyConcepts, String userName) {
        StringBuilder sql = new StringBuilder();
        sql.append("insert into VOCABULARY_CONCEPT_ELEMENT (VOCABULARY_CONCEPT_ID, DATAELEM_ID, ELEMENT_VALUE) ");
        sql.append("values (:vocabularyConceptId, :dataElemId, :elementValue)");

        Date dateCreated = new Date();
        @SuppressWarnings("unchecked")
        /*The size of the map will be the concepts' size * 2 because of the two bound elements*/
        Map<String, Object>[] batchValues = new HashMap[vocabularyConcepts.size()*2];

        //retrieve data element id for identifier sitecodes_DATE_CREATED and sitecodes_USER_CREATED
        //TODO fix variables below so they aren't hard coded
        String dateCreatedIdentifier = "sitecodes_DATE_CREATED";
        String userCreatedIdentifier = "sitecodes_USER_CREATED";
        int dateCreatedElementId = 0;
        int userCreatedElementId = 0;

        dateCreatedElementId = dataElementDao.getCommonDataElementId(dateCreatedIdentifier);
        userCreatedElementId = dataElementDao.getCommonDataElementId(userCreatedIdentifier);

        LOGGER.info(String.format("Data element id for identifier '%s' is #%s", dateCreatedIdentifier, dateCreatedElementId));
        LOGGER.info(String.format("Data element id for identifier '%s' is #%s", userCreatedIdentifier, userCreatedElementId));

        int batchValuesCounter = 0;
        /*A loop is performed in order to insert the username that reserved the site codes and the date*/
        for(int j=0; j < 2; j++) {

            for (int i = 0; i < vocabularyConcepts.size(); i++) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("vocabularyConceptId", vocabularyConcepts.get(i).getId());

                if (j == 0) {
                    params.put("dataElemId", dateCreatedElementId);
                    params.put("elementValue", dateCreated);
                } else {
                    params.put("dataElemId", userCreatedElementId);
                    params.put("elementValue", userName);
                }
                batchValues[batchValuesCounter] = params;
                batchValuesCounter++;
            }
        }
        getNamedParameterJdbcTemplate().batchUpdate(sql.toString(), batchValues);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void allocateSiteCodes(List<SiteCode> freeSiteCodes, String countryCode, String userName, String[] siteNames,
            Date allocationTime) {

            /* TODO
                insert value of bound elements:  sitecodes_CC_ISO2, sitecodes_INITIAL_SITE_NAME, sitecodes_STATUS, sitecodes_DATE_ALLOCATED, sitecodes_USER_ALLOCATED
            */

        StringBuilder sql = new StringBuilder();
        sql.append("update T_SITE_CODE set CC_ISO2 = :country, INITIAL_SITE_NAME = :siteName, STATUS = :status, "
                + "DATE_ALLOCATED = :dateAllocated, USER_ALLOCATED = :userAllocated ");
        sql.append("where VOCABULARY_CONCEPT_ID = :vocabularyConceptId");


        StringBuilder sqlForBoundElements = new StringBuilder();
        sqlForBoundElements.append("insert into VOCABULARY_CONCEPT_ELEMENT (VOCABULARY_CONCEPT_ID, DATAELEM_ID, ELEMENT_VALUE) ");
        sqlForBoundElements.append("values (:vocabularyConceptId, :dataElemId, :elementValue)");

        //TODO change it so it isn't hard coded
        List<String> elementIdentifiers = new ArrayList<String>();
        elementIdentifiers.add("sitecodes_CC_ISO2");
        elementIdentifiers.add("sitecodes_INITIAL_SITE_NAME");
        elementIdentifiers.add("sitecodes_STATUS");
        elementIdentifiers.add("sitecodes_DATE_ALLOCATED");
        elementIdentifiers.add("sitecodes_USER_ALLOCATED");

        Map<String, Integer> elementMap = dataElementDao.getMultipleCommonDataElementIds(elementIdentifiers);



        @SuppressWarnings("unchecked")
        Map<String, Object>[] batchValues = new HashMap[siteNames.length];
        int batchValuesCounter = 0;

        for ( Map.Entry<String, Integer> entry : elementMap.entrySet()) {
            for (int i = 0; i < freeSiteCodes.size(); i++) {
                Map<String, Object> paramsForBoundElements = new HashMap<String, Object>();
                paramsForBoundElements.put("vocabularyConceptId", freeSiteCodes.get(i).getId());
                paramsForBoundElements.put("dataElemId", entry.getValue());
                if(entry.getKey().equals("sitecodes_CC_ISO2")){
                    paramsForBoundElements.put("elementValue", countryCode);
                }
                else if(entry.getKey().equals("sitecodes_INITIAL_SITE_NAME")){
                    if (siteNames.length > i && siteNames[i] != null) {
                        paramsForBoundElements.put("elementValue",  siteNames[i]);
                    } else {
                        paramsForBoundElements.put("elementValue", "");
                    }
                }
                else if(entry.getKey().equals("sitecodes_STATUS")){
                    paramsForBoundElements.put("elementValue", SiteCodeStatus.ALLOCATED.name());
                }
                else if(entry.getKey().equals("sitecodes_DATE_ALLOCATED")){
                    paramsForBoundElements.put("elementValue", allocationTime);
                }
                else if(entry.getKey().equals("sitecodes_USER_ALLOCATED")){
                    paramsForBoundElements.put("elementValue", userName);
                }
                if(batchValues.length > i) {
                    batchValues[batchValuesCounter] = paramsForBoundElements;
                    batchValuesCounter++;
                }
            }
        }
        getNamedParameterJdbcTemplate().batchUpdate(sql.toString(), batchValues);








        //TODO query below can be updated to use vocabulary_concept_id ?
        // update place-holder value in concept label to <allocated>
        StringBuilder sqlForConcepts = new StringBuilder();
        sqlForConcepts.append("update VOCABULARY_CONCEPT set LABEL = :label where VOCABULARY_CONCEPT_ID IN "
                + " (select VOCABULARY_CONCEPT_ELEMENT.VOCABULARY_CONCEPT_ID from VOCABULARY_CONCEPT_ELEMENT "
                + "INNER JOIN DATAELEM on VOCABULARY_CONCEPT_ELEMENT.DATAELEM_ID = DATAELEM.DATAELEM_ID "
                + "WHERE VOCABULARY_CONCEPT_ELEMENT.DATAELEM_ID IN "
                + "(SELECT VOCABULARY_CONCEPT_ELEMENT.DATAELEM_ID FROM DATAELEM INNER JOIN VOCABULARY_CONCEPT_ELEMENT on VOCABULARY_CONCEPT_ELEMENT.DATAELEM_ID = DATAELEM.DATAELEM_ID "
                + "WHERE (IDENTIFIER='sitecodes_STATUS' AND ELEMENT_VALUE = :status) OR (IDENTIFIER='sitecodes_DATE_ALLOCATED' AND ELEMENT_VALUE = dateAllocated) "
                + "OR (IDENTIFIER='sitecodes_USER_ALLOCATED' AND ELEMENT_VALUE = userAllocated)))");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("status", SiteCodeStatus.ALLOCATED.name());
        parameters.put("dateAllocated", allocationTime);
        parameters.put("userAllocated", userName);
        parameters.put("label", "<" + SiteCodeStatus.ALLOCATED.name().toLowerCase() + ">");
        getNamedParameterJdbcTemplate().update(sqlForConcepts.toString(), parameters);
    }

    //TODO The following method will be kept as it is.
    /**
     * {@inheritDoc}
     */
    @Override
    public int getSiteCodeVocabularyFolderId() {

        StringBuilder sql = new StringBuilder();
        sql.append("select min(VOCABULARY_ID) from VOCABULARY where VOCABULARY_TYPE = :type");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("type", VocabularyType.SITE_CODE.name());

        return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params,Integer.class);
    }

    /**
     * {@inheritDoc}
     */
   /* @Override
    public int getFeeSiteCodeAmount() {
        StringBuilder sql = new StringBuilder();
        sql.append("select count(VOCABULARY_CONCEPT_ID) from T_SITE_CODE where STATUS = :status");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("status", SiteCodeStatus.AVAILABLE.name());

        return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params,Integer.class);
    }*/

    /**
     * {@inheritDoc}
     */
  /*  @Override
    public int getCountryUnusedAllocations(String countryCode, boolean withoutInitialName) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("countryCode", countryCode);
        params.put("status", SiteCodeStatus.ALLOCATED.name());

        StringBuilder sql = new StringBuilder();
        sql.append("select count(VOCABULARY_CONCEPT_ID) from T_SITE_CODE where STATUS = :status ");
        sql.append("and CC_ISO2 = :countryCode ");
        if (withoutInitialName) {
            sql.append("and (INITIAL_SITE_NAME is null or INITIAL_SITE_NAME = '') ");
        }

        return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params,Integer.class);
    }*/

    /**
     * {@inheritDoc}
     */
 /*   @Override
    public int getCountryUsedAllocations(String countryCode) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("countryCode", countryCode);
        params.put("statuses", Arrays.asList(SiteCodeFilter.ALLOCATED_USED_STATUSES));

        StringBuilder sql = new StringBuilder();
        sql.append("select count(VOCABULARY_CONCEPT_ID) from T_SITE_CODE where STATUS in (:statuses) ");
        sql.append("and CC_ISO2 = :countryCode");

        return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params,Integer.class);
    }
*/

    //TODO The following method will be kept as it is.
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean siteCodeFolderExists() {
        StringBuilder sql = new StringBuilder();
        sql.append("select count(VOCABULARY_ID) from VOCABULARY where VOCABULARY_TYPE = :type");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("type", VocabularyType.SITE_CODE.name());

        return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params,Integer.class) > 0;
    }

}
