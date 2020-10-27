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

package org.wso2.carbon.identity.organization.mgt.core.internal;

import org.wso2.carbon.identity.organization.mgt.core.dao.CacheBackedOrganizationMgtDAO;
import org.wso2.carbon.identity.organization.mgt.core.dao.OrganizationAuthorizationDao;
import org.wso2.carbon.identity.organization.mgt.core.dao.OrganizationMgtDao;
import org.wso2.carbon.identity.organization.mgt.core.model.OrganizationMgtRole;
import org.wso2.carbon.identity.organization.mgt.core.validator.AttributeValidator;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.HashMap;
import java.util.Map;

/**
 * Organization mgt data holder.
 */
public class OrganizationMgtDataHolder {

    private static final OrganizationMgtDataHolder orgMgtDataHolder = new OrganizationMgtDataHolder();
    private OrganizationAuthorizationDao organizationAuthDao;
    private OrganizationMgtDao organizationMgtDao;
    private CacheBackedOrganizationMgtDAO cacheBackedOrganizationMgtDAO;
    private RealmService realmService;
    private AttributeValidator attributeValidator;
    private Map<String, OrganizationMgtRole> organizationMgtRoles = new HashMap<>();

    public Map<String, OrganizationMgtRole> getOrganizationMgtRoles() {
        return organizationMgtRoles;
    }

    public void setOrganizationMgtRoles(Map<String, OrganizationMgtRole> organizationMgtRoles) {
        this.organizationMgtRoles = organizationMgtRoles;
    }

    public static OrganizationMgtDataHolder getInstance() {

        return orgMgtDataHolder;
    }

    public OrganizationMgtDao getOrganizationMgtDao() {

        return organizationMgtDao;
    }

    public void setOrganizationMgtDao(OrganizationMgtDao organizationMgtDao) {

        this.organizationMgtDao = organizationMgtDao;
    }

    public OrganizationAuthorizationDao getOrganizationAuthDao() {

        return organizationAuthDao;
    }

    public void setOrganizationAuthDao(OrganizationAuthorizationDao organizationAuthDao) {

        this.organizationAuthDao = organizationAuthDao;
    }

    public CacheBackedOrganizationMgtDAO getCacheBackedOrganizationMgtDAO() {

        return cacheBackedOrganizationMgtDAO;
    }

    public void setCacheBackedOrganizationMgtDAO(
            CacheBackedOrganizationMgtDAO cacheBackedOrganizationMgtDAO) {

        this.cacheBackedOrganizationMgtDAO = cacheBackedOrganizationMgtDAO;
    }

    public RealmService getRealmService() {

        return realmService;
    }

    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    public void setAttributeValidator(AttributeValidator attributeValidator) {

        this.attributeValidator = attributeValidator;
    }

    public AttributeValidator getAttributeValidator() {

        return this.attributeValidator;
    }
}
