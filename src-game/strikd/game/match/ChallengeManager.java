package strikd.game.match;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import strikd.communication.outgoing.ChallengeDeclinedMessage;
import strikd.communication.outgoing.ChallengeMessage;
import strikd.communication.outgoing.ChallengeRevokedMessage;
import strikd.game.player.Player;
import strikd.sessions.Session;
import static strikd.game.match.ChallengeManager.State.*;

public class ChallengeManager
{
	private static final Logger logger = LoggerFactory.getLogger(ChallengeManager.class);
	
	private Session session;
	
	private State state;
	private ChallengeManager reference;
	
	public ChallengeManager(Session session)
	{
		this.session = session;
		this.available();
	}
	
	private boolean is(State state)
	{
		return (this.state == state);
	}
	
	private void set(State state, ChallengeManager reference)
	{
		this.state = state;
		this.reference = reference;
		
		logger.debug("{} is now {}", this.getPlayer(), state);
	}
	
	private Player getPlayer()
	{
		return this.session.getPlayer();
	}
	
	public boolean challenge(ChallengeManager other)
	{
		// Legal state?
		if(!(this.is(AVAILABLE) && other.is(AVAILABLE)))
		{
			return false;
		}
		else
		{
			// Update challenger
			this.set(CHALLENGING, other);
			other.set(CHALLENGED, this);
			
			// Ring, ring!
			other.session.send(new ChallengeMessage(this.getPlayer()));
			
			// The waiting game is on!
			return true;
		}
	}
	
	public void acceptChallenge(int playerId)
	{
		if(this.is(CHALLENGED) && playerId == this.reference.getPlayer().getId())
		{
			// Create players
			MatchPlayer p1 = new MatchPlayer(this.reference.session);
			MatchPlayer p2 = new MatchPlayer(this.session);
			
			// Clear data
			this.releaseBoth();
			
			// Create match
			MatchManager matchMgr = this.session.getServer().getMatchMgr();
			matchMgr.newMatch(p1, p2);
		}
	}
	
	public void declineChallenge(int playerId)
	{
		if(this.is(CHALLENGED) && playerId == this.reference.getPlayer().getId())
		{
			this.reference.session.send(new ChallengeDeclinedMessage(this.getPlayer()));
			this.releaseBoth();
		}
	}
	
	public void revokeChallenge(int playerId)
	{
		if(this.is(CHALLENGING) && playerId == this.reference.getPlayer().getId())
		{
			this.reference.session.send(new ChallengeRevokedMessage(this.getPlayer()));
			this.releaseBoth();
		}
	}
	
	private void releaseBoth()
	{
		this.reference.set(AVAILABLE, null);
		this.set(AVAILABLE, null);
	}
	
	public void available()
	{
		this.set(AVAILABLE, null);
	}
	
	public void unavailable()
	{
		// This player is being challenged?
		if(this.is(CHALLENGED))
		{
			// We decline them
			this.declineChallenge(this.reference.getPlayer().getId());
		}
		
		// This player is challenging someone?
		else if(this.is(CHALLENGING))
		{
			// We revoke our challenge to them
			this.revokeChallenge(this.reference.getPlayer().getId());
		}
		
		// Boom!
		this.set(UNAVAILABLE, null);
	}
	
	protected enum State
	{
		AVAILABLE,
		UNAVAILABLE,
		CHALLENGING,
		CHALLENGED
	}
}
