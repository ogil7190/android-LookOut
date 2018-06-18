package com.mycampusdock.dock.utils;

import android.os.AsyncTask;
import android.os.Environment;

import com.mycampusdock.dock.Config;
import com.mycampusdock.dock.console;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by ogil on 03/03/18.
 */

public class DownloadFileFromURL extends AsyncTask<String, String, String> {
    private OnFileDownloadListener listener;
    private File folder;

    public DownloadFileFromURL(String rootFolder, OnFileDownloadListener listener){
        this.listener = listener;
        this.folder = new File(Environment.getExternalStorageDirectory() + File.separator + rootFolder);
    }

    @Override
    protected void onPreExecute() {
        listener.onFileDownloadStarted();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... f_url) {
        int count;
        try {
            String fileUrl = Config.URL_BASE_MEDIA + f_url[0] ;
            URL url = new URL(fileUrl);
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStream input = new BufferedInputStream(url.openStream(), 8192); //8k buffer
            if(!folder.exists()){
                folder.mkdirs();
            }
            folder = new File(folder, f_url[0]);
            if(folder.exists()){
                folder.delete();
            }

            OutputStream output = new FileOutputStream(folder);

            byte data[] = new byte[1024];

            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();

        }
        catch (Exception e) {
            console.log("Error:"+e.getMessage());
            listener.onFileDownloadError();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        listener.onFileDownloadProgress(values[0]);
    }

    @Override
    protected void onPostExecute(String file_url) {
        listener.onFileDownloadComplete();
    }

    public interface  OnFileDownloadListener{
        void onFileDownloadStarted();
        void onFileDownloadProgress(String value);
        void onFileDownloadError();
        void onFileDownloadComplete();
    }
}
