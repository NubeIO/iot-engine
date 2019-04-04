package com.nubeiot.dashboard.props;

import org.junit.Assert;
import org.junit.Test;

public class UserPropsTest {

    @Test
    public void test() {
        String companyId = "ABC";
        UserProps multiTenant = UserProps.builder().companyId(companyId).build();
        Assert.assertEquals(multiTenant.getCompanyId(), companyId);
    }

}
