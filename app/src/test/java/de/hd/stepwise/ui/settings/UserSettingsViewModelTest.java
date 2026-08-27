package de.hd.stepwise.ui.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import de.hd.stepwise.enums.ResultStatus;
import de.hd.stepwise.enums.StepSource;
import de.hd.stepwise.pojos.MethodResult;

public class UserSettingsViewModelTest {

    @Test
    public void successfulRevokeSwitchesToPhoneSourceBeforeReportingSuccess() {
        AtomicReference<StepSource> requestedSource = new AtomicReference<>();
        AtomicReference<MethodResult> result = new AtomicReference<>();

        UserSettingsViewModel.disconnectGoogleHealth("account@example.com",
                (account, success, error) -> success.run(),
                (source, callback) -> {
                    requestedSource.set(source);
                    callback.accept(new MethodResult(ResultStatus.SUCCESS, "Step source updated"));
                },
                result::set,
                exception -> { });

        assertEquals(StepSource.STEP_COUNTER, requestedSource.get());
        assertEquals(ResultStatus.SUCCESS, result.get().status);
        assertEquals("Disconnected from Google Health", result.get().message);
    }

    @Test
    public void failedRevokeDoesNotSwitchSource() {
        Exception revokeFailure = new Exception("revoke failed");
        AtomicBoolean sourceSwitchRequested = new AtomicBoolean();
        AtomicReference<MethodResult> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        UserSettingsViewModel.disconnectGoogleHealth("account@example.com",
                (account, success, failure) -> failure.accept(revokeFailure),
                (source, callback) -> sourceSwitchRequested.set(true),
                result::set,
                error::set);

        assertFalse(sourceSwitchRequested.get());
        assertNull(result.get());
        assertSame(revokeFailure, error.get());
    }

    @Test
    public void unavailablePhoneSensorIsReportedAfterSuccessfulRevoke() {
        AtomicReference<MethodResult> result = new AtomicReference<>();
        AtomicBoolean revokeErrorReported = new AtomicBoolean();

        UserSettingsViewModel.disconnectGoogleHealth("account@example.com",
                (account, success, error) -> success.run(),
                (source, callback) -> callback.accept(new MethodResult(
                        ResultStatus.ERROR, "Phone step counter is unavailable")),
                result::set,
                exception -> revokeErrorReported.set(true));

        assertEquals(ResultStatus.ERROR, result.get().status);
        assertTrue(result.get().message.contains("phone step counter is unavailable"));
        assertFalse(revokeErrorReported.get());
    }
}
