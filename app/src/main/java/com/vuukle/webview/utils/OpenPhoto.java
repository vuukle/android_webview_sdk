package com.vuukle.webview.utils;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import com.vuukle.webview.MainActivity;
import com.vuukle.webview.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.vuukle.webview.MainActivity.REQUEST_SELECT_FILE;

public class OpenPhoto {
    private String FORMAT_TIME = "yyyyMMddHHmmss";
    private String FILE_EXTENSION = ".jpg";
    private String FILE_PROVIDER = "com.vuukle.webview.android.fileprovider";
    private Uri imageUri;

    public Uri getImageUri() {
        return imageUri;
    }

    private File getPictureFile(Context contex) throws IOException {
        String timeStamp = new SimpleDateFormat(FORMAT_TIME).format(new Date());
        String pictureFile = "VUUKLE" + timeStamp;
        File storageDir = contex.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile, FILE_EXTENSION, storageDir);
        String pictureFilePath = image.getAbsolutePath();
        return image;
    }

    public  void selectImage(Context context) {
        final CharSequence[] options = {context.getString(R.string.take_photo), context.getString(R.string.choose_from_gallery), context.getString(R.string.cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.choose_your_profile_picture));

        builder.setItems(options, (dialog, item) -> {

            if (options[item].equals(context.getString(R.string.take_photo))) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photo = null;
                try {
                    photo = getPictureFile(context);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageUri = FileProvider.getUriForFile(
                        context,
                        FILE_PROVIDER,
                        photo);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        imageUri);
                ((MainActivity) context).startActivityForResult(intent, REQUEST_SELECT_FILE);
            } else if (options[item].equals(context.getString(R.string.choose_from_gallery))) {
                try {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    ((MainActivity) context).startActivityForResult(pickPhoto, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e) {
                }
            } else if (options[item].equals(context.getString(R.string.cancel))) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

}
