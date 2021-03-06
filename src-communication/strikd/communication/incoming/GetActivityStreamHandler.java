package strikd.communication.incoming;

import java.util.List;

import strikd.sessions.Session;
import strikd.communication.Opcodes;
import strikd.communication.outgoing.ActivityStreamMessage;
import strikd.game.stream.ActivityStream;
import strikd.game.stream.ActivityStreamItem;
import strikd.net.codec.IncomingMessage;

public class GetActivityStreamHandler extends MessageHandler
{
	@Override
	public Opcodes.Incoming getOpcode()
	{
		return Opcodes.Incoming.GET_ACTIVITY_STREAM;
	}
	
	@Override
	public void handle(Session session, IncomingMessage request)
	{
		// Stream available?
		ActivityStream stream = session.getServer().getActivityStream();
		if(stream != null)
		{
			// Determine span
			int start = request.readInt();
			int end = request.readInt();
			
			// Send all items in this period
			List<ActivityStreamItem> items = stream.filterItems(session.getFollowing(), start, (end - start));
			session.send(new ActivityStreamMessage(start, end, items, session.getPlayer()));
		}
	}
}
