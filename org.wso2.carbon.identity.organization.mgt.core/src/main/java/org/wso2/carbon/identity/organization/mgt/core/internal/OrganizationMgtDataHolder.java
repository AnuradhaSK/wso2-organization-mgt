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

import org.wso2.carbon.identity.organization.mgt.core.dao.OrganizationMgtDao;
import org.wso2.carbon.user.core.service.RealmService;

public class OrganizationMgtDataHolder {

    private static OrganizationMgtDataHolder orgMgtDataHolder = new OrganizationMgtDataHolder();
    private OrganizationMgtDao organizationMgtDao;
    private RealmService realmService;

    public static OrganizationMgtDataHolder getInstance() {

        return orgMgtDataHolder;
    }

    public OrganizationMgtDao getOrganizationMgtDao() {

        return organizationMgtDao;
    }

    public void setOrganizationMgtDao(OrganizationMgtDao organizationMgtDao) {

        this.organizationMgtDao = organizationMgtDao;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }
}
