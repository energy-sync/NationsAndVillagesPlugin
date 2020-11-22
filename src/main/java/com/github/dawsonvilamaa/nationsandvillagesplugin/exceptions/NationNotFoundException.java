package com.github.dawsonvilamaa.nationsandvillagesplugin.exceptions;

public class NationNotFoundException extends Exception {
    public NationNotFoundException() {
        super("That nation could not be found");
    }
}
