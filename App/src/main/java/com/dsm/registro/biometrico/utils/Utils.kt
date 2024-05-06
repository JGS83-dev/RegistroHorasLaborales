package com.dsm.registro.biometrico.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log

class Utils {

    companion object {
        fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
            var cursor: Cursor? = null
            return try {
                val proj = arrayOf(MediaStore.Images.Media.DATA)
                cursor = context.getContentResolver().query(contentUri, proj, null, null, null)
                val column_index: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
//            Log.i("Real Path Imagen ->",cursor.getString(column_index))
                cursor.getString(column_index)
            } catch (e: Exception) {
                Log.e("Fallo al obtener URI: ", "getRealPathFromURI Exception : $e")
                ""
            } finally {
                cursor?.close()
            }
        }
    }
}