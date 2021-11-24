package cvp.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import base.fileUtils.FileUtils;

public class CompactCvpApplicationLog {

	public static void main(String... args) {
		initFromArgs(args);
	}

	private static final Logger logger = Logger
			.getLogger(CompactCvpApplicationLog.class);

	private static final DateFormat isoFormat = new SimpleDateFormat(
			"yyyy-MM-dd hh:mm:ss.SSS");

	private static void initFromArgs(String... args) {
		if (args.length < 1) {
			logger.error("Insuficient params");
			System.out.println("Insuficient params");
			System.out
					.println("Required params: \r\n\tProperties file path: Properties file that contains the configuration. ");
			System.out.println("\r\nSample properties file:\r\n");
			try {
				FileUtils.writeFileToStream(CompactCvpApplicationLog.class
						.getResourceAsStream("configSample.properties"),
						System.out);
			} catch (IOException e) {
				logger.error(e);
			}
		} else {
			logger.info("CompactCvpApplicationLog from MAIN start");
			String propertiesPath = args[0];
			File propertiesFile = new File(propertiesPath);
			if (propertiesFile.exists()) {
				Properties propeties = new Properties();
				try {
					logger.info("Loading properties from " + propertiesPath);
					propeties.load(new FileInputStream(propertiesFile));
				} catch (Exception e) {
					logger.error(e);
					return;
				}
				try {
					compactCvpApplicationsLog(propeties);
				} catch (Exception e) {
					logger.error(e);
				}
			} else {
				logger.error("File " + propertiesPath + " does not exists");
			}
		}
	}

	public static void compactCvpApplicationsLog(Properties properties)
			throws Exception {
		logger.info("CompactCvpApplicationLog start");
		try {
			logger.debug("Properties: ");
			for (Entry<Object, Object> property : properties.entrySet()) {
				logger.debug("\t" + property.getKey() + ": "
						+ property.getValue());
			}
			String rootPath = properties.getProperty("config.cvpAppsRootPath");
			String patternForNameOfLogFiles = properties
					.getProperty("logFile.namePattern");
			String patternOfDateInLogEntry = properties
					.getProperty("logFile.logEntry.date.pattern");
			String logEntryDateFormat = properties
					.getProperty("logFile.logEntry.date.format");
			Integer logEntryDateGroupMatch = Integer.parseInt(properties
					.getProperty("logFile.logEntry.date.pattern.group"));
			DateFormat logEntryDateParser = new SimpleDateFormat(
					logEntryDateFormat);
			final Pattern logFileNamePattern = Pattern
					.compile(patternForNameOfLogFiles);
			final Pattern logEntryDatePattern = Pattern
					.compile(patternOfDateInLogEntry);
			int limitDays = Integer
					.parseInt(properties
							.getProperty("config.daysThatHaveToPassBeforeLogBecomeObsolete"));
			Calendar limitDate = Calendar.getInstance();
			logger.info("Actual date: " + isoFormat.format(limitDate.getTime()));
			logger.info("Days that have to pass before log becomes obsolete: "
					+ limitDays);
			limitDate.set(Calendar.HOUR_OF_DAY, 0);
			limitDate.set(Calendar.MINUTE, 0);
			limitDate.set(Calendar.SECOND, 0);
			limitDate.set(Calendar.MILLISECOND, 0);
			limitDate.add(Calendar.MILLISECOND, -1);
			limitDate.add(Calendar.DAY_OF_MONTH, -limitDays);
			logger.info("Date limit: " + isoFormat.format(limitDate.getTime()));
			List<File> apps = null;
			logger.info("Looking for CVP apps in: " + rootPath);
			apps = getCvpAppsInPath(rootPath);
			logger.info(apps.size() + " app(s) found");
			logger.debug("Apps paths " + apps);
			FileFilter logFileFilter = new FileFilter() {
				public boolean accept(File pathname) {
					if (logFileNamePattern.matcher(pathname.getName())
							.matches()) {
						return true;
					}
					return false;
				}
			};
			for (File app : apps) {
				logger.info("Looking for log files in app " + app.getName());
				List<File> logFiles = getLogFiles(app, logFileFilter);
				logger.info(logFiles.size() + " log file(s) found");
				logger.debug("Log files paths: " + logFiles);
				logger.info("Filtering old files to zip");
				List<File> filesToZip = getLogFilesOlderThan(logFiles,
						logEntryDatePattern, logEntryDateGroupMatch,
						logEntryDateParser, limitDate);
				logger.info(filesToZip.size() + " file(s) to zip");
				logger.debug("Log files to zip: " + filesToZip);
				logger.info("Compressing files");
				for (File fileToZip : filesToZip) {
					boolean successfullyZipped = FileUtils.zipFile(fileToZip);
					if (successfullyZipped) {
						logger.debug("File compression was successful, deleting it");
						boolean successfullyDeleted = fileToZip.delete();
						if (!successfullyDeleted) {
							logger.warn("Could not delete file, requesting to delete it on JVM exit");
							fileToZip.deleteOnExit();
						}
					} else {
						logger.debug("File compression failed, will not perform any other action on it");
					}
				}
			}
		} finally {
			logger.info("Process finished");
		}
	}

