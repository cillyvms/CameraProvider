package dev.estrogen.cameraprovider;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class ImageCaptureActivity extends Activity {
    private static final Random rand = new Random();
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd", Locale.US);
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
    private static final int REQUEST_CAPTURE = 1;

    private Uri outputUri = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        outputUri = CameraPhotoProvider.uriForFilename(generateFilename());

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        cameraIntent.setClipData(ClipData.newRawUri("", outputUri));
        cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(cameraIntent, REQUEST_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Intent resultIntent = new Intent(Intent.ACTION_GET_CONTENT);
                resultIntent.setDataAndType(outputUri, "image/jpeg");
                resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                setResult(resultCode, resultIntent);
            } else {
                setResult(resultCode);
            }
            finish();
        }
    }

    @NonNull
    private String generateFilename() {
        byte[] randId = new byte[8];
        rand.nextBytes(randId);
        char[] randIdCh = new char[randId.length * 2];
        for (int i = 0; i < randId.length; i++) {
            int b = randId[i] & 0xFF;
            randIdCh[i*2] = HEX_CHARS[b >>> 4];
            randIdCh[i*2+1] = HEX_CHARS[b & 0xF];
        }
        String randIdStr = new String(randIdCh);
        String datePart = dateFormatter.format(new Date());
        return "IMG_" + datePart + "_" + randIdStr + ".jpg";
    }
}
