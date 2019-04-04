package com.nubeiot.dashboard.providers;

import io.vertx.reactivex.ext.auth.oauth2.OAuth2Auth;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class RestOAuth2AuthProvider {

    private final OAuth2Auth oAuth2Auth;

}
