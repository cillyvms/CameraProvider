/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.estrogen.cameraprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

class LazyExifLoader {
    private static final String TAG = "LazyExifLoader";
    private final File srcFile;
    private ExifInterface exif = null;
    private boolean initialized = false;

    LazyExifLoader(File file) {
        srcFile = file;
    }

    @Nullable
    public ExifInterface get() {
        if (!initialized) {
            initialized = true;
            try {
                exif = new ExifInterface(srcFile);
            } catch (IOException e) {
                Log.w(TAG, "failed to load exif data", e);
            }
        }
        return exif;
    }
}

public class CameraPhotoProvider extends ContentProvider {
    @NonNull
    static Uri uriForFilename(String filename) {
        return new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority("dev.estrogen.cameraprovider.photo")
                .encodedPath(filename)
                .build();
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        String[] proj;
        if (projection == null) {
            proj = new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE, MediaStore.MediaColumns.MIME_TYPE};
        } else {
            proj = projection;
        }
        MatrixCursor cur = new MatrixCursor(proj, 1);

        File requestedFile = fileFromUri(uri);
        LazyExifLoader exif = new LazyExifLoader(requestedFile);
        if (requestedFile != null && requestedFile.isFile()) {
            Object[] row = new Object[proj.length];
            for (int i = 0; i < proj.length; i++) {
                switch (proj[i]) {
                    case OpenableColumns.DISPLAY_NAME:
                        row[i] = requestedFile.getName();
                        break;
                    case OpenableColumns.SIZE:
                        row[i] = requestedFile.length();
                        break;
                    case MediaStore.MediaColumns.MIME_TYPE:
                        row[i] = "image/jpeg";
                        break;
                    case MediaStore.MediaColumns.ORIENTATION:
                        ExifInterface e = exif.get();
                        row[i] = e != null ? e.getRotationDegrees() : 0;
                        break;
                }
            }
            cur.addRow(row);
        }

        return cur;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        File requestedFile = fileFromUri(uri);
        if (requestedFile != null && requestedFile.isFile()) {
            // currently we only support jpegs as that's the only format ACTION_IMAGE_CAPTURE returns
            return "image/jpeg";
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        File requestedFile = fileFromUri(uri);
        int modeBits = ParcelFileDescriptor.parseMode(mode);
        if ((modeBits & ParcelFileDescriptor.MODE_READ_ONLY) == 0) {
            // if writing make sure the parent directory exists
            File capturesDir = getCapturesDir(requireContextCompat());
            if (!capturesDir.isDirectory() && !capturesDir.mkdir()) {
                throw new IllegalStateException("Cannot create captures directory.");
            }
        }
        return ParcelFileDescriptor.open(requestedFile, modeBits);
    }

    @Nullable
    private File fileFromUri(@NonNull Uri uri) {
        List<String> pathSegments = uri.getPathSegments();
        // we only support files stored directly under the captures directory
        if (pathSegments.size() != 1) {
            return null;
        }
        String filename = pathSegments.get(0);
        if (".".equals(filename) || "..".equals(filename)) {
            return null;
        }
        return new File(getCapturesDir(requireContextCompat()), filename);
    }

    @NonNull
    static File getCapturesDir(Context ctx) {
        return new File(ctx.getCacheDir(), "captures");
    }

    @NonNull
    private Context requireContextCompat() {
        final Context ctx = getContext();
        if (ctx == null) {
            throw new IllegalStateException("Cannot get context from provider.");
        }
        return ctx;
    }
}
