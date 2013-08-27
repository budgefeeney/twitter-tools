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
	private DirectoryStream<Path> folders;
	private DirectoryStream<Path> filesInCurrentFolder;
	
	private Iterator<Path> foldersIter;
	private Iterator<Path> filesInCurrentFolderIter;
	
	private Exception error;

	public FilesInFoldersIterator(Path folderOfFolders) throws IOException
	{	folders = Files.newDirectoryStream(folderOfFolders, new DirectoryStream.Filter<Path>()
		{ @Override public boolean accept(Path path) throws IOException
			{	return Files.isDirectory(path);
			}
		});
		foldersIter = folders.iterator();
		filesInCurrentFolderIter = nextFolder();
	}
	
	public boolean hasNext()
	{	try
		{	while (filesInCurrentFolderIter != null && ! filesInCurrentFolderIter.hasNext())
			{	filesInCurrentFolderIter = nextFolder();
			}
		
			return filesInCurrentFolderIter != null; // the lopp above implies hasNext = true
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
		return filesInCurrentFolderIter.next();
	}

	public void remove()
	{	throw new UnsupportedOperationException();
	}
  
	private Iterator<Path> nextFolder() throws IOException
	{	if (! foldersIter.hasNext())
			return null;
	
		Path folder = foldersIter.next();
		if (filesInCurrentFolder != null)
			filesInCurrentFolder.close();
		
		filesInCurrentFolder = Files.newDirectoryStream(folder, new DirectoryStream.Filter<Path>()
		{	@Override public boolean accept(Path path) throws IOException
			{	return ! Files.isDirectory(path);
			}
		});
		return filesInCurrentFolder.iterator();
	}
	
	public void close() throws Exception
	{	folders.close();
		if (filesInCurrentFolder != null)
			filesInCurrentFolder.close();
	}
}