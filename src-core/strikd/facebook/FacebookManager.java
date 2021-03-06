package strikd.facebook;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;

import strikd.Server;
import strikd.game.facebook.FacebookInviteManager;
import strikd.util.NamedThreadFactory;

public class FacebookManager extends Server.Referent
{
	private static final Logger logger = LoggerFactory.getLogger(FacebookManager.class);
	
	private final String pageId;
	private final String appNamespace;
	private final String appAccessToken;
	
	private final FacebookInviteManager inviteMgr;
	
	private final ExecutorService publisher;
	
	public FacebookManager(String pageId, String appNamespace, String appAccessToken, Server server)
	{
		// Initialize static data
		super(server);
		this.pageId = pageId;
		this.appNamespace = appNamespace;
		this.appAccessToken = appAccessToken;
		
		// Log namespace and a masked copy of the token
		logger.info("pageId='{}' [og:namespace='{}', access_token={}]",
				pageId,
				appNamespace,
				appAccessToken.substring(0, appAccessToken.indexOf('|') + 1) + "<SECRET>");
		
		// Create nested invite manager
		this.inviteMgr = new FacebookInviteManager(server);
		
		// Create background threadpool for publishing actions
		this.publisher = Executors.newCachedThreadPool(new NamedThreadFactory("Facebook Publisher #%d"));
	}
	
	public void publish(FacebookStory story)
	{
		this.publisher.execute(story);
	}
	
	public String getPageId()
	{
		return this.pageId;
	}
	
	public String getAppNamespace()
	{
		return this.appNamespace;
	}
	
	public String getAppAccessToken()
	{
		// This token is derived from app ID + app secret, and is used to authorize the server to post stories
		// https://graph.facebook.com/oauth/access_token?client_id=APP_ID&client_secret=APP_SECRET&grant_type=client_credentials
		return this.appAccessToken;
	}
	
	public FacebookInviteManager getInviteMgr()
	{
		return this.inviteMgr;
	}
	
	private static String sharedAppNamespace, sharedAppAccessToken;

	public static String getSharedAppNamespace()
	{
		return sharedAppNamespace;
	}
	
	public static void setSharedAppNamespace(String appNamespace)
	{
		sharedAppNamespace = appNamespace;
	}
	
	public static void setSharedAppAccessToken(String accessToken)
	{
		sharedAppAccessToken = accessToken;
	}
	
	private static final ThreadLocal<FacebookClient> sharedAppAPI = new ThreadLocal<FacebookClient>();
	
	public static FacebookClient getSharedAppAPI()
	{
		FacebookClient api = sharedAppAPI.get();
		if(api == null)
		{
			api = new DefaultFacebookClient(sharedAppAccessToken);
			sharedAppAPI.set(api);
		}
		
		return api;
	}
}
