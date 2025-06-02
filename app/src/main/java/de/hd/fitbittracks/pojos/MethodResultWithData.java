package de.hd.fitbittracks.pojos;

import de.hd.fitbittracks.enums.ResultStatus;

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
