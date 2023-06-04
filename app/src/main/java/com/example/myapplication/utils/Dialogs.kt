package com.example.myapplication.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.myapplication.R
import com.example.myapplication.manger.MediaManager

fun Context.selectImageDialog(mediaManager:MediaManager?) {
    val options = arrayOf<CharSequence>(
        getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(
            R.string.cancel
        )
    )
    AlertDialog.Builder(this).apply {
        setTitle(getString(R.string.selectanoption))
        setItems(options) { dialog, item ->
            if (options[item] == getString(R.string.take_photo)) {
                dialog.dismiss()
                mediaManager?.handleMediaClick(MediaManager.MediaTypes.PHOTO.name)
            } else if (options[item] == getString(R.string.choose_from_gallery)) {
                dialog.dismiss()
                mediaManager?.handleMediaClick(MediaManager.MediaTypes.GALLERY.name)
            } else if (options[item] == getString(R.string.cancel)) {
                dialog.dismiss()
            }
        }
        show()
    }
}
