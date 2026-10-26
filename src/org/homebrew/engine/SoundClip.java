package org.homebrew.engine;

import java.io.IOException;
import java.net.URL;
import org.havi.ui.HSound;

public final class SoundClip {

    /** sound clip */
    private HSound clip;

    /** filename of audio clip */
    private String filename;

    /** is clip should loop */
    private boolean isLooping;

    /**
     * default constructor
     */
    public SoundClip() {
        clip = new HSound();
    }

    /**
     * overloading constructor
     *
     * @param filename filename of audio clip
     */
    public SoundClip(final String filename) {
        // call the default constructor first
        this();
        // load the audio clip
        load(filename);
    }

    /**
     * returns the audio clips filename
     *
     * @return string audio clip filename
     */
    private String getFilename() { return filename; }

    /**
     * Sets the filename of the audio clip
     *
     * @param filename name of audio clip file
     */
    public final void setFilename(final String filename) { this.filename = filename; }

    /**
     * returns if the audio clip is set to loop
     *
     * @return true if the clip is set to loop, otherwise false
     */
    public final boolean isLooping() {
        return isLooping;
    }

    /**
     * set if the audio clip should loop
     *
     * @param loop boolean if the clip should loop
     */
    public final void setLooping(final boolean loop) {
        this.isLooping = loop;
    }

    /**
     * property to verify when sample is ready
     *
     * @return true if audio clip is loaded, otherwise false
     */
    public final boolean isLoaded() {
        return (boolean)(clip != null);
    }

    /**
     * Returns the url path of the file
     * @param filename name of file to load
     * @return URL url to file
     */
    private URL getURL(final String filename) {
        URL url = null;
        try {
            url = this.getClass().getResource(filename);
        }
        catch (Exception e) {
            System.err.println("Couldn't get class URL: " + filename + "\n");
            System.err.println("Exception: " + e.toString() + "\n");
            System.err.println("StackTrace: ");
            e.printStackTrace();
        }
        return url;
    }

    /**
     * loads the audio clip
     * @param audiofile file to load
     * @return true if successful otherwise false
     */
    public final boolean load(final String audiofile) {
        //prepare the input stream for an audio file
        setFilename(audiofile);
        try {
            clip.load(getURL(filename));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * plays the audio clip
     */
    public final void play() {
        // exit if the sample hasn't been loaded
        if (!isLoaded()) return;

        if (isLooping) {
            // loop the clip
            clip.loop();
        } else {
            // play the clip
            clip.play();
        }
    }

    /**
     * stops an audio clip
     */
    public final void stop() {
        clip.stop();
    }
}