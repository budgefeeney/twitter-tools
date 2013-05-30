package cc.twittertools.spider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.common.base.Charsets;

/**
 * First run of the ranker wrote out the date in the wrong format,
 * and wrongly calculated the user age, thereby getting the sort
 * order wrong.
 * @author bfeeney
 *
 */
public class RankerBugFix
{
  private final static int MONTHS_PER_YEAR = 12;
  
  public static void main(String[] args) throws IOException
  { final DateTime NOW = new DateTime();
    DateTimeFormatter inputFmt  = ISODateTimeFormat.basicDateTime();
    DateTimeFormatter outputFmt = ISODateTimeFormat.dateTimeNoMillis();
    
    Path inputPath  = Paths.get (args.length > 0 ? args[0] : "");
    Path outputPath = Paths.get (args.length > 1 ? args[1] : "");
    
    List<TwitterUser> users = new ArrayList<TwitterUser>(40000);
    
    try (
      BufferedReader rdr = Files.newBufferedReader(inputPath, Charsets.UTF_8);
    )
    {
      String line = null;
      while ((line = rdr.readLine()) != null)
      { if ((line = line.trim()).isEmpty())
          continue;
        
        String[] fields = StringUtils.split(line, '\t');
        
        DateTime creationDate = inputFmt.parseDateTime(fields[2]);
        fields[2] = outputFmt.print(creationDate);
        
        Period age = new Period(creationDate, NOW);
        int ageMonths = age.getYears() * MONTHS_PER_YEAR + age.getMonths();
        
        fields[3] = String.valueOf(ageMonths);
        
        users.add (new TwitterUser (StringUtils.join(fields, '\t')));
      }
    }
    
    Collections.sort(users);
    try (
      BufferedWriter wtr = Files.newBufferedWriter(outputPath, Charsets.UTF_8);
    )
    { for (TwitterUser user : users)
        wtr.write(user.toTabDelimLine());
    }
  }
}
