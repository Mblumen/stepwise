package de.hd.stepwise.helper.googlehealth;

import android.app.PendingIntent;
import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.auth.api.identity.AuthorizationClient;
import com.google.android.gms.auth.api.identity.AuthorizationRequest;
import com.google.android.gms.auth.api.identity.AuthorizationResult;
import com.google.android.gms.auth.api.identity.ClearTokenRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.RevokeAccessRequest;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Tasks;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class GoogleHealthAuthManager {

    public static final String ACTIVITY_SCOPE =
            "https://www.googleapis.com/auth/googlehealth.activity_and_fitness.readonly";

    private static final String PREFS_NAME = "google_health_auth";
    private static final String LEGACY_FITBIT_PREFS_NAME = "fitbit_secure_auth";
    private static final String KEY_CONNECTED = "connected";
    private static final String KEY_AUTHORIZATION_REQUIRED = "authorization_required";
    private static final String KEY_ACCOUNT_NAME = "account_name";
    private static final String KEY_PENDING_ACCOUNT_NAME = "pending_account_name";
    private static final long TOKEN_TIMEOUT_SECONDS = 30;

    private final Context appContext;
    private final SharedPreferences preferences;

    @Inject
    public GoogleHealthAuthManager(@ApplicationContext Context context) {
        appContext = context.getApplicationContext();
        preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Legacy Fitbit tokens cannot be used with Google Health and must not linger locally.
        appContext.getSharedPreferences(LEGACY_FITBIT_PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    public void authorize(String accountName, Consumer<PendingIntent> resolutionCallback,
                          Runnable successCallback,
                          Consumer<Exception> errorCallback) {
        preferences.edit().putString(KEY_PENDING_ACCOUNT_NAME, accountName).apply();
        client().authorize(createRequest(accountName))
                .addOnSuccessListener(result -> handleAuthorizationResult(
                        result, resolutionCallback, successCallback, errorCallback))
                .addOnFailureListener(errorCallback::accept);
    }

    public void completeAuthorization(Intent data, Runnable successCallback,
                                      Consumer<Exception> errorCallback) {
        try {
            AuthorizationResult result = client().getAuthorizationResultFromIntent(data);
            handleAuthorizationResult(result, pendingIntent -> errorCallback.accept(
                    new IllegalStateException("Authorization still requires user interaction")),
                    successCallback, errorCallback);
        } catch (Exception exception) {
            errorCallback.accept(exception);
        }
    }

    public String getAccessTokenSync() throws Exception {
        AuthorizationResult result = Tasks.await(
                client().authorize(createRequest()), TOKEN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        try {
            String accessToken = requireBackgroundAccessToken(
                    result.hasResolution(), result.getAccessToken());
            markConnected();
            return accessToken;
        } catch (GoogleHealthAuthorizationRequiredException exception) {
            markAuthorizationRequired();
            throw exception;
        }
    }

    public void clearAccessTokenSync(String accessToken) throws Exception {
        Tasks.await(client().clearToken(ClearTokenRequest.builder()
                        .setToken(accessToken)
                        .build()),
                TOKEN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    public void revoke(String accountName, Runnable successCallback,
                       Consumer<Exception> errorCallback) {
        Account account = new Account(accountName, "com.google");
        RevokeAccessRequest request = RevokeAccessRequest.builder()
                .setAccount(account)
                .setScopes(requestedScopes())
                .build();
        client().revokeAccess(request)
                .addOnSuccessListener(unused -> {
                    clearLocalState();
                    successCallback.run();
                })
                .addOnFailureListener(errorCallback::accept);
    }

    public boolean isAuthorized() {
        return preferences.getBoolean(KEY_CONNECTED, false)
                && !preferences.getBoolean(KEY_AUTHORIZATION_REQUIRED, false);
    }

    public String getConnectedAccountName() {
        return preferences.getString(KEY_ACCOUNT_NAME, null);
    }

    public void markAuthorizationRequired() {
        preferences.edit().putBoolean(KEY_AUTHORIZATION_REQUIRED, true).apply();
    }

    static String requireBackgroundAccessToken(boolean hasResolution, String accessToken)
            throws GoogleHealthAuthorizationRequiredException {
        if (hasResolution || accessToken == null) {
            throw new GoogleHealthAuthorizationRequiredException();
        }
        return accessToken;
    }

    private void handleAuthorizationResult(AuthorizationResult result,
                                           Consumer<PendingIntent> resolutionCallback,
                                           Runnable successCallback,
                                           Consumer<Exception> errorCallback) {
        if (result.hasResolution()) {
            PendingIntent pendingIntent = result.getPendingIntent();
            if (pendingIntent == null) {
                errorCallback.accept(new IllegalStateException(
                        "Authorization requires a missing resolution"));
                return;
            }
            resolutionCallback.accept(pendingIntent);
            return;
        }
        if (result.getAccessToken() == null) {
            errorCallback.accept(new IllegalStateException(
                    "Google authorization returned no access token"));
            return;
        }
        markConnected();
        successCallback.run();
    }

    private AuthorizationRequest createRequest() {
        return createRequest(getConnectedAccountName());
    }

    private AuthorizationRequest createRequest(String accountName) {
        AuthorizationRequest.Builder builder = AuthorizationRequest.builder()
                .setRequestedScopes(requestedScopes());
        if (accountName != null && !accountName.isBlank()) {
            builder.setAccount(new Account(accountName, "com.google"));
        }
        return builder.build();
    }

    private List<Scope> requestedScopes() {
        return List.of(new Scope(ACTIVITY_SCOPE));
    }

    private AuthorizationClient client() {
        return Identity.getAuthorizationClient(appContext);
    }

    private void markConnected() {
        SharedPreferences.Editor editor = preferences.edit()
                .putBoolean(KEY_CONNECTED, true)
                .putBoolean(KEY_AUTHORIZATION_REQUIRED, false);
        String pendingAccountName = preferences.getString(KEY_PENDING_ACCOUNT_NAME, null);
        if (pendingAccountName != null && !pendingAccountName.isBlank()) {
            editor.putString(KEY_ACCOUNT_NAME, pendingAccountName)
                    .remove(KEY_PENDING_ACCOUNT_NAME);
        }
        editor.apply();
    }

    private void clearLocalState() {
        preferences.edit().clear().apply();
    }
}
