package org.homebrew.engine;

public class Enum {
    /** enum value */
    protected int value;

    /**
     * default constructor
     *
     * @param value enum value
     */
    protected Enum(final int value) {
        this.value = value;
    }

    /**
     * returns the enums' value
     *
     * @return int enums' value
     */
    public int value() {
        return this.value;
    }
}