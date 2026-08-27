# Google Health API migration notes

Research date: 2026-08-26

## Findings

- The legacy Fitbit Web API is scheduled to shut down in September 2026. Google
  requires integrations to move to the Google Health API and Google OAuth 2.0
  before then. [Google Health API overview](https://developers.google.com/health/about)
- This is not an endpoint-only authentication change. Existing Fitbit access and
  refresh tokens cannot be transferred, so users must grant consent again. Google
  recommends temporarily supporting both OAuth systems and recording which one a
  user uses during migration. [Migration overview](https://developers.google.com/health/migration)
- Google Health API setup currently documents a **Web Server** OAuth client, with a
  client ID and client secret. Refresh-token exchange includes that secret, and the
  production codelab states that a server parses the authorization response. That
  secret must not be embedded in this Android APK. Google Health's documentation
  does not currently describe a native Android flow.
  [Cloud and OAuth setup](https://developers.google.com/health/setup),
  [first API call codelab](https://developers.google.com/health/codelabs/make-your-first-api-call)
- Google's installed-app OAuth documentation says custom URI schemes are no longer
  supported for Android clients. The current `de.stepwise://oauth/callback` redirect
  cannot simply be reused for Google OAuth.
  [OAuth 2.0 for native apps](https://developers.google.com/identity/protocols/oauth2/native-app)
- Reading steps requires the restricted
  `https://www.googleapis.com/auth/googlehealth.activity_and_fitness.readonly`
  scope. All Google Health scopes are restricted, and a public integration needs
  OAuth verification/security review; unverified clients are limited to configured
  test users and 100 users.
  [Steps guide](https://developers.google.com/health/data-types/steps),
  [developer checklist](https://developers.google.com/health/developer-checklist)
- The current Fitbit daily time-series request maps to the Google Health API
  `dailyRollUp` operation:
  `POST https://health.googleapis.com/v4/users/me/dataTypes/steps/dataPoints:dailyRollUp`.
  The response uses `rollupDataPoints` with a steps rollup value rather than
  Fitbit's `activities-steps` array.
  [Steps guide](https://developers.google.com/health/data-types/steps),
  [dailyRollUp reference](https://developers.google.com/health/reference/rest/v4/users.dataTypes.dataPoints/dailyRollUp)

## Repository impact

The current implementation performs the entire Fitbit authorization-code exchange
and refresh-token lifecycle on-device through AppAuth. For this small, privately
distributed application, the preferred migration experiment is Google Play
services `AuthorizationClient`, which can request Google API scopes and return an
access token directly on Android without storing a web-client secret. Google
Health's own documentation does not explicitly confirm this client, so acceptance
of the restricted Health scope and a `dailyRollUp` request must be proven before
the full migration proceeds.

If the proof succeeds, replace `FitbitAuthHelper` and `FitbitAuthStateManager` with
native Google authorization/token handling, replace `FitbitApiService` with a
Google Health `dailyRollUp` adapter, update the settings UI to launch the returned
`PendingIntent`, and make `StepSyncWorker` defer cleanly when renewed authorization
requires foreground interaction. `StepManager` does not own OAuth and should not
become responsible for it.

[Google Play services AuthorizationClient](https://developers.google.com/android/reference/com/google/android/gms/auth/api/identity/AuthorizationClient)

## Required Google Cloud configuration

The Android implementation relies on configuration outside this repository:

1. Enable the Google Health API in the Google Cloud project used by the app.
2. Register an Android OAuth client for package `de.hd.stepwise` and the SHA-1 of
   each signing certificate used to install the APK (debug and release certificates
   are separate clients).
3. Add the restricted
   `https://www.googleapis.com/auth/googlehealth.activity_and_fitness.readonly`
   scope to the OAuth consent configuration.
4. While the consent screen is in testing mode, add every friend who uses the APK
   as a test user.

The build verifies the Android integration but cannot prove that Google Health
accepts the native token until an APK signed with a registered certificate is run
on a device with one of those configured test accounts.
