package pl.edu.uj.paperfootball.packets;

/**
 * Enumeration representing available types of packets that can be send via Bluetooth.
 */
public enum PacketType {
	NEW_MOVE, REQUEST_GAME_LOAD, CONTINUE_GAME, REQUEST_SHOW_GAME_REPLAY, ACCEPT_OR_REJECT, REQUEST_REPEAT_MOVE, REQUEST_CANCEL, PACKET_NO_TYPE
}