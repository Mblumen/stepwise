package de.hd.fitbittracks.database;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class InternalStorageHelper {

    public static String saveImageToInternalStorage(Context context, Bitmap bitmap, String filename) {
        File directory = new File(context.getFilesDir(), "track_images");
        boolean directoryExists = true;
        if (!directory.exists()) {
            directoryExists = directory.mkdir();
        }

        if(!directoryExists) {
            // Handle the case where the directory could not be created
            return null;
        }
        File file = new File(directory, filename);
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap loadImageFromInternalStorage(String path) {
        return BitmapFactory.decodeFile(path);
    }
}
