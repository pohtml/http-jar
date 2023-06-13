package com.github.pohtml.annotations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TemporaryFiles {

	File directory;
	
	TemporaryFiles(File zip) {
        try {
        	String name = zip.getName();
        	int index = name.lastIndexOf('.');
        	name = name.substring(0, index);
			directory = new File(zip.getParentFile(), '.' + name);
			directory.mkdir();
	        byte[] buffer = new byte[1024];
	        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zip))) {
		        ZipEntry entry = zis.getNextEntry();
		        while (entry != null) {
		            File newFile = newFile(directory, entry);
		            if (entry.isDirectory()) {
		                if (!newFile.isDirectory() && !newFile.mkdirs()) {
		                    throw new IOException("Failed to create directory " + newFile);
		                }
		            } else {
		                File parent = newFile.getParentFile();
		                if (!parent.isDirectory() && !parent.mkdirs()) {
		                    throw new IOException("Failed to create directory " + parent);
		                }
		                FileOutputStream fos = new FileOutputStream(newFile);
		                int len;
		                while ((len = zis.read(buffer)) > 0) {
		                    fos.write(buffer, 0, len);
		                }
		                fos.close();
		            }
		            newFile.setLastModified(entry.getLastModifiedTime().toMillis());
			        zis.closeEntry();
		            entry = zis.getNextEntry();
		        }	
	        }
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
	    File destFile = new File(destinationDir, zipEntry.getName());
	    String destDirPath = destinationDir.getCanonicalPath();
	    String destFilePath = destFile.getCanonicalPath();
	    if (!destFilePath.startsWith(destDirPath + File.separator)) {
	        throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
	    }
	    return destFile;
	}
	
}