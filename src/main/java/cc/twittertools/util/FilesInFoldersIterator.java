package cc.twittertools.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * Iterates over the downloaded tweet files. Recall we have a folder full of folders
 * (and unrelated files), and in each of these folders are files, *ALL* of which are
 * tweet files	
 */
public final class FilesInFoldersIterator implements Iterator<Path>, AutoCloseable
{
	private DirectoryStream<Path> foldersStream;
	private DirectoryStream<Path> folderFilesStream;
	
	private Iterator<Path> foldersIter;
	private Iterator<Path> folderFilesIter;
	
	private Exception error;

	public FilesInFoldersIterator(Path pathToFolderOfFolders) throws IOException
	{	foldersStream = Files.newDirectoryStream(
			pathToFolderOfFolders, 
			new DirectoryStream.Filter<Path>()
			{ @Override public boolean accept(Path path) throws IOException
				{	return Files.isDirectory(path);
				}
			}
		);
		foldersIter = foldersStream.iterator();
		folderFilesIter = nextFolderFilesIter();
	}
	
	public boolean hasNext()
	{	try
		{	while (folderFilesIter != null && ! folderFilesIter.hasNext())
			{	folderFilesIter = nextFolderFilesIter();
			}
		
			return folderFilesIter != null; // the loop above implies hasNext = true
		}
		catch (Exception e)
		{	error = e;
			return true; // we want the exception to be thrown on the call to next
		}
	}

	public Path next()
	{	if (error != null)
		{	RuntimeException runError = new RuntimeException ("Error occurred while determining the next file to read : " + error.getMessage(), error);
			error = null;
			throw runError;
		}
		return folderFilesIter.next();
	}

	public void remove()
	{	throw new UnsupportedOperationException();
	}
  
	/**
	 * Returns an iterator over the files in the next folder (according to
	 * its iterator), or null if there are no more folders whose contents
	 * need listing.
	 */
	private Iterator<Path> nextFolderFilesIter() throws IOException
	{	if (! foldersIter.hasNext())
			return null;
	
		Path folder = foldersIter.next();
		if (folderFilesStream != null)
			folderFilesStream.close();
		
		folderFilesStream = Files.newDirectoryStream(folder, new DirectoryStream.Filter<Path>()
		{	@Override public boolean accept(Path path) throws IOException
			{	return ! Files.isDirectory(path);
			}
		});
		return folderFilesStream.iterator();
	}
	
	public void close() throws Exception
	{	foldersStream.close();
		if (folderFilesStream != null)
			folderFilesStream.close();
	}
}