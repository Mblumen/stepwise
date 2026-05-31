package de.hd.stepwise.helper.fitbit.auth;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import net.openid.appauth.*;

public class FitbitAuthHelper {

    private static final String CLIENT_ID = "23TZSD";
    public static final String REDIRECT_URI = "de.stepwise://oauth/callback";
    private static final Uri AUTH_URI = Uri.parse("https://www.fitbit.com/oauth2/authorize");
    private static final Uri TOKEN_URI = Uri.parse("https://api.fitbit.com/oauth2/token");
    public static final int RC_AUTH = 911;

    public Intent getFitbitAuthIntent(Activity activity) {
        AuthorizationServiceConfiguration config = new AuthorizationServiceConfiguration(AUTH_URI, TOKEN_URI);

        AuthorizationRequest request = new AuthorizationRequest.Builder(
                config,
                CLIENT_ID,
                ResponseTypeValues.CODE,
                Uri.parse(REDIRECT_URI)
        )
                .setScope("activity")
                .build();

        AuthorizationService authService = new AuthorizationService(activity);
        return authService.getAuthorizationRequestIntent(request);
    }
}