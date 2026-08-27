package de.hd.stepwise.helper.googlehealth;

public class GoogleHealthAuthorizationRequiredException extends Exception {
    public GoogleHealthAuthorizationRequiredException() {
        super("Google Health authorization requires user interaction");
    }
}
