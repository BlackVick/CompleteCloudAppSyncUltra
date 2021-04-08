package com.cloudappsync.ultra.Utilities;

import java.io.File;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 * This utility class provides a method that downloads a structure
 * of a directory (excluding files) from a FTP server, using
 * Apache Commons Net API.
 *
 * @author www.codejava.net
 *
 */
public class FTPDownloadUtil {

	/**
	 * Download structure of a directory from a FTP server.
	 * @param ftpClient an instance of org.apache.commons.net.ftp.FTPClient class.
	 * @param parentDir Path of the parent directory of the current directory being
	 * downloaded.
	 * @param currentDir Path of the current directory being downloaded.
	 * @param saveDir path of directory where the whole remote directory will be
	 * downloaded and saved.
	 * @throws IOException if any network or IO error occurred.
	 */
	public static void downloadDirStructure(FTPClient ftpClient, String parentDir,
			String currentDir, String saveDir) throws IOException {
		String dirToList = parentDir;
		if (!currentDir.equals("")) {
			dirToList += "/" + currentDir;
		}

		FTPFile[] subFiles = ftpClient.listFiles(dirToList);

		if (subFiles != null && subFiles.length > 0) {
			for (FTPFile aFile : subFiles) {
				String currentFileName = aFile.getName();
				if (currentFileName.equals(".") || currentFileName.equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}

				if (aFile.isDirectory()) {
					String newDirPath = saveDir + parentDir + File.separator
						+ currentDir + File.separator + currentFileName;
					if (currentDir.equals("")) {
						newDirPath = saveDir + parentDir + File.separator
						+ currentFileName;
					}

					// create the directory in saveDir
					File newDir = new File(newDirPath);
					boolean created = newDir.mkdirs();
					if (created) {
						System.out.println("CREATED the directory: " + newDirPath);
					} else {
						System.out.println("COULD NOT create the directory: " + newDirPath);
					}

					// download the sub directory
					downloadDirStructure(ftpClient, dirToList, currentFileName,
							saveDir);
				}
			}
		}
	}
}