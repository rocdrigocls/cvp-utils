package base.fileUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class FilesInFolderRelativeToAPath {

	public static List<String> getFiles(File folderPath, File relativeToPath,
			FileFilter filter) {
		List<String> result = new ArrayList<String>();
		if (folderPath.isDirectory()) {
			for (File file : folderPath.listFiles(filter)) {
				result.addAll(getFiles(file, relativeToPath, filter));
			}
		} else {
			String relative = relativeToPath.toURI()
					.relativize(folderPath.toURI()).getPath();
			result.add(relative);
		}
		return result;
	}

	private File folderPath;
	private File relativeToPath;

	public FilesInFolderRelativeToAPath(File folderPath, File relativeToPath) {
		this.folderPath = folderPath;
		this.relativeToPath = relativeToPath;
	}

	public List<String> getFiles(FileFilter filter) {
		return FilesInFolderRelativeToAPath.getFiles(folderPath,
				relativeToPath, filter);
	}

}
