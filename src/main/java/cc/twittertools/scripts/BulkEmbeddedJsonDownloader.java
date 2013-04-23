package cc.twittertools.scripts;

import static cc.twittertools.download.AsyncEmbeddedJsonStatusBlockCrawler.DATA_OPTION;
import static cc.twittertools.download.AsyncEmbeddedJsonStatusBlockCrawler.NOFOLLOW_OPTION;
import static cc.twittertools.download.AsyncEmbeddedJsonStatusBlockCrawler.OUTPUT_OPTION;
import static cc.twittertools.download.AsyncEmbeddedJsonStatusBlockCrawler.REPAIR_OPTION;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileLock;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import cc.twittertools.corpus.demo.ReadStatuses;
import cc.twittertools.download.AsyncEmbeddedJsonStatusBlockCrawler;

/**
 * Given a list of ID files, and a number of tasks, starts downloading
 * simultaneously from the files.
 * @author bfeeney
 *
 */
public class BulkEmbeddedJsonDownloader 
{
	private final static Logger LOG = Logger.getLogger(BulkEmbeddedJsonDownloader.class);
	
	private final static File LOCK_FILE            = new File ("/home/bfeeney/trec.lock.file");
	private final static File STARTED_TASKS_FILE   = new File ("/home/bfeeney/trec.running.tasks");
	private final static File COMPLETED_TASKS_FILE = new File ("/home/bfeeney/trec.completed.tasks");
	
	/**
	 * Locks the lock-file, then reads in the task status file. If the given
	 * input file already exists in the task-status file, returns false. If
	 * the given file doesn't exist, it's added to the task status file, and
	 * we return true.
	 * @param inpath path of the input file, which should serve as a
	 * unique ID for the job
	 * @return true if the job should be undertaken, false if the job should
	 * be abandoned (on the premise that it's already done/in-progress)
	 */
	private final static boolean shouldStart (String inpath) throws Exception
	{	synchronized (BulkEmbeddedJsonDownloader.class) {
			inpath = inpath.toUpperCase();
		
			FileOutputStream out = new FileOutputStream(LOCK_FILE, /* append = */ true);
			try 
			{	FileLock lock = null;
				try
				{	lock = out.getChannel().lock();
					List<String> lines = Files.readLines(STARTED_TASKS_FILE, Charsets.UTF_8);
					if (! lines.contains(inpath))
					{	lines.add(inpath);
						Files.write (Joiner.on('\n').join(lines), STARTED_TASKS_FILE, Charsets.UTF_8);
						return true;
					}
				}
				catch (Exception e)
				{	LOG.error("Can't acquire lock on lock-file " + LOCK_FILE + " or can't access running tasks file " + STARTED_TASKS_FILE + " : " + e.getMessage(), e);
					throw e;
				}
			    finally
			    {	if (lock != null)
			    		lock.release();
			    }
			}
			finally
			{	out.close();
			}
			return false;
		}
	}
	
	/**
	 * Locks the lock-file, then records that this task has been completed
	 * @param inpath path of the input file, which should serve as a
	 * unique ID for the job
	 * @return true if the job should be undertaken, false if the job should
	 * be abandoned (on the premise that it's already done/in-progress)
	 */
	private final static void markCompleted (String inpath, int crawlSize) throws Exception
	{	synchronized (BulkEmbeddedJsonDownloader.class) {
			inpath = inpath.toUpperCase();
		
			FileOutputStream out = new FileOutputStream(LOCK_FILE);
			try 
			{	FileLock lock = null;
				try
				{	lock = out.getChannel().lock();
					List<String> lines = Files.readLines(COMPLETED_TASKS_FILE, Charsets.UTF_8);
					lines.add(inpath + '\t' + crawlSize);
					Files.write (Joiner.on('\n').join(lines), COMPLETED_TASKS_FILE, Charsets.UTF_8);
				}
				catch (Exception e)
				{	LOG.error("Can't acquire lock on lock-file " + LOCK_FILE + " or can't access completed tasks file " + COMPLETED_TASKS_FILE + " : " + e.getMessage(), e);
					throw e;
				}
			    finally
			    {	if (lock != null)
			    		lock.release();
			    }
			}
			finally
			{	out.close();
			}
		}
	}
	
	
	
