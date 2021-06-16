package com.cloudappsync.ultra.Utilities;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipManager {

    private static final int BUFFER = 80000;

    public void zip(String[] _files, String zipFileName) {
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte data[] = new byte[BUFFER];

            for (int i = 0; i < _files.length; i++) {
                Log.v("Compress", "Adding: " + _files[i]);
                FileInputStream fi = new FileInputStream(_files[i]);
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unzip(String _zipFile, String _targetLocation, boolean isNew) {

        File destinationDir = new File(_targetLocation);

        ZipFile zip = null;
        try {
            destinationDir.mkdirs();
            zip = new ZipFile(new File(_zipFile));
            Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();
            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = zipFileEntries.nextElement();
                String entryName = entry.getName();
                File destFile = new File(destinationDir, entryName);
                File destinationParent = destFile.getParentFile();
                if (destinationParent != null && !destinationParent.exists()) {
                    destinationParent.mkdirs();
                }
                if (!entry.isDirectory()) {

                    if (!isNew) {

                        if (!entry.getName().equals("configFile.txt")) {
                            BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                            int currentByte;
                            byte data[] = new byte[8192];
                            FileOutputStream fos = new FileOutputStream(destFile);
                            BufferedOutputStream dest = new BufferedOutputStream(fos, 8192);
                            while ((currentByte = is.read(data, 0, 8192)) != -1) {
                                dest.write(data, 0, currentByte);
                            }
                            dest.flush();
                            dest.close();
                            is.close();
                        }

                    } else {

                        BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                        int currentByte;
                        byte data[] = new byte[8192];
                        FileOutputStream fos = new FileOutputStream(destFile);
                        BufferedOutputStream dest = new BufferedOutputStream(fos, 8192);
                        while ((currentByte = is.read(data, 0, 8192)) != -1) {
                            dest.write(data, 0, currentByte);
                        }
                        dest.flush();
                        dest.close();
                        is.close();

                    }
                }
            }
        } catch (Exception e) {
            Log.d("Unzipping", e.getMessage());
        } finally {
            if (zip != null) {
                try {
                    zip.close();

                } catch (IOException ignored) {
                }
            }
        }
    }

    private void dirChecker(String dir) {
        File f = new File(dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

}
