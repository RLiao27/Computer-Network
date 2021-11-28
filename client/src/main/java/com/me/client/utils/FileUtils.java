package com.me.client.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.loader.content.CursorLoader;

import java.text.DecimalFormat;

public class FileUtils {

  public static String getFilePathByUri(Context context,Uri uri) {
      if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
        return uri.getPath();
      }

      if (isOtherDocument(uri)) {
        return uri.getPath();
      }

      String path = getFilePathByUri_BELOWAPI11(context,uri);
      if (path != null) {
        return path;
      }
      path = getFilePathByUri_API11to18(context,uri);
      if (path != null) {
        return path;
      }

      path = getFilePathByUri_API19(context,uri);
      return path;
  }
  
  private static String getFilePathByUri_BELOWAPI11(Context context,Uri uri) {
      if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
        String path = null;
        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
          if (cursor.moveToFirst()) {
              int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
              if (columnIndex > -1) {
                path = cursor.getString(columnIndex);
              }
          }
          cursor.close();
        }
        return path;
      }
      return null;
  }
  
  private static String getFilePathByUri_API11to18(Context context,Uri contentUri) {
      String[] projection = {MediaStore.Images.Media.DATA};
      String result = null;
      CursorLoader cursorLoader = new CursorLoader(context, contentUri, projection, null, null, null);
      Cursor cursor = cursorLoader.loadInBackground();
      if (cursor != null) {
          int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
          cursor.moveToFirst();
          result = cursor.getString(column_index);
          cursor.close();
      }
      return result;
  }
  
  private static String getFilePathByUri_API19(Context context,Uri uri) {
      if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
          if (isExternalStorageDocument(context,uri)) {
              // ExternalStorageProvider
              String docId = DocumentsContract.getDocumentId(uri);
              String[] split = docId.split(":");
              String type = split[0];
              if ("primary".equalsIgnoreCase(type)) {
                  if (split.length > 1) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                  } else {
                    return Environment.getExternalStorageDirectory() + "/";
                  }
                  // This is for checking SD Card
              }
          } else if (isDownloadsDocument(uri)) {
              int stateCode = context.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");
              if (stateCode != 0 && stateCode != 1) {
                return null;
              }
              String id = DocumentsContract.getDocumentId(uri);
              if (id.startsWith("raw:")) {
                  return id.replaceFirst("raw:", "");
              }
              if (id.contains(":")) {
                  String[] tmp = id.split(":");
                  if (tmp.length > 1) {
                    id = tmp[1];
                  }
              }
              Uri contentUri = Uri.parse("content://downloads/public_downloads");
              try {
                contentUri = ContentUris.withAppendedId(contentUri, Long.parseLong(id));
              } catch (Exception e) {
                e.printStackTrace();
              }
              String path = getDataColumn(context,contentUri, null, null);
              if (path != null) return path;
              String fileName = getFileNameByUri(context,uri);
              if (fileName != null) {
                  path = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
                  return path;
              }
          } else if (isMediaDocument(uri)) {
              String docId = DocumentsContract.getDocumentId(uri);
              String[] split = docId.split(":");
              String type = split[0];
              Uri contentUri = null;
              if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
              } else if ("video".equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
              } else if ("audio".equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
              }
              String selection = "_id=?";
              String[] selectionArgs = new String[]{split[1]};
              return getDataColumn(context,contentUri, selection, selectionArgs);
          }
        }
      }
      return null;
  }
  
  private static String getFileNameByUri(Context context,Uri uri) {
      String relativePath = getFileRelativePathByUri_API18(context,uri);
      if (relativePath == null) relativePath = "";
      final String[] projection = {
          MediaStore.MediaColumns.DISPLAY_NAME
      };
      try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
        if (cursor != null && cursor.moveToFirst()) {
            int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
            return relativePath + cursor.getString(index);
        }
      }
      return null;
  }
  
  private static String getFileRelativePathByUri_API18(Context context,Uri uri) {
      final String[] projection;
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        projection = new String[]{
            MediaStore.MediaColumns.RELATIVE_PATH
        };
        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
          if (cursor != null && cursor.moveToFirst()) {
            int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH);
            return cursor.getString(index);
          }
        }
      }
      return null;
  }
  
  private static String getDataColumn(Context context,Uri uri, String selection, String[] selectionArgs) {
      final String column = MediaStore.Images.Media.DATA;
      final String[] projection = {column};
      try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
          if (cursor != null && cursor.moveToFirst()) {
              final int column_index = cursor.getColumnIndexOrThrow(column);
              return cursor.getString(column_index);
          }
      } catch (IllegalArgumentException iae) {
          iae.printStackTrace();
      }
      return null;
  }
  
  private static boolean isExternalStorageDocument(Context context,Uri uri) {
      return "com.android.externalstorage.documents".equals(uri.getAuthority());
  }
  
  private static boolean isOtherDocument(Uri uri) {
      if (uri != null && uri.getPath() != null) {
          String path = uri.getPath();
          if (path.startsWith("/storage")) {
              return true;
          }
          if (path.startsWith("/external_files")) {
              return true;
          }
      }
      return false;
  }
  
  private static boolean isDownloadsDocument(Uri uri) {
      return "com.android.providers.downloads.documents".equals(uri.getAuthority());
  }
  
  private static boolean isMediaDocument(Uri uri) {
      return "com.android.providers.media.documents".equals(uri.getAuthority());
  }

  public static String getFileSize(long size) {
      DecimalFormat df = new DecimalFormat("#.00");
      String fileSizeString = "";
      String wrongSize = "0B";
      if (size == 0) {
          return wrongSize;
      }
      if (size < 1024) {
          fileSizeString = df.format((double) size) + "B";
      } else if (size < 1048576) {
          fileSizeString = df.format((double) size / 1024) + "KB";
      } else if (size < 1073741824) {
          fileSizeString = df.format((double) size / 1048576) + "MB";
      } else {
          fileSizeString = df.format((double) size / 1073741824) + "GB";
      }
      return fileSizeString;
  }

}