	private final static class DownloadTask implements Callable<Integer>
	{	public final String inpath;
		public final String outpath;
		
		public DownloadTask(String inpath, String outpath) {
			super();
			this.inpath = inpath;
			this.outpath = outpath;
		}



		@Override
		public Integer call() throws Exception
		{	if (! shouldStart (inpath))
			{	LOG.info ("Skipping job as it's already been started - " + inpath);
				return 0;
			}
			
			int completedCount = 0;
			try
			{	
				completedCount =
					new AsyncEmbeddedJsonStatusBlockCrawler(
						new File(inpath),
						outpath,
						/* repair = */   null, 
						/* noFollow = */ false
					).fetch();
				
			}
			catch (Exception e)
			{	LOG.error ("Failed to download files for task " + inpath + " : " + e.getMessage(), e);
				return 0;
			}

			LOG.info("Downloaded " + completedCount + " files from " + inpath);
			markCompleted (inpath, completedCount);
			return completedCount;
		}
	}
	
	
	
    public static final String TASK_COUNT_OPTION = "taskcount";
	
	@SuppressWarnings("static-access")
    public static void main(String[] args) throws Exception {
		Files.touch (LOCK_FILE);
		Files.touch (STARTED_TASKS_FILE);
		Files.touch (COMPLETED_TASKS_FILE);
		
        Options options = new Options();
        options.addOption(OptionBuilder.withArgName("path").hasArg()
                .withDescription("data file with tweet ids").create(DATA_OPTION));
        options.addOption(OptionBuilder.withArgName("path").hasArg()
                .withDescription("output file (*.gz)").create(OUTPUT_OPTION));
        options.addOption(OptionBuilder.withArgName("path").hasArg()
                .withDescription("output repair file (can be used later as a data file)").create(REPAIR_OPTION));
        options.addOption(OptionBuilder.withArgName("taskcount").hasArg()
                .withDescription("number of simultaneous downloads to attempt").create(TASK_COUNT_OPTION));
        options.addOption(NOFOLLOW_OPTION, NOFOLLOW_OPTION, false, "don't follow 301 redirects");

        CommandLine cmdline = null;
        CommandLineParser parser = new GnuParser();
        try {
            cmdline = parser.parse(options, args);
        } catch (ParseException exp) {
            System.err.println("Error parsing command line: " + exp.getMessage());
            System.exit(-1);
        }

        if (!cmdline.hasOption(DATA_OPTION) || !cmdline.hasOption(OUTPUT_OPTION)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(ReadStatuses.class.getName(), options);
            System.exit(-1);
        }

        String data      = cmdline.getOptionValue(DATA_OPTION);
        String output    = cmdline.getOptionValue(OUTPUT_OPTION);
        String repair    = cmdline.getOptionValue(REPAIR_OPTION);
        boolean noFollow = cmdline.hasOption(NOFOLLOW_OPTION);
        int taskCount    = Integer.parseInt (cmdline.getOptionValue(TASK_COUNT_OPTION));
        
        ExecutorService executor = Executors.newFixedThreadPool(taskCount);
        int DEBUG_MAX_TASKS = 2;
        
        String[] inFiles = new File (data).list();
        Arrays.sort (inFiles);
        for (String inFile : inFiles)
        {	if (DEBUG_MAX_TASKS-- == 0)
        		break;
        
        	String outFile = 
        		output + File.separator
        		+ new File (inFile).getName().replaceAll ("\\.dat", ".json.gz");
        	
        	inFile = data + File.separator + inFile;
        	executor.submit(new DownloadTask(inFile, outFile));
        }
        
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.DAYS);
    }

}
