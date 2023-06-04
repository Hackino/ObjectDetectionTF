package com.example.myapplication.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri


fun Context.getPath(uri: Uri?): String {
    if (uri == null)return ""
    if ("content".equals(uri.scheme, ignoreCase = true)) {
        return if (getDataColumn( uri, null, null) != null)
            getDataColumn( uri, null, null) ?:""
        else ""
    } else if ("file".equals(uri.scheme, ignoreCase = true)) {
        return uri.path?:""
    }
    return ""
}

private fun Context.getDataColumn(uri: Uri?, selection: String?, selectionArgs: Array<String?>?): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(column)
    try {
        cursor = contentResolver.query(uri!!, projection, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val column_index = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(column_index)
        }
    } catch (e: Exception) {
    } finally {
        cursor?.close()
    }
    return null
}


