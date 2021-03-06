package strikd.game.match;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import strikd.communication.outgoing.AnnounceMatchMessage;
import strikd.communication.outgoing.BoardInitMessage;
import strikd.communication.outgoing.MatchEndedMessage;
import strikd.communication.outgoing.MatchStartedMessage;
import strikd.game.board.Board;
import strikd.game.board.impl.RenegadeBoard;
import strikd.game.match.bots.MatchBotPlayer;
import strikd.game.player.ExperienceHandler;
import strikd.locale.LocaleBundle;
import strikd.locale.LocaleBundle.DictionaryType;
import strikd.net.codec.OutgoingMessage;

public class Match
{
	private static final Logger logger = LoggerFactory.getLogger(Match.class);
	
	private final long matchId;
	private final LocaleBundle locale;
	private final MatchManager matchMgr;
	
	private final MatchPlayer playerOne;
	private final MatchPlayer playerTwo;
	
	private final MatchTimer timer;
	private final Board board;
	
	private final byte loadingTime;
	private boolean isEnded;
	
	public Match(long matchId, LocaleBundle locale, MatchManager matchMgr, MatchPlayer playerOne, MatchPlayer playerTwo)
	{
		// Setup static match data
		this.matchId = matchId;
		this.locale = locale;
		this.matchMgr = matchMgr;
		
		// Setup timer
		this.loadingTime = 0;
		this.timer = new MatchTimer(this, (int)TimeUnit.MINUTES.toSeconds(2));
		
		// Install the board implementation
        //this.board = new AgingRenegadeBoard(5, 6, locale.getDictionary(DictionaryType.COMMON), this, this.loadingTime, 15);
		 this.board = new RenegadeBoard(5, 6, locale.getDictionary(DictionaryType.COMMON));

		// Link the players to this match with a personal ID
		this.playerOne = playerOne.setMatch(this, 1);
		this.playerTwo = playerTwo.setMatch(this, 2);
	}
	
	public void destroy()
	{
		// Out before the bomb!
		this.playerOne.leave();
		this.playerTwo.leave();
	}
	
	public void announce()
	{
		// Send personal ANNOUNCE messages to both players
		this.playerOne.send(new AnnounceMatchMessage(this, this.playerOne, this.playerTwo));
		this.playerTwo.send(new AnnounceMatchMessage(this, this.playerTwo, this.playerOne));
	}
	
	public void prepareBoard()
	{
		// Generate board and discard updates
		this.board.rebuild();
		this.board.clearUpdates();
	}
	
	public void broadcast(OutgoingMessage msg)
	{
		// Log it seperately
		logger.debug("> {}", msg);
		
		// Don't waste bytes & buffers
		if(this.playerTwo instanceof MatchBotPlayer)
		{
			// Original buffer
			this.playerOne.getSession().getConnection().send(msg);
			
			// Just for triggering events etc (like an IRC bot!)
			this.playerTwo.send(msg);
		}
		else
		{
			// The copy requirement is because crypto (if enabled) ciphers in-place
			this.playerOne.getSession().getConnection().sendCopy(msg);
			this.playerTwo.getSession().getConnection().send(msg);
		}
	}
	
	public void checkReady()
	{
		// Both players ready?
		if(this.playerOne.isReady() && this.playerTwo.isReady())
		{
			this.start();
		}
	}
	
	private void start()
	{
		// Not already started?
		if(!this.timer.isRunning() && !this.isEnded)
		{
			// Initial board!
			this.broadcast(new BoardInitMessage(this.board));
			
			// Start the timers at the clients etc, the game is ON!
			this.broadcast(new MatchStartedMessage());
			
			// Go!
			this.timer.start();
		}
	}

	public void end(MatchPlayer winner)
	{
		// Can be ended?
		if(!this.isEnded)
		{
			// Stop timer
			this.isEnded = true;
			if(this.timer.isRunning())
			{
				this.timer.stop();
			}
			
			// Destroy the board
			this.board.destroy();
			
			// Log result
			if(winner == null)
			{
				logger.debug("{}: draw...", this);
			}
			else
			{
				MatchPlayer loser = winner.getOpponent();
				logger.debug("{}: {} wins, {} loses!", this, winner, loser);
			}
			
			// Broadcast event
			this.broadcast(new MatchEndedMessage(this.playerOne, this.playerTwo, winner));
			
			// Do post-match shit
			ExperienceHandler exp = this.matchMgr.getServer().getExperienceHandler();
			try
			{
				exp.executePostMatchLogic(this, this.playerOne, this.playerTwo, winner);
			}
			catch(Exception e)
			{
				logger.error("error executing post-match logic", e);
			}
			
			// Request match to be destroyed
			this.matchMgr.destroyMatch(this.matchId);
		}
	}
	
	public void removePlayer(MatchPlayer player)
	{
		// Match is still going?
		if(!this.isEnded)
		{
			this.end(player.getOpponent());
		}
		
		// Bye!
		if(player.getSession() != null)
		{
			player.getSession().exitMatch();
		}
	}

	public void timerEnded()
	{
		if(this.playerOne.getScore() > this.playerTwo.getScore())
		{
			this.end(this.playerOne);
		}
		else if(this.playerTwo.getScore() > this.playerOne.getScore())
		{
			this.end(this.playerTwo);
		}
		else
		{
			this.end(null);
		}
	}
	
	public long getMatchId()
	{
		return this.matchId;
	}
	
	public LocaleBundle getLocale()
	{
		return this.locale;
	}
	
	public MatchPlayer getOpponent(MatchPlayer player)
	{
		return (player == this.playerOne) ? this.playerTwo : this.playerOne;
	}
	
	public MatchTimer getTimer()
	{
		return this.timer;
	}
	
	public Board getBoard()
	{
		return this.board;
	}
	
	public byte getLoadingTime()
	{
		return this.loadingTime;
	}
	
	@Override
	public String toString()
	{
		return String.format("match #%d (%s)", this.matchId, this.locale);
	}
}
