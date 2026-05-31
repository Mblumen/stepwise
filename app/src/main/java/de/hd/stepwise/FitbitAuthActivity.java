package de.hd.stepwise;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;

import de.hd.stepwise.helper.fitbit.auth.FitbitAuthHelper;

public class FitbitAuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("FitbitAuthActivity", "Handling authorization response...");
        AuthorizationResponse response = AuthorizationResponse.fromIntent(getIntent());
        AuthorizationException ex = AuthorizationException.fromIntent(getIntent());

        Log.d("FitbitAuthActivity", "Parsed response: " + response);
        Log.d("FitbitAuthActivity", "Parsed exception: " + ex);


        Uri uri = getIntent().getData();
        Log.d("FitbitAuthActivity", "Received URI: " + uri);
        if (uri != null && uri.toString().startsWith(FitbitAuthHelper.REDIRECT_URI)) {
            String code = uri.getQueryParameter("code");
            String state = uri.getQueryParameter("state");
            // Optional: check "error" param for OAuth error
            Log.d("FitbitAuthActivity", "Authorization code: " + code);
        }
            //if (code != null) {
                // Exchange the code for tokens
                //FitbitAuthHelper helper = new FitbitAuthHelper();
                /*helper.performTokenExchange(this, code, new FitbitAuthHelper.AuthCallback() {
                    @Override
                    public void onSuccess(String accessToken, String refreshToken, Long expiration) {
                        FitbitTokenStore store = new FitbitTokenStore(getApplicationContext());
                        store.saveTokens(accessToken, refreshToken, expiration);
                        finish();
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                        finish();
                    }
                });*/
            //}
        //}
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
        AuthorizationException ex = AuthorizationException.fromIntent(intent);

        Log.d("FitbitAuthActivity", "Parsed response: " + response);
        Log.d("FitbitAuthActivity", "Parsed exception: " + ex);
        /*if (response != null) {
            AuthorizationService service = new AuthorizationService(this);
            service.performTokenRequest(
                    response.createTokenExchangeRequest(),
                    (tokenResponse, tokenEx) -> {
                        if (tokenResponse != null) {
                            // Save tokens
                            FitbitTokenStore store = new FitbitTokenStore(getApplicationContext());
                            store.saveTokens(
                                    tokenResponse.accessToken,
                                    tokenResponse.refreshToken,
                                    tokenResponse.accessTokenExpirationTime
                            );
                        } else {
                            tokenEx.printStackTrace();
                        }
                    }
            );
        } else if (ex != null) {
            ex.printStackTrace();
        }*/
    }
}