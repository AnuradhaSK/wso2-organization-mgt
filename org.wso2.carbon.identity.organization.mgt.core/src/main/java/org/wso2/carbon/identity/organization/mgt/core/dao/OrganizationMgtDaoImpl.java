/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.organization.mgt.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.organization.mgt.core.exception.OrganizationManagementClientException;
import org.wso2.carbon.identity.organization.mgt.core.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.mgt.core.exception.PrimitiveConditionValidationException;
import org.wso2.carbon.identity.organization.mgt.core.model.Attribute;
import org.wso2.carbon.identity.organization.mgt.core.model.Operation;
import org.wso2.carbon.identity.organization.mgt.core.model.Organization;
import org.wso2.carbon.identity.organization.mgt.core.model.OrganizationSearchBean;
import org.wso2.carbon.identity.organization.mgt.core.model.UserStoreConfig;
import org.wso2.carbon.identity.organization.mgt.core.search.Condition;
import org.wso2.carbon.identity.organization.mgt.core.search.PlaceholderSQL;
import org.wso2.carbon.identity.organization.mgt.core.search.PrimitiveConditionValidator;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.DN;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.DN_PLACE_HOLDER;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.ErrorMessages.ERROR_CODE_CHECK_ATTRIBUTE_EXIST_ERROR;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.ErrorMessages.ERROR_CODE_CHECK_ORGANIZATION_EXIST_BY_ID_ERROR;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.ErrorMessages.ERROR_CODE_CHECK_ORGANIZATION_EXIST_BY_NAME_ERROR;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.ErrorMessages.ERROR_CODE_INVALID_ORGANIZATION_GET_REQUEST;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_ADD_ERROR;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_DELETE_ERROR;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_GET_BY_ID_ERROR;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_GET_CHILDREN_ERROR;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_GET_CONFIGS_ERROR;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_GET_ERROR;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_GET_ID_BY_NAME_ERROR;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_PATCH_ERROR;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.ErrorMessages.ERROR_CODE_SQL_QUERY_LIMIT_EXCEEDED;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.PATCH_OP_ADD;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.PATCH_OP_REPLACE;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.PATCH_PATH_ORG_ACTIVE;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.PATCH_PATH_ORG_ATTRIBUTES;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.PATCH_PATH_ORG_DESCRIPTION;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.PATCH_PATH_ORG_NAME;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.PATCH_PATH_ORG_PARENT_ID;
import static org.wso2.carbon.identity.organization.mgt.core.constant.OrganizationMgtConstants.RDN;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.CHECK_ATTRIBUTE_EXIST_BY_KEY;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.CHECK_ORGANIZATION_EXIST_BY_ID;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.CHECK_ORGANIZATION_EXIST_BY_NAME;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.CHECK_ORG_HAS_ATTRIBUTES;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.COUNT_COLUMN_NAME;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.DELETE_ORGANIZATION_BY_ID;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.FIND_CHILD_ORG_IDS;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.GET_ALL_ORGANIZATION_IDS;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.GET_ORGANIZATIONS_BY_IDS;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.GET_ORGANIZATION_ID_BY_NAME;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.GET_USER_STORE_CONFIGS_BY_ORG_ID;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.GET_ORGANIZATION_BY_ID;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.INSERT_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.INSERT_ATTRIBUTES;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.INSERT_ATTRIBUTES_CONCLUDE;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.INSERT_ORGANIZATION;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.INSERT_OR_UPDATE_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.INSERT_OR_UPDATE_USER_STORE_CONFIG;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.MAX_QUERY_LENGTH_IN_BYTES_SQL;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.ORDER_BY;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.PAGINATION;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.PATCH_ORGANIZATION;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.PATCH_ORGANIZATION_CONCLUDE;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.PATCH_USER_STORE_CONFIG;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.REMOVE_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.UPDATE_HAS_ATTRIBUTES_FIELD;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.VIEW_ACTIVE;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.VIEW_ATTR_ID;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.VIEW_ATTR_KEY;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.VIEW_ATTR_VALUE;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.VIEW_CONFIG_ID;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.VIEW_CONFIG_KEY;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.VIEW_CONFIG_VALUE;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.VIEW_CREATED_TIME;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.VIEW_DESCRIPTION;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.VIEW_HAS_ATTRIBUTES;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.VIEW_ID;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.VIEW_LAST_MODIFIED;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.VIEW_NAME;
import static org.wso2.carbon.identity.organization.mgt.core.constant.SQLConstants.VIEW_PARENT_ID;
import static org.wso2.carbon.identity.organization.mgt.core.util.Utils.generateUniqueID;
import static org.wso2.carbon.identity.organization.mgt.core.util.Utils.getMaximumQueryLengthInBytes;
import static org.wso2.carbon.identity.organization.mgt.core.util.Utils.getNewTemplate;
import static org.wso2.carbon.identity.organization.mgt.core.util.Utils.handleClientException;
import static org.wso2.carbon.identity.organization.mgt.core.util.Utils.handleServerException;

