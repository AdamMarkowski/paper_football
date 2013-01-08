package pl.edu.uj.paperfootball.bluetooth;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import pl.edu.uj.paperfootball.CurrentPlaygroundState;
import pl.edu.uj.paperfootball.packets.Packet;
import pl.edu.uj.paperfootball.packets.PacketAnswer;
import pl.edu.uj.paperfootball.packets.PacketContinueGameViaBluetooth;
import pl.edu.uj.paperfootball.packets.PacketMoveViaBluetooth;
import pl.edu.uj.paperfootball.state.StateRecorder;
import pl.edu.uj.paperfootball.utils.RefreshHandler;

import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.util.Log;


/**
 * Main game thread which runs on both client and server sides.
 */
public class GameThread extends Thread implements PropertyChangeListener {

	private static final String TAG = GameThread.class.getSimpleName();

	public static final int SERVER = 1;
	public static final int CLIENT = 2;

	private final PropertyChangeSupport mPropertyChangeSupport;
	private final Object mMonitor;
	private final RefreshHandler mRefreshHandler;
	private final StateRecorder mStateRecorder;
	private final CurrentPlaygroundState mCPS;
	private final CountDownLatch mCountDownLatch;
	private boolean mChangePlayer;
	private volatile MoveState mMoveState;

	private BluetoothSocket mSocket;
	private ObjectInputStream mObjectInStream;
	private ObjectOutputStream mObjectOutStream;
	private Packet mObjectToSend;

	/**
	 * Enumeration represents state of the move.
	 */
	public static enum MoveState {
		OPPONENT_MOVE, WAIT_FOR_MY_MOVE, MY_MOVE_SEND_MOVES_VIA_BLUETOOTH
	};

	/**
	 * Constructs main game thread.
	 * 
	 * @param gameMode
	 *            Mode of the game.
	 * @param refreshHandler
	 *            Refresh handler which handles every UI change.
	 * @param moveState
	 *            State of the move.
	 * @param CPS
	 *            Current playground state - game model.
	 * @param countDownLatch
	 *            Latch which helps synchronizing thread.
	 * @param stateRecorder
	 *            State recorder which provides access to the database.
	 */
	public GameThread(RefreshHandler refreshHandler, MoveState moveState, CurrentPlaygroundState cps,
			CountDownLatch countDownLatch, StateRecorder stateRecorder) {
		mRefreshHandler = refreshHandler;
		mCountDownLatch = countDownLatch;
		mStateRecorder = stateRecorder;
		mMoveState = moveState;
		mCPS = cps;

		mPropertyChangeSupport = new PropertyChangeSupport(this);
		mMonitor = new Object();
		mChangePlayer = true;
		setName(TAG);
	}

	/**
	 * Sets socket object and initializes output and input streams.
	 * 
	 * @param socket
	 *            Socket which provides communication via Bluetooth.
	 */
	public boolean setSocketAndInitializeStreams(BluetoothSocket socket) {
		mSocket = socket;
		ObjectInputStream input = null;
		ObjectOutputStream output = null;

		// Get the input and output streams, using temporary objects because member streams are final
		try {
			// same for server and client
			output = new ObjectOutputStream(socket.getOutputStream());
			output.flush();
			input = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			showConnectionErrorAndFinishActivity();
			return false;
		}

		mObjectInStream = input;
		mObjectOutStream = output;
		return true;
	}

	@Override
	public void run() {
		while (true) {
			synchronized (mMonitor) {
				if (mMoveState == MoveState.OPPONENT_MOVE) {
					// read opponent move (that comes from bluetooth)
					Packet myPacketObject = null;
					try {
						if (mObjectInStream != null) {
							myPacketObject = (Packet) mObjectInStream.readObject();
						}
					} catch (OptionalDataException e) {
						// thrown when other side sent via bluetooth primitive type instead of object
						break;
					} catch (ClassNotFoundException e) {
						// thrown when opponent sent via bluetooth not existing class object
						break;
					} catch (IOException e) {
						// thrown when opponent via bluetooth closed output stream
						// opponent closed mObjectOutStream and
						// here we are waiting on readObject => throw IOException
						break;
					}

					if (myPacketObject != null) {
						handlePacket(myPacketObject);
					} else {
						Log.e(TAG, "Received null instead of packet");
					}
				}

				else if (mMoveState == MoveState.WAIT_FOR_MY_MOVE) {
					try {
						// while(mMoveState == MoveState.WAIT_FOR_MY_MOVE)
						// if(mCPS.isNextPlayerMove())
						mMonitor.wait();
						// move done on the phone so now send move via socket to 2nd phone
						mObjectOutStream.writeObject(mObjectToSend);
						mObjectOutStream.flush();
						mObjectOutStream.reset();
					} catch (InterruptedException e) {
						// thrown when user closes game @ monitor.wait()
						break;
					} catch (IOException e) {
						// thrown when opponent via bluetooth closed input stream @
						// mObjectOutStream.writeObject(moveObjectToDo)
						break;
					}
				} else if (mMoveState == MoveState.MY_MOVE_SEND_MOVES_VIA_BLUETOOTH) {
					prepareLoadGamePacket();
					try {
						mObjectOutStream.writeObject(mObjectToSend);
						mObjectOutStream.flush();
						mObjectOutStream.reset();
					} catch (IOException e) {
						// probably socket is closed, so end this thread
						break;
					}
				}
			}
		}

		Message msg = mRefreshHandler.obtainMessage();
		msg.what = RefreshHandler.TOAST_MSG;
		msg.obj = "Opponent left the game - game finished";
		mRefreshHandler.sendMessage(msg);
		mRefreshHandler.sendEmptyMessageDelayed(RefreshHandler.FINISH_ACTIVITY, 3000);

		closeThread();
	}

