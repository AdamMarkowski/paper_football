package pl.edu.uj.paperfootball.packets;

import java.io.Serializable;

/**
 * Simple raw packet.
 */
public class Packet implements Serializable {

	private static final long serialVersionUID = 3250238582064055131L;

	private final PacketType mType;
	private final boolean mChangePlayer;

	/**
	 * Constructor of the Packet.
	 * 
	 * @param type
	 *            Type of the packet.
	 * @param changePlayer
	 *            Indicated whether to change the player.
	 */
	public Packet(final PacketType type, final boolean changePlayer) {
		mType = type;
		mChangePlayer = changePlayer;
	}

	public boolean isChangePlayer() {
		return mChangePlayer;
	}

	public PacketType getPacketType() {
		return mType;
	}
}
