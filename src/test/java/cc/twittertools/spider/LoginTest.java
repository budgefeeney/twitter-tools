package cc.twittertools.spider;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cc.twittertools.util.FutureImpl;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;
import com.ning.http.client.extra.ThrottleRequestFilter;

public class LoginTest 
{
  private final static Logger LOG = Logger.getLogger(LoginTest.class);
  
  private static final int MAX_CONNECTIONS = 100;
  private static final int CONNECTION_TIMEOUT = 10000;
  private static final int IDLE_CONNECTION_TIMEOUT = 10000;
  private static final int REQUEST_TIMEOUT = 10000;
  private static final int MAX_RETRY_ATTEMPTS = 2;
  private static final int WAIT_BEFORE_RETRY = 1000;
  
  
  private AsyncHttpClient httpClient;
  private FutureImpl<String> authToken;

  private Date currentDateTime = new Date();
  
  
  @Before
  public void setUp() throws Exception
  {
    LOG.setLevel(Level.ALL);

    AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
    .addRequestFilter(new ThrottleRequestFilter(MAX_CONNECTIONS))
    .setConnectionTimeoutInMs(CONNECTION_TIMEOUT)
    .setIdleConnectionInPoolTimeoutInMs(IDLE_CONNECTION_TIMEOUT)
    .setRequestTimeoutInMs(REQUEST_TIMEOUT)
    .setMaxRequestRetry(0)
    //.setProxyServer(new ProxyServer ("cornillon.grenoble.xrce.xerox.com", 8000))
    .setFollowRedirects(true)
    .build();
    
    this.httpClient = new AsyncHttpClient(config);
    this.authToken = new FutureImpl<>();
  }
  
  @After
  public void tearDown() throws Exception
  {
    httpClient.close();
  }
  
  @Test
  public void testLogin() throws Exception
  { 
    String authZHeader = buildHeader();
    httpClient
      .prepareGet("https://twitter.com")
      .addHeader("Accept-Charset", "utf-8")
      .addHeader("Accept-Language", "en-US")
      .execute(new TweetWelcomeHandler (authToken));
    
    String authTokenValue = authToken.get();
   
    httpClient
      .preparePost("https://twitter.com/sessions")
      .setBody (StringUtils.join(new String[] {
          "session[username_or_email]=budgefeeneyf1",
          "session[password]=FIXME",
          "scribe_log=",
          "return_to_ssl=true",
          "remember_me=1",
          "redirect_after_login=/",
          "authenticity_token:" + authToken.get()
        }, '&'))
      .execute (new EchoHandler());
      
    Thread.sleep(TimeUnit.MINUTES.toMillis(10));
  }
  
  @Test
  public void testFollowers() throws Exception
  {
    String authZHeader = buildHeader();
    httpClient
      .prepareGet("https://twitter.com/charlie_whiting/followers")
      .addHeader("Accept-Charset", "utf-8")
      .addHeader("Accept-Language", "en-US")
      .addHeader("Authorization", authZHeader)
      .execute(new EchoHandler ());
    
    Thread.sleep(TimeUnit.MINUTES.toMillis(10));
  }
  
  private String buildHeader() {
    long dateTime = currentDateTime .getTime();
    return String.format(
        "OAuth oauth_consumer_key=\"%s\", oauth_nonce=\"%s%d\", oauth_signature=\"%s\", oauth_signature_method=\"%s\", oauth_timestamp=\"%d\", oauth_token=\"%s\", oauth_version=\"1.0\"",
        "mODntTBvqFWEtnlsV6THQ",
        "36a7c4104da73", 
        dateTime, 
        "fGPT9StUyBwOnLbJvRT3ZrRZRrg%3D",
        "HMAC-SHA1", 
        dateTime,
        "18447686-FixB8106ipARDQi1BZ9tJ8Yx17WH7r6n29bHzPMYi");
  }

  private static class EchoHandler extends AsyncCompletionHandler<Response>
  {

    @Override
    public Response onCompleted(Response res) throws Exception {
      System.out.println (res.getResponseBody());
      return res;
    }
    
  }
  
  private static class TweetWelcomeHandler extends AsyncCompletionHandler<Response>
  {
    private final FutureImpl<String> authToken;
    
        
    public TweetWelcomeHandler(FutureImpl<String> authToken) {
      super();
      this.authToken = authToken;
    }

    @Override
    public Response onCompleted(final Response res) throws Exception
    {
      try {

        String html = res.getResponseBody("UTF-8");
        Document document = Jsoup.parse(html);
        
        Element authTokenElt = document.select("input[name=authenticity_token]").first();
        authToken.put(authTokenElt.attr("value"));
        
        /*
         * <div class="js-front-language">
                        <form action="/sessions/change_locale" class="language" method="POST">
                          <input type="hidden" name="lang">
                          <input type="hidden" name="redirect">
                          <input type="hidden" name="authenticity_token" value="fe687518ddee06e35d7fca138c01a6ef22bef497">
                        </form>
                      </div>
         */
        return res;
      }
      catch (Exception e)
      { authToken.putError(e);
        return res;
      }
    }
    
  }
  
  private static class TweetLoginHandler extends AsyncCompletionHandler<Response>
  {
    @Override
    public Response onCompleted(Response res) throws Exception
    {
      return null;
    }
    
  }
}
