package pl.edu.uj.paperfootball.packets;

import java.io.Serializable;

/**
 * Packet with opponents answer.
 */
public class PacketAnswer extends Packet implements Serializable {

	private static final long serialVersionUID = -3231561214498400326L;

	private final boolean mAnswer;

	/**
	 * Constructor of the packet with opponents answer.
	 * 
	 * @param answer
	 *            Opponents answer.
	 */
	public PacketAnswer(final boolean answer) {
		super(PacketType.ACCEPT_OR_REJECT, true);
		mAnswer = answer;
	}

	/**
	 * Copy constructor.
	 * 
	 * @param packet
	 *            PacketAnswer to copy.
	 */
	public PacketAnswer(PacketAnswer packet) {
		this(packet.mAnswer);
	}

	public boolean isAnswerTrue() {
		return mAnswer;
	}
}
