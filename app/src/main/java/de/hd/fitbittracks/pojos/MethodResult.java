package de.hd.fitbittracks.pojos;

import de.hd.fitbittracks.enums.ResultStatus;

public class MethodResult {
    public ResultStatus status;
    public String message;

    public MethodResult(ResultStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
