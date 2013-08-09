package cc.twittertools.post;

/**
 * Extracts paired features from tweets: one references the "text", one references the "event"
 * 
 * The text features will include normal words, and optionally addressees and hash-tags. Hashtags
 * can be encoded as normal words (i.e. with the hash marker removed) or as special words (i.e
 * retaining the hash-marker).
 * 
 * The event features contain the user ID, the post date, and optionally whether it was a retweet,
 * the addressee identifier and [TODO the list of people the user follows.]
 * @author bryanfeeney
 *
 */
public class TweetFeatureExtractor
{
  /** Remove all addressee tokens. Otherwise they get encoded in the BOW vector like any word (albeit with the addressee marker still included) */
  private final boolean stripAddresseesFromText = false;
  
  /** */
  private final boolean stripHashTagsFromText   = false;
  private final boolean stripRtMarkersFromText  = false;
  private final boolean stripRetweets           = false;
  private final boolean treatHashTagsAsWords    = false;
}
