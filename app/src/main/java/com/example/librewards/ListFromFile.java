package com.example.librewards;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ListFromFile {

    private Context context;

    public ListFromFile(Context context) {
        this.context = context;
    }

    public List<String> readLine(String path) {
        List<String> lines = new ArrayList<>();

        AssetManager am = context.getAssets();

        try {
            InputStream inputStream = am.open(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            inputStream.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }
}