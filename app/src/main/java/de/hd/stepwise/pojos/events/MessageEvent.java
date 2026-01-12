package de.hd.stepwise.pojos.events;

public abstract class MessageEvent<T> extends Event<T> {

    public String message;
    public MessageEvent(T content, String message) {
        super(content);
        this.message = message;
    }

    public MessageEvent(T content) {
        super(content);
        this.message = null;
    }
}
