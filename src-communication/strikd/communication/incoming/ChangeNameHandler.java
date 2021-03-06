package strikd.communication.incoming;

import strikd.communication.Opcodes;
import strikd.communication.outgoing.ExperienceAddedMessage;
import strikd.communication.outgoing.NameRejectedMessage;
import strikd.game.util.InputFilter;
import strikd.net.codec.IncomingMessage;
import strikd.sessions.Session;

public class ChangeNameHandler extends MessageHandler
{
	@Override
	public Opcodes.Incoming getOpcode()
	{
		return Opcodes.Incoming.CHANGE_NAME;
	}
	
	@Override
	public void handle(Session session, IncomingMessage request)
	{
		String requestedName = request.readStr();
		
		// Filter & validate new name
		String newName = InputFilter.sanitizeInput(requestedName);
		
		// Reject name?
		if(newName.equals("Satan"))
		{
			session.send(new NameRejectedMessage(requestedName, "forbidden"));
		}
		else
		{
			session.renamePlayer(newName);
			
			// Try to add x XP
			int added = session.getServer().getExperienceHandler().addExperience(session.getPlayer(), 70);
			
			// Notify player
			session.send(new ExperienceAddedMessage(added, session.getPlayer().getXp()));
		}
	}
}