	/**
	 * Handles incoming packet from remote device.
	 * 
	 * @param packet
	 *            Incoming packet.
	 */
	private void handlePacket(Packet packet) {
		mChangePlayer = packet.isChangePlayer();

		if (mChangePlayer) {
			mMoveState = MoveState.WAIT_FOR_MY_MOVE;
		}

		switch (packet.getPacketType()) {
		case NEW_MOVE:
			PacketMoveViaBluetooth movePacket = (PacketMoveViaBluetooth) packet;

			mChangePlayer = movePacket.isChangePlayer();

			if (mChangePlayer) {
				// changing state to myMove and send via Bluetooth
				// (e.g. fireProperty... will call propertyChange @ GameManager)
				Message msg = mRefreshHandler.obtainMessage();
				msg.what = RefreshHandler.SET_MOVE_STATE;
				msg.obj = mMoveState;
				mRefreshHandler.sendMessage(msg);
				mRefreshHandler.sendEmptyMessage(RefreshHandler.VIBRATE);
			}

			// this fires up @GameManager's the propertyChange() function
			mPropertyChangeSupport.firePropertyChange("Przyszedl ruch przeciwnika z Bluetooth", null,
					new PacketMoveViaBluetooth(movePacket));
			break;
		case REQUEST_GAME_LOAD:
			PacketContinueGameViaBluetooth moveslistPacket = (PacketContinueGameViaBluetooth) packet;
			mMoveState = moveslistPacket.getMoveState();

			// this fires up @GameManager's the propertyChange() function
			mPropertyChangeSupport.firePropertyChange("Przyszla lista ruchów przeciwnika z Bluetooth", null,
					new PacketContinueGameViaBluetooth(moveslistPacket));
			break;
		case REQUEST_CANCEL:
			break;
		case PACKET_NO_TYPE:
			break;
		case REQUEST_REPEAT_MOVE:
			mRefreshHandler.sendEmptyMessage(RefreshHandler.REPEAT_MOVE);
			break;
		case REQUEST_SHOW_GAME_REPLAY:
			mRefreshHandler.sendEmptyMessage(RefreshHandler.SHOW_CHOOSE_ANSWER);
			break;
		case ACCEPT_OR_REJECT:
			PacketAnswer answer = (PacketAnswer) packet;
			mRefreshHandler.sendEmptyMessage(RefreshHandler.DISMISS_DIALOG);

			if (answer.isAnswerTrue()) {
				mRefreshHandler.sendEmptyMessage(RefreshHandler.SHOW_REPLAY);
			}

			break;
		default:
			Log.e(TAG, "Wrong packet " + packet.getPacketType());
			break;
		}

	}

	/**
	 * Interrupts this thread.
	 */
	public void interruptThread() {
		if (!isInterrupted()) {
			interrupt();
		}
	}

	/*
	 * Call this from the main activity to shutdown the connection.
	 */
	private void closeThread() {
		interruptThread();

		try {
			if (mObjectOutStream != null) {
				mObjectOutStream.close();
			}
		} catch (IOException e) {
			Log.w(TAG, "mObjectOutStream.close() failed");
		}

		try {
			if (mObjectInStream != null) {
				mObjectInStream.close();
			}
		} catch (IOException e) {
			Log.w(TAG, "mObjectInStream.close() failed");
		}

		try {
			if (mSocket != null) {
				mSocket.close();
			}
		} catch (IOException e) {
			Log.w(TAG, "mSocket.close() failed");
		}

	}

