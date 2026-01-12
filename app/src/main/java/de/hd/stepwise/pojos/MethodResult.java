package de.hd.stepwise.pojos;

import de.hd.stepwise.enums.ResultStatus;

public class MethodResult {
    public ResultStatus status;
    public String message;

    public MethodResult(ResultStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
