package com.asnb.agentfe.main;

import java.io.*;
import java.util.Properties;

public class readConfigFile {

    public static String main(String args) {

        Properties prop = new Properties();

        //**************************************
        //get value from properties file
        //**************************************

        try{

            String fileName = "app.config";

            InputStream is = new FileInputStream(fileName);

            prop.load(is);

            //System.out.println(args + ": " + prop.getProperty(args));

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return prop.getProperty(args);

    }
}
