package pl.edu.uj.paperfootball.bluetooth;

import java.util.Set;

import junit.framework.Assert;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import pl.edu.uj.paperfootball.R;

/**
 * Activity which allows user choose device to connect.
 */
public class DeviceListActivity extends Activity {
	/**
	 * An extra for intent.
	 */
	public static final String EXTRA_DEVICE_ADDRESS = "DEVICE ADDRESS";

	private BluetoothAdapter mBluetoothAdapter;
	private DeviceAdapter<Device> mPairedDevicesArrayAdapter;
	private DeviceAdapter<Device> mNewDevicesArrayAdapter;
	private Button mScanButton;

	/**
	 * The on-click listener for all devices in the ListViews.
	 */
	private final OnItemClickListener mDeviceClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// Cancel discovery because it's costly and we're about to connect
			mBluetoothAdapter.cancelDiscovery();

			// Obtain Device information from DeviceArray
			Device device = (Device) parent.getItemAtPosition(position);
			String deviceAddress = device.getDeviceAddress();

			if (deviceAddress != null) {
				// Create the result Intent and include the MAC address
				Intent intent = new Intent();
				intent.putExtra(EXTRA_DEVICE_ADDRESS, deviceAddress);

				// Set result and finish this Activity
				setResult(Activity.RESULT_OK, intent);
				DeviceListActivity.this.finish();
			}
		}
	};

	/**
	 * The BroadcastReceiver that listens for discovered devices and changes the title when discovery is finished.
	 */
	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					String deviceName = device.getName();
					if (deviceName != null && !deviceName.isEmpty()) {
						mNewDevicesArrayAdapter.add(new Device(deviceName, device.getAddress()));
					}
				}
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				setTitle(R.string.select_device);
				if (mNewDevicesArrayAdapter.getCount() == 0) {
					mNewDevicesArrayAdapter.add(new Device(getString(R.string.none_found), null));
				}
				mScanButton.setVisibility(View.VISIBLE);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_list);

		mScanButton = (Button) findViewById(R.id.button_scan);

		initalizeAdaptersAndListViews();
		initializeIntents();

		// Get the local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		Assert.assertNotNull(mBluetoothAdapter);

		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

		// If there are paired devices, add each one to the ArrayAdapter
		if (!pairedDevices.isEmpty()) {
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices) {
				mPairedDevicesArrayAdapter.add(new Device(device.getName(), device.getAddress()));
			}
		} else {
			mPairedDevicesArrayAdapter.add(new Device(getString(R.string.none_paired), null));
		}
	}

	/**
	 * Called when search for devices button is pressed.
	 * 
	 * @param view
	 *            Pressed view (in this particular case it's button).
	 */
	public void onClick(View view) {
		mNewDevicesArrayAdapter.clear();
		doDiscovery();
		view.setVisibility(View.GONE);
	}

	private void initalizeAdaptersAndListViews() {
		mPairedDevicesArrayAdapter = new DeviceAdapter<Device>(this, R.layout.device);
		mNewDevicesArrayAdapter = new DeviceAdapter<Device>(this, R.layout.device);

		// Find and set up the ListView for paired devices
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);

		// Find and set up the ListView for newly discovered devices
		ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);
	}

	/**
	 * Initializes intents.
	 */
	private void initializeIntents() {
		// Register for broadcasts when a device is discovered
		IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mBroadcastReceiver, intentFilter);

		// Register for broadcasts when discovery has finished
		intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mBroadcastReceiver, intentFilter);
	}

	@Override
	protected void onDestroy() {
		// Make sure we're not doing discovery anymore
		if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}

		// Unregister broadcast listeners
		unregisterReceiver(mBroadcastReceiver);

		super.onDestroy();
	}

	/**
	 * Start device discover with the BluetoothAdapter.
	 */
	private void doDiscovery() {
		// Indicate scanning in the title
		setTitle(R.string.scanning);

		// Turn on sub-title for new devices if is not visible at the moment
		View otherAvailableDevices = findViewById(R.id.title_new_devices);

		if (otherAvailableDevices.getVisibility() != View.VISIBLE) {
			otherAvailableDevices.setVisibility(View.VISIBLE);
		}

		// If we're already discovering, stop it
		if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}

		// Request discover from BluetoothAdapter
		mBluetoothAdapter.startDiscovery();
	}

	/**
	 * Sets result to {@value Activity#RESULT_CANCELED} and finishes the activity.
	 */
	@Override
	public void onBackPressed() {
		setResult(Activity.RESULT_CANCELED);
		finish();
	};

	/**
	 * Class that contains device name and MAC address.
	 */
	private static class Device {
		private final String mDeviceName;
		private final String mDeviceAddress;

		/**
		 * Constructor for device object.
		 * 
		 * @param deviceName
		 *            Device name obtained from BluetoothDevice object.
		 * @param deviceAddress
		 *            Device MAC address obtained from BluetoothDevice object.
		 */
		public Device(String deviceName, String deviceAddress) {
			super();
			mDeviceName = deviceName;
			mDeviceAddress = deviceAddress;
		}

		public String getDeviceName() {
			return mDeviceName;
		}

		public String getDeviceAddress() {
			return mDeviceAddress;
		}
	}

	/**
	 * ArrayAdapter which contains Device objects, but returns TextView just with device name.
	 * 
	 * @param <T>
	 *            An object inheriting from the Device class.
	 */
	private class DeviceAdapter<T extends Device> extends ArrayAdapter<T> {

		/**
		 * Resource ID which will be inflated into TextView.
		 */
		private final int mResource;

		/**
		 * {@inheritDoc}
		 */
		public DeviceAdapter(Context context, int resource) {
			super(context, resource);
			mResource = resource;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Device device = getItem(position);
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			TextView deviceTextView = (TextView) layoutInflater.inflate(mResource, parent, false);
			deviceTextView.setText(device.getDeviceName());
			return deviceTextView;
		}

	}
}
