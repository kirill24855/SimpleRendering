package pro.shpin.kirill.simplerendering.game;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by wiish on 10/16/2016.
 */

public class Utils {
    public static void writeToFile(String data, String path) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(GLApplication.context.openFileOutput(path, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static String readFromFile(int file_id) {
        String ret = "";

        try {
            InputStream inputStream = GLApplication.resources.openRawResource(file_id);

            if(inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String reciveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while((reciveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(reciveString + "\n");
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch(FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
}
