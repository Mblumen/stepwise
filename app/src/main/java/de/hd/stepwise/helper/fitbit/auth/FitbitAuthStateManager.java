package de.hd.stepwise.helper.fitbit.auth;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.TokenRequest;

import org.json.JSONException;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class FitbitAuthStateManager {

    private static final String PREFS_NAME = "fitbit_secure_auth";
    private static final String KEY_AUTH_STATE = "auth_state";
    private final SharedPreferences sharedPreferences;
    private final Context appContext;


    @Inject
    public FitbitAuthStateManager(@ApplicationContext Context context) {
        appContext = context.getApplicationContext();
        try {

            MasterKey masterKey = new MasterKey.Builder(appContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to initialize encrypted storage", e);
        }
    }

    public void exchangeAuthorizationCode(
            AuthorizationResponse response,
            AuthorizationException authException,
            AuthState.AuthStateAction callback
    ) {

        AuthState authState = new AuthState(response, authException);
        AuthorizationService authService = new AuthorizationService(appContext);
        TokenRequest tokenRequest = response.createTokenExchangeRequest();
        authService.performTokenRequest(tokenRequest,
                (tokenResponse, tokenException) -> {

                    if (tokenResponse != null) {

                        authState.update(tokenResponse, tokenException);
                        save(authState);
                        callback.execute(
                                tokenResponse.accessToken,
                                tokenResponse.idToken,
                                null
                        );

                    } else {
                        callback.execute(null, null, tokenException);
                    }
                });
    }

    public void save(AuthState authState) {
        sharedPreferences.edit()
                .putString(KEY_AUTH_STATE, authState.jsonSerializeString())
                .apply();
    }

    public AuthState read() {
        String stateJson = sharedPreferences.getString(KEY_AUTH_STATE, null);

        if (stateJson == null) {
            return null;
        }

        try {
            return AuthState.jsonDeserialize(stateJson);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void clear() {
        sharedPreferences.edit()
                .remove(KEY_AUTH_STATE)
                .apply();
    }

    public boolean isAuthorized() {
        AuthState state = read();
        return state != null && state.isAuthorized();
    }

    public void performActionWithFreshTokens(
            AuthState.AuthStateAction action
    ) {

        AuthState state = read();

        if (state == null) {
            //action.execute(null, null, new AuthorizationException(AuthorizationException.TYPE_OAUTH_TOKEN_ERROR, 500, "No stored AuthState"));
            return;
        }

        AuthorizationService authService = new AuthorizationService(appContext);
        state.performActionWithFreshTokens(authService, (accessToken, idToken, ex) -> {

                    if (ex == null) {
                        // Persist updated state after refresh
                        save(state);
                    }

                    action.execute(accessToken, idToken, ex);
                });
    }
}