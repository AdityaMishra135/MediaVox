package org.skywaves.mediavox.jobs

import android.annotation.TargetApi
import android.app.job.JobInfo
import android.app.job.JobInfo.TriggerContentUri
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.provider.MediaStore.Audio
import android.provider.MediaStore.Video
import org.skywaves.mediavox.core.extensions.getParentPath
import org.skywaves.mediavox.core.extensions.getStringValue
import org.skywaves.mediavox.core.helpers.ensureBackgroundThread
import org.skywaves.mediavox.extensions.addPathToDB
import org.skywaves.mediavox.extensions.updateDirectoryPath

// based on https://developer.android.com/reference/android/app/job/JobInfo.Builder.html#addTriggerContentUri(android.app.job.JobInfo.TriggerContentUri)
@TargetApi(Build.VERSION_CODES.N)
class NewPhotoFetcher : JobService() {
    companion object {
        const val PHOTO_VIDEO_CONTENT_JOB = 1
        private val MEDIA_URI = Uri.parse("content://${MediaStore.AUTHORITY}/")
        private val VIDEO_PATH_SEGMENTS = Video.Media.EXTERNAL_CONTENT_URI.pathSegments
        private val AUDIO_PATH_SEGMENTS = Audio.Media.EXTERNAL_CONTENT_URI.pathSegments
    }

    private val mHandler = Handler()
    private val mWorker = Runnable {
        scheduleJob(this@NewPhotoFetcher)
        jobFinished(mRunningParams, false)
    }

    private var mRunningParams: JobParameters? = null

    fun scheduleJob(context: Context) {
        val componentName = ComponentName(context, NewPhotoFetcher::class.java)
        val videoUri = Video.Media.EXTERNAL_CONTENT_URI
        val audioUri = Audio.Media.EXTERNAL_CONTENT_URI
        JobInfo.Builder(PHOTO_VIDEO_CONTENT_JOB, componentName).apply {
            addTriggerContentUri(TriggerContentUri(videoUri, TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS))
            addTriggerContentUri(TriggerContentUri(audioUri, TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS))
            addTriggerContentUri(TriggerContentUri(MEDIA_URI, 0))

            try {
                context.getSystemService(JobScheduler::class.java)?.schedule(build())
            } catch (ignored: Exception) {
            }
        }
    }

    fun isScheduled(context: Context): Boolean {
        val jobScheduler = context.getSystemService(JobScheduler::class.java)
        val jobs = jobScheduler.allPendingJobs
        return jobs.any { it.id == PHOTO_VIDEO_CONTENT_JOB }
    }

    override fun onStartJob(params: JobParameters): Boolean {
        mRunningParams = params
        ensureBackgroundThread {
            val affectedFolderPaths = HashSet<String>()
            if (params.triggeredContentAuthorities != null && params.triggeredContentUris != null) {
                val ids = arrayListOf<String>()
                for (uri in params.triggeredContentUris!!) {
                    val path = uri.pathSegments
                    if (path != null && (path.size == VIDEO_PATH_SEGMENTS.size + 1 || path.size ==AUDIO_PATH_SEGMENTS.size + 1)) {
                        ids.add(path[path.lastIndex])
                    }
                }

                if (ids.isNotEmpty()) {
                    val selection = StringBuilder()
                    for (id in ids) {
                        if (selection.isNotEmpty()) {
                            selection.append(" OR ")
                        }
                        selection.append("${Video.VideoColumns._ID} = '$id'")
                    }

                    var cursor: Cursor? = null
                    try {
                        val projection = arrayOf(Video.VideoColumns.DATA)
                        val uris = arrayListOf(Video.Media.EXTERNAL_CONTENT_URI, Audio.Media.EXTERNAL_CONTENT_URI)
                        uris.forEach {
                            cursor = contentResolver.query(it, projection, selection.toString(), null, null)
                            while (cursor!!.moveToNext()) {
                                val path = cursor!!.getStringValue(Video.VideoColumns.DATA)
                                affectedFolderPaths.add(path.getParentPath())
                                addPathToDB(path)
                            }
                        }
                    } catch (ignored: Exception) {
                    } finally {
                        cursor?.close()
                    }
                }
            }

            affectedFolderPaths.forEach {
                updateDirectoryPath(it)
            }
        }

        mHandler.post(mWorker)
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        mHandler.removeCallbacks(mWorker)
        return false
    }
}
