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

package org.wso2.carbon.identity.organization.mgt.core;

import org.wso2.carbon.identity.organization.mgt.core.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.mgt.core.model.BasicOrganization;
import org.wso2.carbon.identity.organization.mgt.core.model.Organization;
import org.wso2.carbon.identity.organization.mgt.core.model.OrganizationAdd;

import java.util.List;

/**
 * Organization manager service interface.
 */
public interface OrganizationManager {

    /**
     * @param organizationAdd
     * @return
     * @throws OrganizationManagementException
     */
    Organization addOrganization(OrganizationAdd organizationAdd)
            throws OrganizationManagementException;

    /**
     * @param organizationId
     * @return
     * @throws OrganizationManagementException
     */
    Organization getOrganization(String organizationId) throws OrganizationManagementException;

    /**
     * @param offset
     * @param limit
     * @param sortBy
     * @param sortOrder
     * @return
     * @throws OrganizationManagementException
     */
    List<BasicOrganization> getOrganizations(int offset, int limit, String sortBy, String sortOrder)
            throws OrganizationManagementException;

    /**
     * @param organizationName
     * @return
     * @throws OrganizationManagementException
     */
    boolean isOrganizationExistByName(String organizationName) throws OrganizationManagementException;

    /**
     * @param id
     * @return
     * @throws OrganizationManagementException
     */
    boolean isOrganizationExistById(String id) throws OrganizationManagementException;

    /**
     * @param organizationId
     * @param organizationAdd
     * @return
     * @throws OrganizationManagementException
     */
    Organization updateOrganization(String organizationId, OrganizationAdd organizationAdd)
            throws OrganizationManagementException;

    /**
     * @param organizationId
     * @throws OrganizationManagementException
     */
    void deleteOrganization(String organizationId) throws OrganizationManagementException;
}
