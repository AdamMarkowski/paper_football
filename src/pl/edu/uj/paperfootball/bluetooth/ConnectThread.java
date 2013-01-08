package pl.edu.uj.paperfootball.bluetooth;

import java.io.IOException;
import java.util.UUID;

import pl.edu.uj.paperfootball.utils.RefreshHandler;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.util.Log;


/**
 * It is a client thread, which allows the client to connect to the server.
 */
public class ConnectThread extends Thread {

	private static final String TAG = ConnectThread.class.getSimpleName();

	private final BluetoothDevice mDevice;
	private final GameThread mGameThread;
	private final RefreshHandler mRefreshHandler;
	private BluetoothSocket mSocket;

	// Local Bluetooth adapter
	private final BluetoothAdapter mBluetoothAdapter;

	// Unique UUID for this application
	private static final UUID MY_UUID = UUID.fromString("352e9381-f06a-11e1-aff1-0800200c9a66");

	/**
	 * Constructs connect thread.
	 * 
	 * @param bluetoothAdapter
	 *            BluetoothAdapter object to cancel ongoing discovery.
	 * @param device
	 *            BluetoothDevice to obtain Socket object.
	 * @param gameThread
	 *            Main game thread.
	 * @param refreshHandler
	 *            Refresh handler which handles every UI change.
	 */
	public ConnectThread(BluetoothAdapter bluetoothAdapter, BluetoothDevice device, GameThread gameThread,
			RefreshHandler refreshHandler) {
		mBluetoothAdapter = bluetoothAdapter;
		mRefreshHandler = refreshHandler;
		mDevice = device;
		mGameThread = gameThread;
		setName(TAG);
	}

	@Override
	public void run() {
		// Cancel discovery because it will slow down the connection
		mBluetoothAdapter.cancelDiscovery();

		// Get a BluetoothSocket to connect with the given BluetoothDevice
		try {
			// MY_UUID is the app's UUID string, also used by the server code
			mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
			showConnectionErrorAndFinishActivity();
		}

		try {
			// Connect the device through the socket. This will block until it succeeds or throws an exception
			mSocket.connect();
			mGameThread.setSocketAndInitializeStreams(mSocket);
			mGameThread.start();
			// Now connection is established
			mRefreshHandler.sendEmptyMessage(RefreshHandler.DISMISS_DIALOG);
		} catch (IOException connectException) {
			// Unable to connect; close the socket and get out
			showConnectionErrorAndFinishActivity();
		}
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
	 * Will cancel an in-progress connection, and close the socket.
	 */
	public void closeThread() {
		try {
			if (mSocket != null) {
				mSocket.close();
			}
		} catch (IOException e) {
			Log.w(TAG, "mSocket.close() failed");
		}
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
}
