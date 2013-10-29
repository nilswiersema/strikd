package strikd.communication.outgoing;

import java.util.List;

import strikd.communication.Opcodes;
import strikd.game.board.Square;
import strikd.net.codec.OutgoingMessage;

public class BoardUpdateMessage extends OutgoingMessage
{
	public BoardUpdateMessage(List<Square> removed, List<Square> added)
	{
		super(Opcodes.Outgoing.BOARD_UPDATE);
		
		// Removed tiles
		super.writeByte((byte)removed.size());
		for(Square tile : removed)
		{
			super.writeByte((byte)(tile.x << 4 | tile.y));
		}
		
		// New tiles (spawn at the top y and fall down till they hit something)
		super.writeByte((byte)added.size());
		for(Square tile : added)
		{
			super.writeByte((byte)tile.x);
			super.writeByte((byte)tile.getLetter());
			
			// TODO: store trigger info in remaining 3 bits (square.getLetter() << 5)); 
		}
	}
}