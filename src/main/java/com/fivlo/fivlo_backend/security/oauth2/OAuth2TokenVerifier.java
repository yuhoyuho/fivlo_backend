package com.fivlo.fivlo_backend.security.oauth2;

import com.fivlo.fivlo_backend.domain.user.entity.User;

public interface OAuth2TokenVerifier {

    User verifyAndGetOrCreate(String token);

    Boolean supports(String provider);
}
