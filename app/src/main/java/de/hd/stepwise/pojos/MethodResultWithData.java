package de.hd.stepwise.pojos;

import de.hd.stepwise.enums.ResultStatus;

public class MethodResultWithData<T> extends MethodResult{

    public T data;
    public MethodResultWithData(ResultStatus status, String message) {
        super(status, message);
    }

    public MethodResultWithData(ResultStatus status, String message, T data) {
        super(status, message);
        this.data = data;
    }
}