	/**
	 * Called when canvas will change GameManager class will call this via calling
	 * mPropertyChangeSupport.firePropertyChange() from moveBall2().
	 * 
	 * @param event
	 *            Event which contains changed property.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// called when SCanvas has changed (called by GameManager)

		if (mMoveState == MoveState.MY_MOVE_SEND_MOVES_VIA_BLUETOOTH) {
			// this moves are from loading same saved game and
			// they will be send via PacketContinueGameViaBluetooth
			// so here don't send them to second device
			return;
		}
		synchronized (mMonitor) {
			Packet packet = (Packet) event.getNewValue();
			mObjectToSend = packet;

			switch (packet.getPacketType()) {
			case NEW_MOVE:
				final PacketMoveViaBluetooth move = (PacketMoveViaBluetooth) event.getNewValue();
				mChangePlayer = move.isChangePlayer();

				if (mChangePlayer) {
					mMoveState = MoveState.OPPONENT_MOVE;
					Message msg = mRefreshHandler.obtainMessage();
					msg.what = RefreshHandler.SET_MOVE_STATE;
					msg.obj = mMoveState;
					mRefreshHandler.sendMessage(msg);
				}
				break;
			case REQUEST_CANCEL:
				break;
			case PACKET_NO_TYPE:
				break;
			case REQUEST_REPEAT_MOVE:
				break;
			case REQUEST_SHOW_GAME_REPLAY:
			case ACCEPT_OR_REJECT:
				mMoveState = MoveState.OPPONENT_MOVE;
				break;
			default:
				Log.e(TAG, "wrong packet " + packet.getPacketType());
				break;
			}

			// Flag: move made -(i.e.)-> now it's opponent move
			mMonitor.notifyAll();
		}
	}

	/**
	 * Prepares one packet with all moves. It is called only on server side.
	 */
	private void prepareLoadGamePacket() {
		MoveState moveState;
		try {
			mCountDownLatch.await();
		} catch (InterruptedException e) {
			// do nothing
			Log.w(TAG, "Count down latch was interrupted");
		}

		// mStateRecorder.load();
		List<Integer> moves = mStateRecorder.getMoves();
		boolean isNextPlayerMove = mCPS.isNextPlayerMove();

		if (mCPS.getChangePlayerCounter() % 2 != 0) {
			// this goes to SERVER
			Message msg = mRefreshHandler.obtainMessage();
			msg.what = RefreshHandler.SET_MOVE_STATE;
			msg.obj = MoveState.OPPONENT_MOVE;
			mRefreshHandler.sendMessage(msg);

			mMoveState = MoveState.OPPONENT_MOVE;

			// this goes to CLIENT
			// isNextPlayerMove = true;
			moveState = MoveState.WAIT_FOR_MY_MOVE;
		} else {
			// number of changer is even => CLIENT move state the same as at the beginning of game =>
			// MoveState.OPPONENT_MOVE
			// this goes to CLIENT
			// isNextPlayerMove = false;
			mMoveState = MoveState.WAIT_FOR_MY_MOVE;
			moveState = MoveState.OPPONENT_MOVE;
		}

		mObjectToSend = new PacketContinueGameViaBluetooth(moves, isNextPlayerMove, moveState);
	}

	/**
	 * Shows connection error and finishes the current activity.
	 */
	private void showConnectionErrorAndFinishActivity() {
		closeThread();
		showToast("This device established no server");
		mRefreshHandler.sendEmptyMessage(RefreshHandler.DISMISS_DIALOG);
		mRefreshHandler.sendEmptyMessage(RefreshHandler.FINISH_ACTIVITY);
	}

	/**
	 * Shows toast message on the screen.
	 * 
	 * @param message
	 *            Message to show.
	 */
	public void showToast(String message) {
		Message msg = mRefreshHandler.obtainMessage();
		msg.what = RefreshHandler.TOAST_MSG;
		msg.obj = message;
		mRefreshHandler.sendMessage(msg);
	}

	/**
	 * Adds property change listener. It is like observer pattern, where all listeners are notified about any change of
	 * the game model.
	 * 
	 * @param listener
	 *            The object which will be informed of any property change.
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		mPropertyChangeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * Removes all property listeners.
	 */
	public void removePropertyChangeListeners() {
		PropertyChangeListener[] listeners = mPropertyChangeSupport.getPropertyChangeListeners();

		for (PropertyChangeListener listener : listeners) {
			mPropertyChangeSupport.removePropertyChangeListener(listener);
		}
	}
}
