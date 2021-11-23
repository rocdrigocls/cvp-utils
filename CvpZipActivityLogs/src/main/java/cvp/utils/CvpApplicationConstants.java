package cvp.utils;

import java.io.File;
import java.io.FileFilter;

import org.apache.log4j.Logger;

public class CvpApplicationConstants {
	public static Logger logger = Logger
			.getLogger(CvpApplicationConstants.class);

	public static String CVP_APPLICATION_LOG_DIR = "logs";

	public static FileFilter CVP_APPLICATION_LOG_DIR_FILTER = new FileFilter() {
		public boolean accept(File pathname) {
			if (pathname.getName().equalsIgnoreCase(CVP_APPLICATION_LOG_DIR)) {
				logger.warn("Filtering out " + pathname);
				return false;
			}
			return true;
		}
	};
}
