package org.homebrew.engine;

import org.havi.ui.HScene;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.MediaTracker;
import java.awt.Rectangle;

/**
 * This class contains all the methods for displaying an image on screen. It is an extends the BaseGameObject
 * class.
 */
public final class ImageEntity extends BaseGameEntity {
    // entity image
    protected Image img;
    protected HScene hscene;
    protected Graphics g;

    /**
     * class constructor
     * @param hscene scene to add to for drawing purposes
     */
    public ImageEntity(final HScene hscene) {
        this.hscene = hscene;
        img = null;
        setAlive(true);
    }

    /**
     * Returns the image to display
     * @return Image Image to display
     */
    public final Image getImage() {
        return img;
    }

    /**
     * Sets the image to display
     * @param img Image to display
     */
    public final void setImage(final Image img) {
        this.img = img;
    }

    /**
     * returns the width of the image
     * @return int images width
     */
    public final int width() {
        return img.getWidth(hscene);
    }

    /**
     * returns the height of the image
     * @return int images height
     */
    public final int height() {
        return img.getHeight(hscene);
    }

    /**
     * returns the x coordinate of the center of the image
     * @return int x coordinate of the center of the image
     */
    public final int getCenterX() {
        return getX() + width() / 2;
    }

    /**
     * returns the y coordinate of the center of the image
     * @return int y coordinate of the center of the image
     */
    public final int getCenterY() {
        return getY() + height() / 2;
    }

    /**
     * Sets the graphic context for the image
     * @param g new graphic context to use
     */
    public final void setGraphics(final Graphics g) {
        this.g = g;
    }

    /**
     * loads an image to display
     * @param filename filename of image to load
     */
    public final void load(final String filename) {
        try {
            Toolkit tk = Toolkit.getDefaultToolkit();
            img = tk.getImage(this.getClass().getResource(filename));
            MediaTracker mt = new MediaTracker(this.hscene);
            mt.addImage(img,1);
            mt.waitForAll();
        } catch (Exception e) {
            System.err.println("Couldn't load Image: " + filename + "\n");
            System.err.println("Exception: " + e.toString() + "\n");
            System.err.println("StackTrace: ");
            e.printStackTrace();

        }
    }

    /**
     * Draws the image on the screen on its x and y coordinate
     * @param g graphics context to use for drawing
     */
    public final void draw(final Graphics g) {
       if (img!=null) g.drawImage(img, getX(),getY(), null);
    }

    /**
     * Returns the bounding rectangle for the image
     * @return Rectangle bounding rectangle of image
     */
    public final Rectangle getBounds() {
        Rectangle r;
        r = new Rectangle((int)getX(), (int)getY(), (int)width(), (int)height());
        return r;
    }
}
