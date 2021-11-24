package base.fileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

public class FileUtils {

	private static final Logger logger = Logger.getLogger(FileUtils.class);

	public static List<String> scanFile(String file)
			throws FileNotFoundException {
		return scanFile(new File(file));
	}

	public static void appendLineToFile(File file, String line) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(file, true)));
			out.println(line);
			out.close();
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public static void writeFileToStream(InputStream inputStream,
			OutputStream out) throws IOException {
		InputStream input = new BufferedInputStream(inputStream);
		byte[] buffer = new byte[8192];

		try {
			for (int length = 0; (length = input.read(buffer)) != -1;) {
				out.write(buffer, 0, length);
			}
		} finally {
			input.close();
		}
	}

	public static String readLastLineOfTextFile(File file) {
		RandomAccessFile fileHandler = null;
		try {
			fileHandler = new RandomAccessFile(file, "r");
			long fileLength = fileHandler.length() - 1;
			StringBuilder sb = new StringBuilder();

			for (long filePointer = fileLength; filePointer != -1; filePointer--) {
				fileHandler.seek(filePointer);
				int readByte = fileHandler.readByte();

				if (readByte == 0xA) {
					if (filePointer == fileLength) {
						continue;
					} else {
						break;
					}
				} else if (readByte == 0xD) {
					if (filePointer == fileLength - 1) {
						continue;
					} else {
						break;
					}
				}
				sb.append((char) readByte);
			}
			String lastLine = sb.reverse().toString();
			return lastLine;
		} catch (java.io.FileNotFoundException e) {
			logger.error(e);
			return null;
		} catch (java.io.IOException e) {
			logger.error(e);
			return null;
		} finally {
			if (fileHandler != null)
				try {
					fileHandler.close();
				} catch (IOException e) {
					logger.error(e);
				}
		}
	}

	public static boolean zipFile(File fileToZip) {
		String oldFileName = fileToZip.getName();
		//int extensionPosition = oldFileName.lastIndexOf(".");
		int extensionPosition = -1;
		String oldFIleNameWithoutExtension = null;
		if (extensionPosition > 1) {
			oldFIleNameWithoutExtension = oldFileName.substring(0,
					extensionPosition);
		} else if (extensionPosition > 0) {
			oldFIleNameWithoutExtension = oldFileName
					.substring(extensionPosition + 1);
		} else {
			oldFIleNameWithoutExtension = oldFileName;
		}
		File newZipFile = new File(fileToZip.getParentFile(),
				oldFIleNameWithoutExtension + ".zip");
		logger.debug("Compressing file " + fileToZip.getName() + " into "
				+ newZipFile);
		byte[] buffer = new byte[1024];
		FileInputStream in = null;
		ZipOutputStream zos = null;
		boolean result = true;
		try {
			FileOutputStream fos = new FileOutputStream(newZipFile);
			zos = new ZipOutputStream(fos);
			ZipEntry ze = new ZipEntry(fileToZip.getName());
			zos.putNextEntry(ze);
			in = new FileInputStream(fileToZip);
			int len;
			while ((len = in.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}
		} catch (IOException ex) {
			logger.error(ex);
			result = false;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					result = false;
					logger.error(e);
				}
			}
			if (zos != null) {
				try {
					zos.closeEntry();
					zos.close();
				} catch (IOException e) {
					result = false;
					logger.error(e);
				}
			}
		}
		return result;
	}

	public static void copyFileUsingRelativePath(File sourceRoot,
			String relativeFilePath, File destinationRoot,
			List<String> directoryNames) {
		logger.debug("Copying file '" + relativeFilePath + "' for directories "
				+ directoryNames);
		InputStream is = null;
		List<OutputStream> listOfOutputStreams = new ArrayList<OutputStream>();
		File source = new File(sourceRoot, relativeFilePath);
		List<File> destinations = new ArrayList<File>();
		for (String appName : directoryNames) {
			File appPath = new File(destinationRoot, appName);
			File destinationFile = new File(appPath, relativeFilePath);
			destinations.add(destinationFile);
		}
		try {
			for (File file : destinations) {
				file.getParentFile().mkdirs();
				listOfOutputStreams.add(new FileOutputStream(file));
			}
			if (listOfOutputStreams.isEmpty()) {
				logger.warn("No files copied");
				return;
			}
			is = new FileInputStream(source);

			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				for (OutputStream os : listOfOutputStreams) {
					os.write(buffer, 0, length);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				logger.error(e);
			}
			for (OutputStream os : listOfOutputStreams) {
				try {
					if (os != null) {
						os.close();
					}
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
		logger.debug("File copied into " + destinations);
	}

	public static List<String> scanFile(File fileWhereNewNamesAre)
			throws FileNotFoundException {
		FileInputStream fis = null;
		List<String> newAppNames = new ArrayList<String>();
		logger.info("Reading file " + fileWhereNewNamesAre);
		fis = new FileInputStream(fileWhereNewNamesAre);
		logger.info("Scanning file for valid entries");
		Scanner scanner = new Scanner(fis);
		while (scanner.hasNext()) {
			newAppNames.add(scanner.next());
		}
		scanner.close();
		return newAppNames;
	}

	public static void copyFilesInFolder(String sourcePath,
			String destinationPath, String directoriesFile, FileFilter filter) {
		List<String> newAppNames;
		List<String> relativePathsOfFilesToCopy;
		logger.info("Looking if source path exists and is a folder");
		File sourceFolder = new File(sourcePath);
		if (sourceFolder.exists()) {
			if (sourceFolder.isDirectory()) {
				logger.debug("Source path '" + sourcePath + "' is valid");
				logger.info("Looking if destination path exists and is a folder");
				File destinationFolder = new File(destinationPath);
				if (destinationFolder.exists()) {
					if (destinationFolder.isDirectory()) {
						logger.debug("Destination path '" + destinationPath
								+ "' is valid");
						logger.info("Looking if the file that contains the new apps names exists");
						File fileWhereNewNamesAre = new File(directoriesFile);
						if (fileWhereNewNamesAre.exists()) {
							logger.debug("File with new app names '"
									+ directoriesFile + "' is valid");
							try {
								newAppNames = FileUtils
										.scanFile(fileWhereNewNamesAre);
							} catch (FileNotFoundException e) {
								logger.error(e);
								return;
							}
							logger.info("New app names: "
									+ newAppNames.toString());
							logger.info("Generating list of files to copy");
							relativePathsOfFilesToCopy = FilesInFolderRelativeToAPath
									.getFiles(sourceFolder, sourceFolder,
											filter);
							logger.info(relativePathsOfFilesToCopy.size()
									+ " files to copy");
							logger.debug("Files to copy: "
									+ relativePathsOfFilesToCopy.toString());
							logger.info("Copying files...");
							for (String relativeFilePath : relativePathsOfFilesToCopy) {
								FileUtils.copyFileUsingRelativePath(
										sourceFolder, relativeFilePath,
										destinationFolder, newAppNames);
							}
							logger.info("Done");
						} else {
							logger.error("File with new app names '"
									+ directoriesFile + "' does not exists");
						}
					} else {
						logger.error("Destination path '" + destinationPath
								+ "' is not a directoty");
					}
				} else {
					logger.error("Destination path '" + destinationPath
							+ "' does not exists");
				}
			} else {
				logger.error("Source path '" + sourcePath
						+ "' is not a directoty");
			}
		} else {
			logger.error("Source path '" + sourcePath + "' does not exists");
		}
	}

}
