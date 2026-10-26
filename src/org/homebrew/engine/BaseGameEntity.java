package org.homebrew.engine;

/**
 * This class contains the basic properties of a game entity
 */
public class BaseGameEntity {
    /** flag to determine if to display the object */
    protected boolean alive;

    /** object's x coordinate */
    protected int x;

    /** object's y coordinate */
    protected int y;

    /** object's x velocity */
    protected int velX;

    /** object's y velocity */
    protected int velY;

    /**
     * default constructor initalizes all the object's properties
     */
    public BaseGameEntity() {
       alive = false;
       x = 0;
       y = 0;
       velX = 0;
       velY = 0;
    }

    /**
     * returns weither to display the object or not
     * @return bool true if to display the object, otherwise false
     */
    public final boolean isAlive() { return alive; }

    /**
     * returns the object's x position
     * @return int object's x position
     */
    public final int getX() { return x; }

    /**
     * returns the object's y position
     * @return int object's y position
     */
    public final int getY() { return y; }

    /**
     * returns the object's x velocity
     * @return int object's x velocity
     */
    public final int getVelX() { return velX; }

    /**
     * returns the object's y velocity
     * @return int object's y velocity
     */
    public final int getVelY() { return velY; }

    /**
     * sets whether to display the object or not
     * @param alive true to display the object, otherwise false
     */
    public final void setAlive(final boolean alive) { this.alive = alive; }

    /**
     * sets the objects x position
     * @param x objects new x position
     */
    public final void setX(final int x) { this.x =  x; }

    /**
     * sets the objects y position
     * @param y objects new y position
     */
    public final void setY(final int y) { this.y =  y; }

    /**
     * sets the objects new x velocity
     * @param velX objects new x velocity
     */
    public final void setVelX(final int velX) { this.velX =  velX; }

    /**
     * sets the objects new y velocity
     * @param velY objects new y velocity
     */
    public final void setVelY(final int velY) { this.velY =  velY; }
}
