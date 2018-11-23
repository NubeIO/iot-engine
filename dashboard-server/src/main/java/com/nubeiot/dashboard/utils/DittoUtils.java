package com.nubeiot.dashboard.utils;

import java.util.List;

import com.nubeiot.dashboard.Role;

import io.vertx.core.json.JsonObject;

public class DittoUtils {
    public static JsonObject createPolicy(List<JsonObject> jsonObjectList) {
        JsonObject policy = new JsonObject(defaultPolicy);
        JsonObject superAdminSubjects = new JsonObject();
        JsonObject adminSubjects = new JsonObject();

        for (JsonObject jsonObject : jsonObjectList) {
            if (jsonObject.getString("role").equals(Role.SUPER_ADMIN.toString())) {
                superAdminSubjects.put("nginx:" + jsonObject.getString("username"), new JsonObject().put("type", "super_admin"));
            } else {
                adminSubjects.put("nginx:" + jsonObject.getString("username"), new JsonObject().put("type", "admin"));
            }
        }

        policy.getJsonObject("entries").getJsonObject("super_admin").put("subjects", superAdminSubjects);
        policy.getJsonObject("entries").getJsonObject("admin").put("subjects", adminSubjects);
        System.out.println("Policy ::: " + policy);
        return policy;
    }

    private static String defaultPolicy = "{\n" +
        "  \"entries\": {\n" +
        "    \"super_admin\": {\n" +
        "      \"subjects\": {},\n" +
        "      \"resources\": {\n" +
        "        \"thing:/\": {\n" +
        "          \"grant\": [\n" +
        "            \"READ\",\n" +
        "            \"WRITE\"\n" +
        "          ],\n" +
        "          \"revoke\": []\n" +
        "        },\n" +
        "        \"policy:/\": {\n" +
        "          \"grant\": [\n" +
        "            \"READ\",\n" +
        "            \"WRITE\"\n" +
        "          ],\n" +
        "          \"revoke\": []\n" +
        "        },\n" +
        "        \"message:/\": {\n" +
        "          \"grant\": [\n" +
        "            \"READ\",\n" +
        "            \"WRITE\"\n" +
        "          ],\n" +
        "          \"revoke\": []\n" +
        "        }\n" +
        "      }\n" +
        "    },\n" +
        "    \"admin\": {\n" +
        "      \"subjects\": {},\n" +
        "      \"resources\": {\n" +
        "        \"thing:/\": {\n" +
        "          \"grant\": [\n" +
        "            \"READ\",\n" +
        "            \"WRITE\"\n" +
        "          ],\n" +
        "          \"revoke\": []\n" +
        "        },\n" +
        "        \"policy:/\": {\n" +
        "          \"grant\": [\n" +
        "            \"READ\",\n" +
        "            \"WRITE\"\n" +
        "          ],\n" +
        "          \"revoke\": []\n" +
        "        }\n" +
        "      }\n" +
        "    },\n" +
        "    \"manager\": {\n" +
        "      \"subjects\": {},\n" +
        "      \"resources\": {\n" +
        "        \"thing:/\": {\n" +
        "          \"grant\": [\n" +
        "            \"READ\",\n" +
        "            \"WRITE\"\n" +
        "          ],\n" +
        "          \"revoke\": []\n" +
        "        },\n" +
        "        \"policy:/entries/user/subjects/\": {\n" +
        "          \"grant\": [\n" +
        "            \"READ\",\n" +
        "            \"WRITE\"\n" +
        "          ],\n" +
        "          \"revoke\": []\n" +
        "        }\n" +
        "      }\n" +
        "    },\n" +
        "    \"user\": {\n" +
        "      \"subjects\": {},\n" +
        "      \"resources\": {\n" +
        "        \"thing:/\": {\n" +
        "          \"grant\": [\n" +
        "            \"READ\"\n" +
        "          ],\n" +
        "          \"revoke\": []\n" +
        "        }\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}";
}
