package org.skywaves.mediavox.helpers

import android.os.AsyncTask

import java.io.File

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

class GetAudioCoverImageTask(private val context: Context, private val audioFilePath: String, private val callback: (Uri?) -> Unit) :
    AsyncTask<Void, Void, Uri?>() {

    override fun doInBackground(vararg params: Void?): Uri? {
        return getAudioCoverImage(context, audioFilePath)
    }

    override fun onPostExecute(result: Uri?) {
        callback(result)
    }

    private fun getAudioCoverImage(context: Context, audioFilePath: String): Uri? {
        val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.ALBUM_ID
        )
        val selection = "${MediaStore.Audio.Media.DATA}=?"
        val selectionArgs = arrayOf(audioFilePath)

        context.contentResolver.query(audioUri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                return getAlbumArtUri(albumId)
            }
        }
        return null
    }

    private fun getAlbumArtUri(albumId: Long): Uri? {
        val albumArtUri = Uri.parse("content://media/external/audio/albumart")
        return ContentUris.withAppendedId(albumArtUri, albumId)
    }
}
