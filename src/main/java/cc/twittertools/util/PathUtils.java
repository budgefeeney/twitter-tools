package cc.twittertools.util;

import java.nio.file.Path;

/**
 * A grab-bag of utilities for manipulating paths
 */
public class PathUtils
{

	private PathUtils() { } 
	
	/**
	 * Assuming the given path is meant to resolve to a file, creates a new path
	 * which resolves to a file with the same name except that a suffix has been
	 * added, so "/home/bfeeney/dat", ".txt" becomes "/home/bfeeney/dat.txt"
	 */
	public static Path appendFileNameSuffix(Path path, String suffix) {
		return path.getParent().resolve(path.getFileName().toString() + suffix);
	}
}
