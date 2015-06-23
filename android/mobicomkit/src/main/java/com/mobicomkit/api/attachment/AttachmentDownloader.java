/*
 * Copyright (C) ${year} The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobicomkit.api.attachment;

import android.content.Context;
import android.util.Log;

import com.mobicomkit.api.MobiComKitClientService;
import com.mobicomkit.api.MobiComKitServer;
import com.mobicomkit.api.conversation.Message;
import com.mobicomkit.api.conversation.database.MessageDatabaseService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;

/**
 * This task downloads bytes from a resource addressed by a URL.  When the task
 * has finished, it calls handleState to report its results.
 * <p/>
 * Objects of this class are instantiated and managed by instances of PhotoTask, which
 * implements the methods of TaskRunnableDecodeMethods. PhotoTask objects call
 * {@link #AttachmentDownloader(AttachmentDownloader.TaskRunnableDownloadMethods) PhotoDownloadRunnable()} with
 * themselves as the argument. In effect, an PhotoTask object and a
 * PhotoDownloadRunnable object communicate through the fields of the PhotoTask.
 */
class AttachmentDownloader implements Runnable {

    // Constants for indicating the state of the download
    static final int HTTP_STATE_FAILED = -1;
    static final int HTTP_STATE_STARTED = 0;
    static final int HTTP_STATE_COMPLETED = 1;
    private static final String TAG = "AttachmentDownloader";
    // Sets the size for each read action (bytes)
    //Aman testing
    private static final int READ_SIZE = 1024 * 1;
    // Sets a tag for this class
    @SuppressWarnings("unused")
    private static final String LOG_TAG = "PhotoDownloadRunnable";
    // Defines a field that contains the calling object of type PhotoTask.
    final TaskRunnableDownloadMethods mPhotoTask;

    /**
     * This constructor creates an instance of PhotoDownloadRunnable and stores in it a reference
     * to the PhotoTask instance that instantiated it.
     *
     * @param photoTask The PhotoTask, which implements TaskRunnableDecodeMethods
     */
    AttachmentDownloader(TaskRunnableDownloadMethods photoTask) {
        mPhotoTask = photoTask;
    }

    /*
     * Defines this object's task, which is a set of instructions designed to be run on a Thread.
     */
    @SuppressWarnings("resource")
    @Override
    public void run() {

        /*
         * Stores the current Thread in the the PhotoTask instance, so that the instance
         * can interrupt the Thread.
         */
        mPhotoTask.setDownloadThread(Thread.currentThread());

        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);


        /*
         * A try block that downloads a attachment from a mobitexter url.
         */
        // Tries to download the picture from Mobitexter server
        try {
            // Before continuing, checks to see that the Thread hasn't been
            // interrupted
            if (Thread.interrupted()) {

                throw new InterruptedException();
            }

            // If there's no for this image
            if (mPhotoTask.getMessage() != null && !mPhotoTask.getMessage().isAttachmentDownloaded()) {

                /*
                 * Calls the PhotoTask implementation of {@link #handleDownloadState} to
                 * set the state of the download
                 */
                mPhotoTask.handleDownloadState(HTTP_STATE_STARTED);
                // Downloads the image and catches IO errors
                loadAttachmentImage(mPhotoTask.getMessage(), mPhotoTask.getContext());

            }

            /*
             * Sets the status message in the PhotoTask instance. This sets the
             * ImageView background to indicate that the image is being
             * decoded.
             */
            mPhotoTask.handleDownloadState(HTTP_STATE_COMPLETED);

            // Catches exceptions thrown in response to a queued interrupt
        } catch (InterruptedException e1) {

            // Does nothing

            // In all cases, handle the results
        } finally {

            // If the byteBuffer is null, reports that the download failed.
            if (mPhotoTask.getMessage() != null && !mPhotoTask.getMessage().isAttachmentDownloaded()) {
                mPhotoTask.handleDownloadState(HTTP_STATE_FAILED);
            }

            /*
             * The implementation of setHTTPDoCwnloadThread() in PhotoTask calls
             * PhotoTask.setCurrentThread(), which then locks on the static ThreadPool
             * object and returns the current thread. Locking keeps all references to Thread
             * objects the same until the reference to the current Thread is deleted.
             */

            // Sets the reference to the current Thread to null, releasing its storage
            mPhotoTask.setDownloadThread(null);

            // Clears the Thread's interrupt flag
            Thread.interrupted();
        }
    }

    public void loadAttachmentImage(Message message, Context context) {
        try {
            InputStream inputStream = null;
            FileMeta fileMeta = message.getFileMetas().get(0);
            String contentType = fileMeta.getContentType();
            String fileKey = fileMeta.getKeyString();
            HttpURLConnection connection = new MobiComKitClientService(context).openHttpConnection( new MobiComKitServer(context).getFileUrl()+ fileKey);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
            } else {
                //TODO: Error Handling...
                Log.i(TAG, "Got Error response while uploading file : " + connection.getResponseCode());
                return;
            }
            File file = FileClientService.getFilePath(fileMeta.getName(), context, contentType);
            OutputStream output = new FileOutputStream(file);
            byte data[] = new byte[1024];
            int totalSize = fileMeta.getSize();
            int progressCount = 0;
            int count = 0;
            while ((count = inputStream.read(data)) != -1) {
                output.write(data, 0, count);
                progressCount = progressCount + count;
                android.os.Message msg = new android.os.Message();
                int percentage = progressCount * 100 / totalSize;
                //TODO: pecentage should be transfer via handler
                //Message code 2 represents image is successfully downloaded....
                if ((percentage % 10 == 0)) {
                    msg.what = 1;
                    msg.obj = this;
                }
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
            }
            output.flush();
            output.close();
            inputStream.close();

            //Todo: Fix this, so that attach package can be moved to mobicom mobicom.
            new MessageDatabaseService(context).updateInternalFilePath(message.getKeyString(), file.getAbsolutePath());

            ArrayList<String> arrayList = new ArrayList<String>();
            arrayList.add(file.getAbsolutePath());
            message.setFilePaths(arrayList);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            Log.e(TAG, "File not found on server");
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "Exception fetching file from server");
        }

    }

    /**
     * An interface that defines methods that PhotoTask implements. An instance of
     * PhotoTask passes itself to an PhotoDownloadRunnable instance through the
     * PhotoDownloadRunnable constructor, after which the two instances can access each other's
     * variables.
     */
    interface TaskRunnableDownloadMethods {

        /**
         * Sets the Thread that this instance is running on
         *
         * @param currentThread the current Thread
         */
        void setDownloadThread(Thread currentThread);

        /**
         * Defines the actions for each state of the PhotoTask instance.
         *
         * @param state The current state of the task
         */
        void handleDownloadState(int state);

        /**
         * Gets the URL for the image being downloaded
         *
         * @return The image URL
         */
        String getImageURL();

        Message getMessage();

        Context getContext();

        String getContentType();
    }
}

