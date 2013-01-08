package pl.edu.uj.paperfootball.packets;

import java.io.Serializable;

import pl.edu.uj.paperfootball.utils.Point;


/**
 * Packet with a single move of the player.
 */
public class PacketMoveViaBluetooth extends Packet implements Serializable {

	private static final long serialVersionUID = 1737860661093665229L;

	private final Point mEndPoint;
	private final Point mStartPoint;

	/**
	 * Constructs a single move of the player in the game.
	 * 
	 * @param startPoint
	 *            The point at which the player starts move.
	 * @param endPoint
	 *            The point at which the player ends move.
	 * @param changePlayer
	 *            Indicated whether to change the player.
	 */
	public PacketMoveViaBluetooth(Point startPoint, Point endPoint, boolean changePlayer) {
		super(PacketType.NEW_MOVE, changePlayer);
		mStartPoint = startPoint;
		mEndPoint = endPoint;
	}

	/**
	 * Copy constructor.
	 * 
	 * @param packet
	 *            PacketMoveViaBluetooth to copy.
	 */
	public PacketMoveViaBluetooth(PacketMoveViaBluetooth packet) {
		this(new Point(packet.mStartPoint), new Point(packet.mEndPoint), packet.isChangePlayer());
	}

	public Point getEndPoint() {
		return mEndPoint;
	}

	@Override
	public String toString() {
		final int expectedStringSize = 35;
		StringBuilder builder = new StringBuilder(expectedStringSize);
		builder.append("X: ").append(mEndPoint.getX()).append(" Y: ").append(mEndPoint.getY())
				.append(" ChangePlayer: ").append(isChangePlayer());
		return builder.toString();
	}
}
