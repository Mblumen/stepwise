package de.hd.stepwise.ui.passport;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class JourneyPassportNavigationTest {
    @Test
    public void repeatedJourneysKeepDistinctSafeArgsProgressIds() {
        JourneyPassportFragmentArgs first = new JourneyPassportFragmentArgs.Builder(10).build();
        JourneyPassportFragmentArgs second = new JourneyPassportFragmentArgs.Builder(11).build();

        assertEquals(10L, first.getProgressId());
        assertEquals(11L, second.getProgressId());
    }
}