public class OrganizationMgtDaoImpl implements OrganizationMgtDao {

    private static final Log log = LogFactory.getLog(OrganizationMgtDaoImpl.class);
    private static final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(UTC));

    @Override
    public void addOrganization(int tenantId, Organization organization) throws OrganizationManagementException {

        Timestamp currentTime = new java.sql.Timestamp(new Date().getTime());
        JdbcTemplate jdbcTemplate = getNewTemplate();
        try {
            jdbcTemplate.executeInsert(INSERT_ORGANIZATION,
                    preparedStatement -> {
                        int parameterIndex = 1;
                        preparedStatement.setString(parameterIndex, organization.getId());
                        preparedStatement.setInt(++parameterIndex, organization.getTenantId());
                        preparedStatement.setString(++parameterIndex, organization.getName());
                        preparedStatement.setString(++parameterIndex, organization.getDescription());
                        preparedStatement.setTimestamp(++parameterIndex, currentTime, calendar);
                        preparedStatement.setTimestamp(++parameterIndex, currentTime, calendar);
                        preparedStatement.setInt(++parameterIndex, organization.hasAttributes() ? 1 : 0);
                        preparedStatement.setInt(++parameterIndex, organization.isActive() ? 1 : 0);
                        preparedStatement.setString(++parameterIndex, organization.getParentId());
                    },
                    organization,
                    false
            );
            if (organization.hasAttributes()) {
                insertOrganizationAttributes(jdbcTemplate, organization);
            }
            insertOrUpdateUserStoreConfigs(jdbcTemplate, organization);
            organization.setCreated(currentTime.toInstant().toString());
            organization.setLastModified(currentTime.toInstant().toString());
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ORGANIZATION_ADD_ERROR, "Organization name " + organization.getName()
                    + ", Tenant Id " + organization.getTenantId(), e);
        }
    }

    @Override
    public List<Organization> getOrganizations(Condition condition, int tenantId, int offset, int limit,
                                               String sortBy, String sortOrder) throws OrganizationManagementException {

        PlaceholderSQL placeholderSQL = buildQuery(condition, offset, limit, sortBy, sortOrder);
        // Get organization IDs
        JdbcTemplate jdbcTemplate = getNewTemplate();
        List<String> orgIds;
        List<Organization> organizations = new ArrayList<>();
        try {
            orgIds = jdbcTemplate.executeQuery(placeholderSQL.getQuery(),
                    (resultSet, rowNumber) ->
                            resultSet.getString(VIEW_ID),
                    preparedStatement -> {
                        int parameterIndex = 0;
                        // Populate tenant ID
                        preparedStatement.setInt(++parameterIndex, tenantId);
                        // Populate generated conditions if any
                        for (int count = 0; placeholderSQL.getData() != null && count < placeholderSQL.getData().size(); count++) {
                            if (placeholderSQL.getData().get(count).getClass().equals(Integer.class)) {
                                preparedStatement.setInt(++parameterIndex, (Integer) placeholderSQL.getData().get(count));
                            } else if (placeholderSQL.getData().get(count).getClass().equals(Boolean.class)) {
                                int bool = ((Boolean) (placeholderSQL.getData().get(count))) ? 1 : 0;
                                preparedStatement.setInt(++parameterIndex, bool);
                            } else {
                                preparedStatement.setString(++parameterIndex, (String) placeholderSQL.getData().get(count));
                            }
                        }
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ORGANIZATION_GET_ERROR,
                    "Error while retrieving organization IDs.", e);
        }
        if (orgIds.isEmpty()) {
            return organizations;
        }

        // Get organizations by IDs
        String query = GET_ORGANIZATIONS_BY_IDS;
        StringJoiner sj = new StringJoiner(",");
        for (String id : orgIds) {
            sj.add("'" + id + "'");
        }
        // Can not perform this in a prepared statement due to character escaping.
        // This query only expects a list of organization IDs(server generated) to be retrieved. Hence, no security vulnerability.
        query = query.replace("?", sj.toString());
        validateQueryLength(query);
        try {
            organizations = jdbcTemplate.executeQuery(query,
                    (resultSet, rowNumber) -> {
                        Organization organization = new Organization();
                        organization.setId(resultSet.getString(VIEW_ID));
                        organization.setName(resultSet.getString(VIEW_NAME));
                        organization.setDescription(resultSet.getString(VIEW_DESCRIPTION));
                        organization.setParentId(resultSet.getString(VIEW_PARENT_ID));
                        organization.setActive(resultSet.getInt(VIEW_ACTIVE) == 1 ? true : false);
                        organization.setLastModified(resultSet.getTimestamp(VIEW_LAST_MODIFIED, calendar).toString());
                        organization.setCreated(resultSet.getTimestamp(VIEW_CREATED_TIME, calendar).toString());
                        return organization;
                    });
            // When sorting is required, organization IDs were fetched sorted from the DB. But the collected organizations may not.
            // Therefore, sort the organization as per the order of their IDs.
            return sortBy != null ? sortCollectedOrganizations(organizations, orgIds) : organizations;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ORGANIZATION_GET_ERROR,
                    "Error while constructing organizations by IDs", e);
        }
    }

    @Override
    public Organization getOrganization(int tenantId, String organizationId) throws OrganizationManagementException {

        JdbcTemplate jdbcTemplate = getNewTemplate();
        List<OrganizationRowDataCollector> organizationRowDataCollectors;
        try {
            organizationRowDataCollectors = jdbcTemplate.executeQuery(GET_ORGANIZATION_BY_ID,
                    (resultSet, rowNumber) -> {
                        OrganizationRowDataCollector collector = new OrganizationRowDataCollector();
                        collector.setId(organizationId);
                        collector.setName(resultSet.getString(VIEW_NAME));
                        collector.setDescription(resultSet.getString(VIEW_DESCRIPTION));
                        collector.setParentId(resultSet.getString(VIEW_PARENT_ID));
                        collector.setActive(resultSet.getInt(VIEW_ACTIVE) == 1 ? true : false);
                        collector.setLastModified(resultSet.getTimestamp(VIEW_LAST_MODIFIED, calendar));
                        collector.setCreated(resultSet.getTimestamp(VIEW_CREATED_TIME, calendar));
                        collector.setHasAttributes(resultSet.getInt(VIEW_HAS_ATTRIBUTES) == 1 ? true : false);
                        collector.setAttributeId(resultSet.getString(VIEW_ATTR_ID));
                        collector.setAttributeKey(resultSet.getString(VIEW_ATTR_KEY));
                        collector.setAttributeValue(resultSet.getString(VIEW_ATTR_VALUE));
                        return collector;
                    },
                    preparedStatement -> {
                        int parameterIndex = 0;
                        preparedStatement.setInt(++parameterIndex, tenantId);
                        preparedStatement.setString(++parameterIndex, organizationId);
                    }
            );
            return (organizationRowDataCollectors == null || organizationRowDataCollectors.size() == 0) ?
                    null : buildOrganizationFromRawData(organizationRowDataCollectors);
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ORGANIZATION_GET_BY_ID_ERROR, organizationId, e);
        }
    }

    @Override
    public void deleteOrganization(int tenantId, String organizationId) throws OrganizationManagementException {

        JdbcTemplate jdbcTemplate = getNewTemplate();
        try {
            // Delete organization from IDN_ORG table and cascade the deletion to other two tables
            jdbcTemplate.executeUpdate(DELETE_ORGANIZATION_BY_ID,
                    preparedStatement -> {
                        int parameterIndex = 0;
                        preparedStatement.setInt(++parameterIndex, tenantId);
                        preparedStatement.setString(++parameterIndex, organizationId);
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ORGANIZATION_DELETE_ERROR, "Organization Id " + organizationId, e);
        }
    }

    @Override
    public List<String> getChildOrganizationIds(String organizationId) throws OrganizationManagementException {

        JdbcTemplate jdbcTemplate = getNewTemplate();
        try {
            List<String> childOrganizationIds = jdbcTemplate.executeQuery(FIND_CHILD_ORG_IDS,
                    (resultSet, rowNumber) -> resultSet.getString(VIEW_ID),
                    preparedStatement -> {
                        preparedStatement.setString(1, organizationId);
                    });
            return childOrganizationIds;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ORGANIZATION_GET_CHILDREN_ERROR, "Organization Id " + organizationId, e);
        }
    }

    @Override
    public Map<String, UserStoreConfig> getUserStoreConfigsByOrgId(int tenantId, String organizationId) throws OrganizationManagementException {

        JdbcTemplate jdbcTemplate = getNewTemplate();
        try {
            List<UserStoreConfig> userStoreConfigs = jdbcTemplate.executeQuery(GET_USER_STORE_CONFIGS_BY_ORG_ID,
                    (resultSet, rowNumber) -> {
                        UserStoreConfig config = new UserStoreConfig();
                        config.setId(resultSet.getString(VIEW_CONFIG_ID));
                        config.setKey(resultSet.getString(VIEW_CONFIG_KEY));
                        config.setValue(resultSet.getString(VIEW_CONFIG_VALUE));
                        return config;
                    },
                    preparedStatement -> {
                        int parameterIndex = 0;
                        preparedStatement.setInt(++parameterIndex, tenantId);
                        preparedStatement.setString(++parameterIndex, organizationId);
                    });
            return userStoreConfigs.stream().collect(Collectors.toMap(UserStoreConfig::getKey, config -> config));

        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ORGANIZATION_GET_CONFIGS_ERROR, organizationId, e);
        }
    }

    @Override
    public boolean isOrganizationExistByName(int tenantId, String name) throws OrganizationManagementException {

        JdbcTemplate jdbcTemplate = getNewTemplate();
        try {
            int orgCount = jdbcTemplate.fetchSingleRecord(CHECK_ORGANIZATION_EXIST_BY_NAME,
                    (resultSet, rowNumber) ->
                            resultSet.getInt(COUNT_COLUMN_NAME),
                    preparedStatement -> {
                        int parameterIndex = 0;
                        preparedStatement.setInt(++parameterIndex, tenantId);
                        preparedStatement.setString(++parameterIndex, name);
                    });
            return orgCount > 0;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_CHECK_ORGANIZATION_EXIST_BY_NAME_ERROR,
                    "Organization name " + name + ", Tenant id " + tenantId, e);
        }
    }

    @Override
    public boolean isOrganizationExistById(int tenantId, String id) throws OrganizationManagementException {

        JdbcTemplate jdbcTemplate = getNewTemplate();
        try {
            int orgCount = jdbcTemplate.fetchSingleRecord(CHECK_ORGANIZATION_EXIST_BY_ID,
                    (resultSet, rowNumber) ->
                            resultSet.getInt(COUNT_COLUMN_NAME),
                    preparedStatement -> {
                        int parameterIndex = 0;
                        preparedStatement.setInt(++parameterIndex, tenantId);
                        preparedStatement.setString(++parameterIndex, id);
                    }
            );
            return orgCount > 0;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_CHECK_ORGANIZATION_EXIST_BY_ID_ERROR,
                    "Organization ID " + id + ", Tenant id " + tenantId, e);
        }
    }

    @Override
    public void patchOrganization(String organizationId, Operation operation)
            throws OrganizationManagementException {

        String path = operation.getPath();
        JdbcTemplate jdbcTemplate = getNewTemplate();
        if (path.startsWith(PATCH_PATH_ORG_ATTRIBUTES)) {
            // Patch an attribute
            String attributeKey = path.replace(PATCH_PATH_ORG_ATTRIBUTES, "").trim();
            operation.setPath(attributeKey);
            try {
                patchAttribute(jdbcTemplate, organizationId, operation);
            } catch (DataAccessException e) {
                throw handleServerException(ERROR_CODE_ORGANIZATION_PATCH_ERROR,
                        "Error while patching attribute : " + attributeKey +
                                ", value : " + operation.getValue() + ", op : " + operation.getOp() + ", org : " + organizationId, e);
            }
        } else {
            // Updating a primary field
            StringBuilder sb = new StringBuilder();
            sb.append(PATCH_ORGANIZATION);
            if (path.equals(PATCH_PATH_ORG_NAME)) {
                sb.append(VIEW_NAME);
            } else if (path.equals(PATCH_PATH_ORG_DESCRIPTION)) {
                sb.append(VIEW_DESCRIPTION);
            } else if (path.equals(PATCH_PATH_ORG_ACTIVE)) {
                sb.append(VIEW_ACTIVE);
            } else if (path.equals(PATCH_PATH_ORG_PARENT_ID)) {
                sb.append(VIEW_PARENT_ID);
            }
            sb.append(PATCH_ORGANIZATION_CONCLUDE);
            String query = sb.toString();
            if (log.isDebugEnabled()) {
                log.debug("Organization patch query : " + query);
            }
            try {
                jdbcTemplate.executeUpdate(query,
                        preparedStatement -> {
                            int parameterIndex = 0;
                            if (path.equals(PATCH_PATH_ORG_ACTIVE)) {
                                preparedStatement.setInt(++parameterIndex, operation.getValue().equals("true") ? 1 : 0);
                            } else {
                                preparedStatement.setString(++parameterIndex, operation.getValue());
                            }
                            preparedStatement.setString(++parameterIndex, organizationId);
                        });
            } catch (DataAccessException e) {
                throw handleServerException(ERROR_CODE_ORGANIZATION_PATCH_ERROR,
                        "Error while updating the primary field : " + path +
                                ", value : " + operation.getValue() + ", org : " + organizationId, e);
            }
        }
    }

    @Override
    public String getOrganizationIdByName(int tenantId, String organizationName)
            throws OrganizationManagementException {

        JdbcTemplate jdbcTemplate = getNewTemplate();
        try {
            return jdbcTemplate.fetchSingleRecord(GET_ORGANIZATION_ID_BY_NAME,
                    (resultSet, rowNumber) ->
                            resultSet.getString(VIEW_ID),
                    preparedStatement -> {
                        int parameterIndex = 0;
                        preparedStatement.setInt(++parameterIndex, tenantId);
                        preparedStatement.setString(++parameterIndex, organizationName);
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ORGANIZATION_GET_ID_BY_NAME_ERROR,
                    "Organization name " + organizationName + ", tenant id " + tenantId, e);
        }
    }

    @Override
    public void patchUserStoreConfigs(String organizationId, Operation operation)
            throws OrganizationManagementException {

        JdbcTemplate jdbcTemplate = getNewTemplate();
        if (RDN.equals(operation.getPath())) {
            // Set both RDN and DN appropriately
            Map<String, UserStoreConfig> userStoreConfigs = getUserStoreConfigsByOrgId(
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(),
                    organizationId);
            String dn = userStoreConfigs.get(DN).getValue();
            dn = dn.replace(String.format(DN_PLACE_HOLDER, userStoreConfigs.get(RDN).getValue()),
                    String.format(DN_PLACE_HOLDER, operation.getValue()));
            if (log.isDebugEnabled()) {
                log.debug("New DN of the organization ID : " + organizationId + " is : " + dn);
            }
            try {
                updateUserStoreConfig(jdbcTemplate, organizationId, RDN, operation.getValue());
                updateUserStoreConfig(jdbcTemplate, organizationId, DN, dn);
            } catch (DataAccessException e) {
                throw handleServerException(ERROR_CODE_ORGANIZATION_PATCH_ERROR,
                        "Error while updating user store configs for the organization ID : " + organizationId, e);
            }
        }
    }

    private void updateUserStoreConfig(JdbcTemplate template, String organizationId, String key, String value)
            throws DataAccessException {

        template.executeUpdate(PATCH_USER_STORE_CONFIG,
                preparedStatement -> {
                    int parameterIndex = 0;
                    preparedStatement.setString(++parameterIndex, value);
                    preparedStatement.setString(++parameterIndex, organizationId);
                    preparedStatement.setString(++parameterIndex, key);
                });
    }

    @Override
    public boolean isAttributeExistByKey(int tenantId, String organizationId, String attributeKey)
            throws OrganizationManagementException {

        JdbcTemplate jdbcTemplate = getNewTemplate();
        try {
            int attrCount = jdbcTemplate.fetchSingleRecord(CHECK_ATTRIBUTE_EXIST_BY_KEY,
                    (resultSet, rowNumber) ->
                            resultSet.getInt(COUNT_COLUMN_NAME),
                    preparedStatement -> {
                        int parameterIndex = 0;
                        preparedStatement.setInt(++parameterIndex, tenantId);
                        preparedStatement.setString(++parameterIndex, organizationId);
                        preparedStatement.setString(++parameterIndex, attributeKey);
                    });
            return attrCount > 0;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_CHECK_ATTRIBUTE_EXIST_ERROR,
                    "Error while checking if the the Attribute key exist : " + attributeKey, e);
        }
    }

    private void patchAttribute(JdbcTemplate template, String organizationId, Operation operation)
            throws DataAccessException, OrganizationManagementException {

        String attributeKey = operation.getPath().replace(PATCH_PATH_ORG_ATTRIBUTES, "").trim();
        // Insert or update attribute
        if (operation.getOp().equals(PATCH_OP_ADD) || operation.getOp().equals(PATCH_OP_REPLACE)) {
            template.executeInsert(INSERT_OR_UPDATE_ATTRIBUTE,
                    preparedStatement -> {
                        int parameterIndex = 0;
                        // On update, unique ID and Org ID will not be updated
                        preparedStatement.setString(++parameterIndex, generateUniqueID());
                        preparedStatement.setString(++parameterIndex, organizationId);
                        preparedStatement.setString(++parameterIndex, attributeKey);
                        preparedStatement.setString(++parameterIndex, operation.getValue());
                    },
                    new Attribute(),
                    false
            );
        } else {
            // Remove attribute
            template.executeUpdate(REMOVE_ATTRIBUTE,
                    preparedStatement -> {
                        int parameterIndex = 0;
                        preparedStatement.setString(++parameterIndex, organizationId);
                        preparedStatement.setString(++parameterIndex, operation.getPath());
                    }
            );
        }
        validateHasAttributesField(template, organizationId);
    }

    private void validateHasAttributesField(JdbcTemplate template, String organizationId)
            throws OrganizationManagementException {
        int attrCount;
        try {
            attrCount = template.fetchSingleRecord(CHECK_ORG_HAS_ATTRIBUTES,
                    (resultSet, rowNumber) ->
                            resultSet.getInt(COUNT_COLUMN_NAME),
                    preparedStatement -> {
                        int parameterIndex = 0;
                        preparedStatement.setString(++parameterIndex, organizationId);
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_CHECK_ATTRIBUTE_EXIST_ERROR,
                    "Error while checking if the organization has any attributes for the organization Id " + organizationId, e);
        }
        try {
            template.executeUpdate(UPDATE_HAS_ATTRIBUTES_FIELD,
                    preparedStatement -> {
                        int parameterIndex = 0;
                        preparedStatement.setInt(++parameterIndex, attrCount > 0 ? 1 : 0);
                        preparedStatement.setString(++parameterIndex, organizationId);
                    }
            );
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_CHECK_ATTRIBUTE_EXIST_ERROR,
                    "Error while updating HAS_ATTRIBUTES field of the organization Id " + organizationId, e);
        }
    }


    private void insertOrganizationAttributes(JdbcTemplate template, Organization organization)
            throws DataAccessException, OrganizationManagementClientException {

        String query = buildQueryForAttributes(organization);
        template.executeInsert(query,
                preparedStatement -> {
                    int parameterIndex = 0;
                    for (Map.Entry<String, Attribute> entry : organization.getAttributes().entrySet()) {
                        preparedStatement.setString(++parameterIndex, generateUniqueID());
                        preparedStatement.setString(++parameterIndex, organization.getId());
                        preparedStatement.setString(++parameterIndex, entry.getValue().getKey());
                        preparedStatement.setString(++parameterIndex, entry.getValue().getValue());
                    }
                },
                organization,
                false
        );
    }

    private void insertOrUpdateUserStoreConfigs(JdbcTemplate template, Organization organization)
            throws DataAccessException {

        for (Map.Entry<String, UserStoreConfig> entry : organization.getUserStoreConfigs().entrySet()) {
            template.executeInsert(INSERT_OR_UPDATE_USER_STORE_CONFIG,
                    preparedStatement -> {
                        int parameterIndex = 0;
                        // On update, unique ID and Org ID will not be updated
                        preparedStatement.setString(++parameterIndex, generateUniqueID());
                        preparedStatement.setString(++parameterIndex, organization.getId());
                        preparedStatement.setString(++parameterIndex, entry.getValue().getKey());
                        preparedStatement.setString(++parameterIndex, entry.getValue().getValue());
                    },
                    organization,
                    false
            );
        }
    }

    private String buildQueryForAttributes(Organization organization) throws OrganizationManagementClientException {

        StringBuilder sb = new StringBuilder();
        sb.append(INSERT_ATTRIBUTES);
        for (int i = 0; i < organization.getAttributes().size(); i++) {
            sb.append(INSERT_ATTRIBUTE);
        }
        // Multiple insertions require a 'SELECT 1 FROM Dual' at the end
        sb.append(INSERT_ATTRIBUTES_CONCLUDE);
        if (sb.toString().getBytes().length > getMaximumQueryLengthInBytes()) {
            if (log.isDebugEnabled()) {
                log.debug("Error building SQL query for the attribute insert. Number of attributes: " +
                        organization.getAttributes().size() + " exceeds the maximum query length: " +
                        MAX_QUERY_LENGTH_IN_BYTES_SQL);
            }
            throw handleClientException(ERROR_CODE_SQL_QUERY_LIMIT_EXCEEDED, "Too much attributes for the creation request. Try patch.");
        }
        return sb.toString();
    }

    private Organization buildOrganizationFromRawData(List<OrganizationRowDataCollector> organizationRowDataCollectors) {

        Organization organization = new Organization();
        organizationRowDataCollectors.forEach(collector -> {
            if (organization.getId() == null) {
                organization.setId(collector.getId());
                organization.setName(collector.getName());
                organization.setDescription(collector.getDescription());
                organization.setParentId(collector.getParentId());
                organization.setActive(collector.isActive());
                organization.setLastModified(collector.getLastModified().toString());
                organization.setCreated(collector.getCreated().toString());
                organization.setHasAttributes(collector.hasAttributes());
            }
            if (organization.hasAttributes() && collector.getAttributeKey() != null
                    && !organization.getAttributes().containsKey(collector.getAttributeKey())) {
                organization.getAttributes()
                        .put(collector.getAttributeKey(),
                                new Attribute(collector.getAttributeKey(), collector.getAttributeValue()));
            }
        });
        return organization;
    }

    private void validateQueryLength(String query) throws OrganizationManagementClientException {
        if (query.getBytes().length > getMaximumQueryLengthInBytes()) {
            if (log.isDebugEnabled()) {
                log.debug("Error building SQL query. Get organizations expression " +
                        "query length: " + query.length() + " exceeds the maximum limit: " +
                        MAX_QUERY_LENGTH_IN_BYTES_SQL);
            }
            throw handleClientException(ERROR_CODE_SQL_QUERY_LIMIT_EXCEEDED, "Query length exceeded the maximum limit.");
        }
    }

    private PlaceholderSQL buildQuery(Condition condition, int offset, int limit,
                                      String sortBy, String sortOrder) throws OrganizationManagementException {

        boolean paginationReq = offset > -1 && limit > 0;
        boolean searchReq = condition != null;
        // Ascending if not specified otherwise.
        sortOrder = sortOrder != null && "DESC".equals(sortOrder.trim().toUpperCase()) ? "DESC" : "ASC";
        boolean sortingReq = sortBy != null;

        PlaceholderSQL placeholderSQL;
        try {
            placeholderSQL = searchReq ? condition.buildQuery(
                    new PrimitiveConditionValidator(new OrganizationSearchBean())) : new PlaceholderSQL();
        } catch (PrimitiveConditionValidationException e) {
            log.error("Error passing the condition ", e);
            throw handleClientException(ERROR_CODE_INVALID_ORGANIZATION_GET_REQUEST, "Error passing the condition");
        }

        StringBuilder sb = new StringBuilder();
        // Base query
        sb.append(GET_ALL_ORGANIZATION_IDS);
        // Append generated search conditions
        if (searchReq) {
            sb.append(" AND ").append(placeholderSQL.getQuery());
        }
        if (sortingReq) {
            sb.append(String.format(ORDER_BY, sortBy, sortOrder));
        }
        if (paginationReq) {
            sb.append(String.format(PAGINATION, offset, limit));
        }
        placeholderSQL.setQuery(sb.toString());
        if (log.isDebugEnabled()) {
            log.debug("Built query : " + placeholderSQL.getQuery());
        }
        validateQueryLength(placeholderSQL.getQuery());
        return placeholderSQL;
    }

    private List<Organization> sortCollectedOrganizations(List<Organization> organizations,
                                                          List<String> organizationIds) {

        Map<String, Organization> organizationMap = organizations.stream()
                .collect(Collectors.toMap(Organization::getId, organization -> organization));
        organizations.clear();
        for (String id : organizationIds) {
            organizations.add(organizationMap.get(id));
        }
        return organizations;
    }
}
