/**
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aiwatch.cloud.gdrive;

import com.aiwatch.Logger;
import com.google.android.gms.tasks.Task;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
public class GDriveServiceHelper {
    private static final Logger LOGGER = new Logger();
    private final Drive mDriveService;
    public static final String TYPE_AUDIO = "application/vnd.google-apps.audio";
    public static final String TYPE_GOOGLE_DRIVE_FILE = "application/vnd.google-apps.file";
    public static final String TYPE_GOOGLE_DRIVE_FOLDER = "application/vnd.google-apps.folder";
    public static final String TYPE_PHOTO = "application/vnd.google-apps.photo";
    public static final String TYPE_UNKNOWN = "application/vnd.google-apps.unknown";
    public static final String TYPE_VIDEO = "application/vnd.google-apps.video";
    public static final String APP_FOLDER_NAME = "aiwatch";

    public GDriveServiceHelper(Drive driveService) {
        mDriveService = driveService;
    }

    public String createFolder(String folderName, String parentFolderId, boolean isAppFolder) {
        List<String> parent;
        String folderId;
        LOGGER.d("Ready to create folder " + folderName);
        if (parentFolderId == null) {
            String parentFolderName = isAppFolder ? "root" : APP_FOLDER_NAME;
            parent = Collections.singletonList(parentFolderName);
            folderId = getFolderIdIfExists(folderName, parentFolderName);;
        } else {
            parent = Collections.singletonList(parentFolderId);
            folderId = getFolderIdIfExists(folderName, parentFolderId);
        }
        if(folderId != null){
            LOGGER.d("Found existing folder " + folderName);
            return folderId;
        }
        File metadata = new File()
                .setParents(parent)
                .setMimeType(TYPE_GOOGLE_DRIVE_FOLDER)
                .setName(folderName);
        try{
            File googleFile = mDriveService.files().create(metadata).setFields("id, parents").execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }
            folderId = googleFile.getId();
        }catch(IOException ioe){
            //ignore exception if folder already exists
            LOGGER.e(ioe, "error creating folder ");
        }
        return folderId;
    }

    public String getFolderIdIfExists(final String folderName, final String parentFolderId) {
        String folderId = null;
        try{
            String query = "mimeType = '" + TYPE_GOOGLE_DRIVE_FOLDER + "' and name = '" + folderName + "' and trashed = false";
            LOGGER.d("gdrive query used "+query);
            if(parentFolderId != null && !parentFolderId.equals("root")){
                query = query + " and '" + parentFolderId + "' in parents";
            }
            FileList files = mDriveService.files().list().setQ(query).execute();
            if (files != null && !files.isEmpty() && files.getFiles().size() > 0) {
                LOGGER.d("Google drive aiwatch folder found");
                folderId = files.getFiles().get(0).getId();
            }
        }catch(Exception e){
            LOGGER.e(e, "Exception getting folderId ");
        }
        return folderId;
    }

    public File uploadFile(String fileName, String parentFolderId, String filePath, String mimeType) throws IOException {
            java.io.File mediaFile = new java.io.File(filePath);
            File metadata = new File()
                .setParents(Collections.singletonList(parentFolderId))
                .setMimeType(mimeType)
                .setName(fileName);

            InputStreamContent mediaContent =
                    new InputStreamContent(mimeType,
                            new BufferedInputStream(new FileInputStream(mediaFile)));
            mediaContent.setLength(mediaFile.length());
            File fileMeta = mDriveService.files().create(metadata, mediaContent).execute();
            return fileMeta;
    }

    public void downloadFile(String fileId, String outputFilePath) throws IOException {
        if(fileId != null){
            OutputStream out = new FileOutputStream(outputFilePath);
            mDriveService.files().get(fileId)
                    .executeMediaAndDownloadTo(out);
        }
    }

    public Task<Void> deleteFolderFile(String fileId) throws IOException {
        // Retrieve the metadata as a File object.
        if (fileId != null) {
            mDriveService.files().delete(fileId).execute();
        }
        return null;
    }
}