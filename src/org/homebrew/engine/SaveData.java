package org.homebrew.engine;

import javax.tv.xlet.XletContext;
import java.io.*;

public class SaveData {

    /** ada save location */
    private final String adaFolder;

    /** save file name */
    private String filename;

    /**
     * constructor
     *
     * @param context Xlet context
     */
    public SaveData(XletContext context) {
        // get ada root
        String adaRoot = System.getProperty("dvb.persistent.root");
        System.err.println("ada root: " + adaRoot);
        // get the origination id
        String orgID = (String) context.getXletProperty("dvb.org.id");
        System.err.println("orgID: " + orgID);
        // get the app id
        String appID = (String) context.getXletProperty("dvb.app.id");
        System.err.println("appID: " + appID);
        // make the ada folder path based on origination and app id
        adaFolder = adaRoot + java.io.File.separator + orgID + java.io.File.separator + appID + java.io.File.separator;
        System.err.println("ada root: " + adaFolder);
    }

    /**
     * overload constructor
     *
     * @param context Xlet context
     *
     * @param filename filename to use for save data
     */
    public SaveData(XletContext context, String filename) {
        // run constructor
        this(context);
        // set the filename
        setFilename(filename);
    }

    /**
     * set the file name to save data into
     *
     * @param filename save file name
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * get the save file name
     *
     * @return String save file name
     */
    public String getFilename() {
        return filename;
    }

    /**
     * get the file path
     */
    public String getPath(String filename) {
        // create full path
        return adaFolder + filename;
    }

    /**
     * create file if it doesn't exist
     */
    public void createNewFile() {
        if(!exists()) {
            File file = new File(getPath(filename));
            try {
                file.createNewFile();
            } catch(IOException e) {
                System.err.println("Could not create file: " + getPath(filename) + "\n");
                System.err.println("Exception: " + e.toString() + "\n");
                System.err.println("StackTrace: ");
                e.printStackTrace();
            }
        }
    }

    /**
     * return if file exists
     *
     * @return true if file exists, otherwise false
     */
    public boolean exists() {
        // create file handler
        File file = new File(getPath(filename));
        // return if it exists
        return file.exists();
    }

    /**
     * deletes the save file
     */
    public boolean delete() {
        // create file handler
        File file = new File(getPath(filename));
        // if it doesn't create the file first
        return file.delete();
    }

    /**
     * save an integer in the save file
     *
     * @param number integer to save in save data
     *
     * @return true on success, otherwise false
     */
    public boolean writeInteger(int number) {
        try {
            // open a file output stream
            FileOutputStream out = new FileOutputStream(getPath(filename));
            // write the data
            out.write(number);
        } catch (IOException e) {
            System.err.println("Could not write integer to: " + getPath(filename) + "\n");
            System.err.println("Exception: " + e.toString() + "\n");
            System.err.println("StackTrace: ");
            e.printStackTrace();
            return false;
        }
        // return success
        return true;
    }

    /**
     * save an integer in the save file
     *
     * @param string string to save in save data
     *
     * @return true on success, otherwise false
     */
    public boolean writeString(String string) {
        try {
            // open a file output stream
            FileOutputStream out = new FileOutputStream(getPath(filename));
            // convert to string of bytes
            byte[] stringToBytes = string.getBytes();
            // write the data
            out.write(stringToBytes);
        } catch (IOException e) {
            System.err.println("Could not write string to: " + getPath(filename) + "\n");
            System.err.println("Exception: " + e.toString() + "\n");
            System.err.println("StackTrace: ");
            e.printStackTrace();
            return false;
        }
        // return success
        return true;
    }

    /**
     * reads an integer from the save data
     *
     * @return integer integer from save file
     */
    public int getInteger() {
        // integer to return
        int integer = 0;
        try {
            // create file handler
            FileInputStream in = new FileInputStream(getPath(filename));
            // get the integer
            integer = in.read();
        } catch (IOException e) {
            System.err.println("An io exception occurred: " + filename + "\n");
            System.err.println("Exception: " + e.toString() + "\n");
            System.err.println("StackTrace: ");
            e.printStackTrace();
        }
        return integer;
    }

    /**
     * reads an integer from the save data
     *
     * @return String string in save file
     */
    public String getString() {
        // string to return
        String string = "";
        try {
            // create file handler
            FileInputStream in = new FileInputStream(getPath(filename));
            // get the integer
            string = in.toString();
        } catch (IOException e) {
            System.err.println("An io exception occurred: " + filename + "\n");
            System.err.println("Exception: " + e.toString() + "\n");
            System.err.println("StackTrace: ");
            e.printStackTrace();
        }
        return string;
    }
}
