package pl.edu.uj.paperfootball.bluetooth;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import pl.edu.uj.paperfootball.utils.RefreshHandler;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.SystemClock;
import android.util.Log;


/**
 * Threads that waits for the client at server socket accept method.
 */
public class AcceptThread extends Thread {

	private static final String TAG = AcceptThread.class.getSimpleName();

	private final GameThread mGameThread;
	private final RefreshHandler mRefreshHandler;
	private BluetoothServerSocket mServerSocket;

	// Name for the SDP record when creating server socket
	private static final String NAME = "FootballPlayers";
	private final BluetoothAdapter mBluetoothAdapter;
	private final AtomicBoolean mRunThread;

	// Unique UUID for this application
	private static final UUID MY_UUID = UUID.fromString("352e9381-f06a-11e1-aff1-0800200c9a66");

	/**
	 * Constructs accept thread which accepts clients.
	 * 
	 * @param bluetoothAdapter
	 *            Needed for ServerSocket object.
	 * @param gameThread
	 *            Main game thread.
	 * @param refreshHandler
	 *            Refresh handler which handles every UI change.
	 */
	public AcceptThread(BluetoothAdapter bluetoothAdapter, GameThread gameThread, RefreshHandler refreshHandler) {
		mBluetoothAdapter = bluetoothAdapter;
		mRefreshHandler = refreshHandler;
		mGameThread = gameThread;
		mRunThread = new AtomicBoolean(true);
		setName(TAG);
	}

	@Override
	public void run() {
		// Keep listening until exception occurs or a socket is returned
		while (mRunThread.get()) {
			try {
				// MY_UUID is the app's UUID string, also used by the client code
				while (mServerSocket == null) {
					SystemClock.sleep(2000);
					mServerSocket = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID);
				}
			} catch (IOException e) {
				mRefreshHandler.sendEmptyMessage(RefreshHandler.BLUETOOTH_ERROR);
				break; // goto cancelServerSocket();
			}

			BluetoothSocket socket = null;

			while (true) {
				try {
					while (mRunThread.get()) {
						socket = mServerSocket.accept(1000);

						if (socket != null) {
							mRunThread.set(false);
						}
					}
				} catch (IOException e) {
					// exception driven development
					if (mRunThread.get() && socket == null) {
						continue;
					} else {
						break;
					}
				}

				// If connection was accepted
				// Null dereference is not necessary, because before we were waiting for socket to be not null
				if (socket != null) {
					boolean initializationResult = mGameThread.setSocketAndInitializeStreams(socket);

					if (initializationResult) {
						mGameThread.start();
						mRefreshHandler.sendEmptyMessage(RefreshHandler.DISMISS_DIALOG);
					} else {
						// Continue waiting for client
						mRunThread.set(true);
						continue;
					}
				} else {
					// Continue waiting for client
					mRunThread.set(true);
					continue;
				}

				if (!mRunThread.get()) {
					break;
				}
			}

			// Ends main loop and thread
			mRunThread.set(false);
		}
		cancelServerSocket();
	}

	/**
	 * Cancels this thread - it is visible outside the thread.
	 */
	public void cancelThread() {
		mRunThread.set(false);
	}

	/**
	 * Will cancel the listening socket, and cause the thread to finish.
	 */
	private void cancelServerSocket() {
		try {
			if (mServerSocket != null) {
				mServerSocket.close();
			}
		} catch (IOException e) {
			Log.w(TAG, "mServerSocket.close() failed");
		}
	}
}
