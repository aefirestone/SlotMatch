package org.homebrew.engine;

/**
 * This class represents a single point on a game object and classes may extend it to represent a single
 * fixed point value such as a position or velocity etc.
 */
public final class Point2D {
    /** x value */
    private double x;

    /** y value */
    private double y;

    /**
     * double constructor
     * @param x x value as a double
     * @param y y value as a double
     */
    public Point2D(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * float constructor
     * @param x x value as a float
     * @param y y value as a float
     */
    public Point2D(final float x, final float y) {
        this.x = (double)x;
        this.y = (double)y;
    }

    /**
     * int constructor
     * @param x x value as integer
     * @param y y value as integer
     */
    public Point2D(final int x, final int y) {
        this.x = (double)x;
        this.y = (double)y;
    }

    /**
     * Returns the x value
     * @return double x value
     */
    public final double X() { return x; }

    /**
     * Sets the x value as a double
     * @param x new x value as a double
     */
    public final void setX(final double x) { this.x = x; }

    /**
     * Sets the x value as a float
     * @param x new x value as a float
     */
    public final void setX(final float x) { this.x = (double)x; }

    /**
     * Sets the x value as an integer
     * @param x new x value as an integer
     */
    public final void setX(final int x) { this.x = (double)x; }

    /**
     * increment the x value as a double
     * @param inc increment to x as a double
     */
    public final void incrementX(final double inc) { this.x += inc; }

    /**
     * increment the x value as a float
     * @param inc increment to x as a float
     */
    public final void incrementX(final float inc) { this.x += inc; }

    /**
     * increment the x value as an integer
     * @param inc increment to x as a integer
     */
    public final void incrementX(final int inc) { this.x += inc; }

    /**
     * decrement the x value as a double
     * @param dec increment to x as a double
     */
    public final void decrementX(final double dec) { this.x -= dec; }

    /**
     * decrement the x value as a float
     * @param dec increment to x as a float
     */
    public final void decrementX(final float dec) { this.x -= dec; }

    /**
     * decrement the x value as an integer
     * @param dec increment to x as a integer
     */
    public final void decrementX(final int dec) { this.x -= dec; }

    /**
     * Returns the y value
     * @return double y value
     */
    public final double Y() { return y; }

    /**
     * Sets the y value as a double
     * @param y new y value as a double
     */
    public final void setY(final double y) { this.y = y; }

    /**
     * Sets the y value as a float
     * @param y new y value as a float
     */
    public final void setY(final float y) { this.y = (double)y; }

    /**
     * Sets the y value as an integer
     * @param y new y value as an integer
     */
    public final void setY(final int y) { this.y = (double)y; }

    /**
     * increment the y value as a double
     * @param inc increment to y as a double
     */
    public final void incrementY(final double inc) { this.y += inc; }

    /**
     * increment the y value as a float
     * @param inc increment to y as a float
     */
    public final void incrementY(final float inc) { this.y += inc; }

    /**
     * increment the y value as an integer
     * @param inc increment to y as a integer
     */
    public final void incrementY(final int inc) { this.y += inc; }

    /**
     * decrement the y value as a double
     * @param dec increment to y as a double
     */
    public final void decrementY(final double dec) { this.y -= dec; }

    /**
     * decrement the y value as a float
     * @param dec increment to y as a float
     */
    public final void decrementY(final float dec) { this.y -= dec; }

    /**
     * decrement the y value as an integer
     * @param dec increment to y as a integer
     */
    public final void decrementY(final int dec) { this.y -= dec; }
}
