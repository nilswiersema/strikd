package strikd.game.stream;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import strikd.Server;
import strikd.game.player.Player;
import strikd.game.stream.items.NewsStreamItem;

public class ActivityStreamManager extends Server.Referent
{
	private static final Logger logger = LoggerFactory.getLogger(ActivityStreamManager.class);
	private List<NewsStreamItem> news = Collections.emptyList();
	
	public ActivityStreamManager(Server server)
	{
		super(server);
		logger.info("{} events", 0);
	}
	
	public void reloadNews()
	{
		// Sample news item
		/*
		if(this.dbNews.count() == 0)
		{
			NewsStreamItem welcome = new NewsStreamItem();
			welcome.timestamp = new Date();
			welcome.headline = "Welcome aboard Strik!";
			welcome.imageUrl = "http://i.imgur.com/blabla.png";
			welcome.body = "Thanks for playing Strik! In the coming days we will blablablaa!";
			this.dbNews.save(welcome);
		}
		
		// Load them all into memory
		this.news = Lists.newArrayList(this.dbNews.find().as(NewsStreamItem.class));
		logger.info("%d news items", this.news.size()));
		
		// Print them
		for(NewsStreamItem news : this.news)
		{
			logger.debug("\"%s\" published on %s", news.headline, news.timestamp));
		}*/
	}
	
	public void postItem(ActivityStreamItem item)
	{
		/*
		item.timestamp = new Date();
		this.dbStream.save(item);*/
	}
	
	public List<ActivityStreamItem> getPlayerStream(Player player, int begin, int end, Player requester)
	{
		return Collections.emptyList();
		/*
		//this.dbStream.find("{t:{$gte:#,$lt:#}}", periodBegin, periodEnd);
		
		// A list that will be sorted later
		List<EventStreamItem> result = Lists.newArrayList();
		
		// Add the own items
		Iterables.addAll(result, this.dbStream.find("{p:#,t:{$gte:#,$lt:#}}", player.id, periodBegin, periodEnd).as(EventStreamItem.class));
		
		// Add items of direct Facebook friends who are also players
		if(player.isFacebookLinked())
		{
			List<Long> friendIds = this.getServer().getPlayerRegister().getFriends(player.fbIdentity);
			Iterables.addAll(result, this.dbStream.find("{p:{$in:#},t:{$gte:#,$lt:#}}", friendIds, periodBegin, periodEnd).as(EventStreamItem.class));
		}
		
		// Add the news items that are within range
		result.addAll(this.news);
		
		// Sort on timestamp
		Collections.sort(result, TIMESTAMP_SORTER);
		return result;*/
	}
	
	private static final Comparator<ActivityStreamItem> TIMESTAMP_SORTER = new Comparator<ActivityStreamItem>()
	{
		@Override
		public int compare(ActivityStreamItem a, ActivityStreamItem b)
		{
			return a.timestamp.compareTo(b.timestamp);
		}
	};
}