package pl.edu.uj.paperfootball.packets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pl.edu.uj.paperfootball.bluetooth.GameThread.MoveState;


/**
 * Packet with list of saved moves.
 */
public class PacketContinueGameViaBluetooth extends Packet implements Serializable {

	private static final long serialVersionUID = -1759345762777918335L;

	private final MoveState mMoveState;
	private final List<Integer> mMovesList;

	/**
	 * Constructor of the packet with a list of moves.
	 * 
	 * @param movesList
	 *            Saved moves in previous game.
	 * @param changePlayer
	 *            Indicates whether to change the player.
	 * @param moveState
	 *            Indicates whether is player or opponent move.
	 */
	public PacketContinueGameViaBluetooth(List<Integer> movesList, boolean changePlayer, MoveState moveState) {
		super(PacketType.REQUEST_GAME_LOAD, changePlayer);
		mMovesList = movesList;
		mMoveState = moveState;
	}

	/**
	 * Copy constructor.
	 * 
	 * @param packet
	 *            PacketContinueGameViaBluetooth to copy.
	 */
	public PacketContinueGameViaBluetooth(PacketContinueGameViaBluetooth packet) {
		this(new ArrayList<Integer>(packet.mMovesList), packet.isChangePlayer(), packet.mMoveState);
	}

	public List<Integer> getPoints() {
		return mMovesList;
	}

	public MoveState getMoveState() {
		return mMoveState;
	}

	@Override
	public String toString() {
		int size = mMovesList.size();
		final int expectedStringSize = size * 12 + 30;
		StringBuilder builder = new StringBuilder(expectedStringSize);

		for (int i = 0; i < size; i += 2) {
			int x = mMovesList.get(i);
			int y = mMovesList.get(i + 1);
			builder.append("X: ").append(x).append(" Y: ").append(y).append("  #next point:  ");
		}
		builder.append(" ChangePlayer: ").append(isChangePlayer());
		return builder.toString();
	}
}
