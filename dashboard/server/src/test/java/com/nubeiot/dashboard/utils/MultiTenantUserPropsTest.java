package com.nubeiot.dashboard.utils;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.dashboard.MultiTenantUserProps;

public class MultiTenantUserPropsTest {

    @Test
    public void test() {
        String companyId = "ABC";
        MultiTenantUserProps multiTenant = MultiTenantUserProps.builder().companyId(companyId).build();
        Assert.assertEquals(multiTenant.getCompanyId(), companyId);
    }

}
