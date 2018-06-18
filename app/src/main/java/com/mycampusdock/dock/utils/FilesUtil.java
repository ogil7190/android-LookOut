package com.mycampusdock.dock.utils;

import android.os.Environment;

import java.io.File;

public class FilesUtil {

    public static boolean fileExists(String file, String rootfolder){
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + rootfolder);
        folder = new File(folder, file);
        if(folder.exists())
            return true;
        else
            return false;
    }

    public static File getFileFromURL(String localFile, String rootFolder){
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + rootFolder);
        folder = new File(folder, localFile);
        if(folder.exists()){
            return folder;
        }
        else
            return null;
    }
}
