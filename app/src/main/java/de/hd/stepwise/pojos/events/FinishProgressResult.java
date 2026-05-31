package de.hd.stepwise.pojos.events;

import de.hd.stepwise.pojos.MethodResult;

public class FinishProgressResult {
    public MethodResult methodResult;
    public StepUpdateResult stepUpdateResult;

    public FinishProgressResult(MethodResult methodResult, StepUpdateResult stepUpdateResult) {
        this.methodResult = methodResult;
        this.stepUpdateResult = stepUpdateResult;
    }
}