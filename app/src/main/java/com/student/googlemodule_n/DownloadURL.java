
package com.student.googlemodule_n;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


//retrieve data from URL using http connection and file handling method
//returns data in JSON
public class DownloadURL {
    public String ReadTheURL(String placeURL) throws IOException{
        String Data ="";
        InputStream inputStream = null;

        //http obj
        HttpURLConnection httpURLConnection = null;


        try {

            URL url = new URL(placeURL);
            httpURLConnection=(HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //read data from url
            inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer = new StringBuffer();

            String line="";

            while((line= bufferedReader.readLine())!=null){
                stringBuffer.append(line);
            }

            Data = stringBuffer.toString();
            bufferedReader.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        finally{
            inputStream.close();
            httpURLConnection.disconnect();
        }

        return Data;
    }
}

