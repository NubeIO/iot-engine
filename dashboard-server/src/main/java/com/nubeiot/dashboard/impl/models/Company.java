package com.nubeiot.dashboard.impl.models;

import com.nubeiot.core.common.Model;
import io.vertx.core.json.JsonObject;

public class Company extends Model {
    public String name;
    public String role; // ADMIN or MANAGER; defining the company level
    public String associated_company_id; // for pointing parent company; we will point this on company creation

    public Company(JsonObject body) {
        super(body);
    }
}
