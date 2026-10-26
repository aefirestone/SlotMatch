package org.homebrew.engine;

import org.havi.ui.HScene;

import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * This class contains all the methods necessary to work with a single sprite such as moving, displaying and
 * collision detection.
 */
public class Sprite {
    /** the sprites image */
    private ImageEntity entity;

    /** sprites position */
    protected Point2D pos;

    /** sprites velocity */
    protected Point2D vel;

    /** the sprites current state */
    protected int currentState;

    /**
     * class constructor initializes the sprites properties and adds it to the scene
     * @param hscene scene to add to for drawing purposes
     */
    public Sprite(final HScene hscene) {
        entity = new ImageEntity(hscene);
        entity.alive = false;
        pos = new Point2D(0, 0);
        vel = new Point2D(0, 0);
        currentState = 0;
    }

    /**
     * loads an image to use as the sprite
     * @param filename filename of image to load and use as the sprite image
     */
    public final void load(final String filename) {
        entity.load(filename);
    }

    /**
     * draws the sprite on the screen
     * @param g graphics context to use for drawing
     */
    public final void draw(final Graphics g) {
        entity.draw(g);
    }

    /**
     * updates the sprites position based on it's velocity
     */
    public final void updatePosition() {
        pos.setX(pos.X() + vel.X());
        pos.setY(pos.Y() + vel.Y());
        entity.setX((int)pos.X());
        entity.setY((int)pos.Y());
    }

    /**
     * gets the sprites current state (alive, dead, etc.)
     * @return int sprites current state
     */
    public final int getState() { return currentState; }

    /**
     * sets the sprites new state
     * @param state sprites new state
     */
    public final void setState(final int state) { this.currentState = state; }

    /**
     * returns the bounding rectangle of the sprite
     * @return Rectangle sprites bounding rectangle
     */
    public final Rectangle getBounds() { return entity.getBounds(); }

    /**
     * Reurns the sprites position as a single tuple
     * @return Point2D sprites position
     */
    public final Point2D Position() { return pos; }

    /**
     * sets the sprites position
     * @param pos sprites new position as a tuple
     */
    public final void setPosition(final Point2D pos) { this.pos = pos; }

    /**
     * returns the sprites velocity as a single tuple
     * @return Point2D sprites new velocity
     */
    public final Point2D Velocity() { return vel; }

    /**
     * sets the sprites velocity
     * @param vel sprites new velocity as a tuple
     */
    public final void setVelocity(final Point2D vel) { this.vel = vel; }

    /**
     * returns the center position of the image as a tuple
     * @return Point2D center of image as a tuple of x and y position
     */
    public final Point2D center() { return(new Point2D(entity.getCenterX(), entity.getCenterY())); }

    /**
     * returns if the sprite should be displayed
     * @return boolean true if to display the sprite, otherwise false
     */
    public final boolean alive() { return entity.isAlive(); }

    /**
     * sets whether to display the sprite or not
     * @param alive true to display the object, otherwise false
     */
    public final void setAlive(final boolean alive) { entity.setAlive(alive); }

    /**
     * returns the sprite images width
     * @return int sprite images width
     */
    public final int imageWidth() { return entity.width(); }

    /**
     * returns the sprite images height
     * @return int sprite images height
     */
    public final int imageHeight() { return entity.height(); }

    /**
     * returns if the sprite collides with another object
     * @param rect bounds of object to check against
     * @return true if sprite has collided with object, otherwise false
     */
    public final boolean collidesWith(final Rectangle rect) { return (rect.intersects(getBounds())); }

    /**
     * returns if the sprite collides with another sprite
     * @param sprite sprite to check against
     * @return true if sprite has collided with sprite, otherwise false
     */
    public final boolean collidesWith(final Sprite sprite) { return (getBounds().intersects(sprite.getBounds())); }

    /**
     * returns if the sprite collides with another point
     * @param point point to check against
     * @return true if sprite has collided with point, otherwise false
     */
    public final boolean collidesWith(final Point2D point) { return (getBounds().contains(point.X(), point.Y())); }
}