	private static List<File> getLogFilesOlderThan(List<File> logFiles,
			Pattern logFileEntryDatePattern, int datePatternGroup,
			DateFormat dateParser, Calendar calendar) {
		List<File> oldFiles = new ArrayList<File>();
		for (File logFile : logFiles) {
			String lastLine = FileUtils.readLastLineOfTextFile(logFile);
			Matcher logEntryDateMatcher = logFileEntryDatePattern
					.matcher(lastLine);
			if (logEntryDateMatcher.find()) {
				String logEntryDateText = logEntryDateMatcher
						.group(datePatternGroup);
				try {
					Date logEntryDate = dateParser.parse(logEntryDateText);
					logger.debug("Last entry in file " + logFile.getName()
							+ " is dated at: " + isoFormat.format(logEntryDate));
					if (logEntryDate.before(calendar.getTime())) {
						logger.debug("Adding file to old list");
						oldFiles.add(logFile);
					}
				} catch (ParseException e) {
					logger.error(e);
				}
			} else {
				logger.debug("[Entry not found LastLine]" + logFile.getName() + "=>" + lastLine);
			}
		}
		return oldFiles;
	}

	private static List<File> getLogFiles(File app, FileFilter logFilefilter) {
		File logDir = getCvpLogDir(app);
		List<File> logFiles = new ArrayList<File>();
		for (File file : logDir.listFiles()) {
			if (file.isDirectory()) {
				logFiles.addAll(Arrays.asList(file.listFiles(logFilefilter)));
			} else {
				logFiles.add(file);
			}
		}
		return logFiles;
	}

	private static File getCvpLogDir(File app) {
		File logs = new File(app,
				CvpApplicationConstants.CVP_APPLICATION_LOG_DIR);
		return logs;
	}

	public static List<File> getCvpAppsInPath(String path) throws Exception {
		File root = new File(path);
		if (root.exists()) {
			if (root.isDirectory()) {
				return getCvpAppsInPath(root);
			} else {
				throw new Exception("Path " + path + " is not a directory");
			}
		} else {
			throw new FileNotFoundException("Path " + path + " does not exists");
		}
	}

	public static List<File> getCvpAppsInPath(File path) {
		List<File> cvpApps = new ArrayList<File>();
		for (File file : path.listFiles()) {
			if (file.isDirectory() && hasLogsDirectory(file)) {
				cvpApps.add(file);
			}
		}
		return cvpApps;
	}

	private static boolean hasLogsDirectory(File file) {
		File logs = getCvpLogDir(file);
		if (logs.exists() && logs.isDirectory()) {
			logger.debug(file + " is a CVP Application");
			return true;
		}
		logger.debug(file + " is NOT a CVP Application");
		return false;
	}

}
