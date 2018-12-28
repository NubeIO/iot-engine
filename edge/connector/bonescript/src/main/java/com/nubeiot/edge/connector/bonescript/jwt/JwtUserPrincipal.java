package com.nubeiot.edge.connector.bonescript.jwt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.nubeiot.core.dto.JsonData;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class JwtUserPrincipal implements JsonData {

    @NonNull private String accessToken;
    private Boolean authorized;
    private Role role;
    private String username;
    @JsonIgnore @JsonSetter private String password;

}

