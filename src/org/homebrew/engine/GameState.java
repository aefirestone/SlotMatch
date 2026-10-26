package org.homebrew.engine;

import org.homebrew.engine.Enum;

public class GameState extends Enum {
    /** title screen state */
    public static final int TITLESCREEN = 0;
    public static final int PLAYING = 1;
    public static final int DEATH = 2;
    public static final int GAMEOVER = 3;

    /**
     * default constructor
     *
     * @param value enum value
     */
    public GameState(int value) {
        super(value);
    }
}
