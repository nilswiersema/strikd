package strikd.game.facebook;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import strikd.Server;
import strikd.game.player.Player;
import strikd.game.player.PlayerRegister;

public class FacebookInviteManager extends Server.Referent
{
	private static final Logger logger = LoggerFactory.getLogger(FacebookInviteManager.class);
	
	public FacebookInviteManager(Server server)
	{
		super(server);
		//this.dbInvites = server.getDbCluster().getCollection("invites");
		
		logger.info("pending invites for {} persons", 0);//this.dbInvites.count()));
	}
	
	public void registerInvite(long userId, Player inviter)
	{
		//this.dbInvites.update("{_id:#}", personId).upsert().with("{$addToSet:{by:#}}", inviter.getId());	
		logger.info("{} invited person #{}!", inviter, userId);
	}
	
	public void processInvites(long userId)
	{
		// Was this FB user invited by existing players?
		List<Integer> invites = Lists.newArrayList();//this.dbInvites.findAndModify("{_id:#}", personId).remove().as(InvitedByList.class);
		if(invites != null)
		{
			// Reward these guys
			PlayerRegister register = this.getServer().getPlayerRegister();
			for(int playerId : invites)
			{
				// Player _should_ still exist
				Player player = register.findPlayer(playerId);
				if(player != null)
				{
					logger.info("FB user #{} linked (welcome!), rewarding {} for invite", userId, player);
				}
			}
		}
	}
}
