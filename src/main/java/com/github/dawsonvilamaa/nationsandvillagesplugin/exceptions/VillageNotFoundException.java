package com.github.dawsonvilamaa.nationsandvillagesplugin.exceptions;

public class VillageNotFoundException extends Exception {
    public VillageNotFoundException() {
        super("That village could not be found");
    }
}
