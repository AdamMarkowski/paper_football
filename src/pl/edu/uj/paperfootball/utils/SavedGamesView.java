package pl.edu.uj.paperfootball.utils;

import java.util.List;

import pl.edu.uj.paperfootball.state.SqliteStateRecorder;
import pl.edu.uj.paperfootball.state.StateRecorder;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import pl.edu.uj.paperfootball.R;

/**
 * Activity with list of the saved games.
 */
public class SavedGamesView extends ListActivity {

	private EditText mEditText;
	private List<String> mGameNameList;
	private SqliteStateRecorder mStateRecorder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.save_game);

		Button saveButton = (Button) findViewById(R.id.saveGameBtn);
		mEditText = (EditText) findViewById(R.id.saveGameEditText);
		saveButton.setOnClickListener(mSaveButtonClickListener);

		mStateRecorder = new SqliteStateRecorder(this);

		// loads moves from db (current_game table) -to-> StateRecorder::mMoves
		mStateRecorder.load();

		// delete current_game from db
		mStateRecorder.delete(StateRecorder.CURRENT_GAMESTATE_NAME);

		// get list of all saved games
		mGameNameList = mStateRecorder.getGameStateList();

		OrderAdapter orderAdapter = new OrderAdapter(this, R.layout.save_game_item, mGameNameList);
		setListAdapter(orderAdapter);
	}

	/**
	 * On click listener for save button.
	 */
	private final OnClickListener mSaveButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			String nameOfGameToSave = mEditText.getText().toString();
			mStateRecorder.save(nameOfGameToSave);
			finish();
		}
	};

	/**
	 * Adapter for this list activity.
	 */
	private class OrderAdapter extends ArrayAdapter<String> {

		private final List<String> mItems;

		/**
		 * Custom array adapter for the list activity.
		 * 
		 * @param context
		 *            Application context.
		 * @param textViewResourceId
		 *            Resource ID indicates TextView.
		 * @param items
		 *            Items to show.
		 */
		public OrderAdapter(Context context, int textViewResourceId, List<String> items) {
			super(context, textViewResourceId, items);
			mItems = items;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public View getView(final int position, View view, ViewGroup parent) {
			View convertView = view;

			if (convertView == null) {
				LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(R.layout.save_game_item, null);
			}

			final TextView textView = (TextView) convertView.findViewById(R.id.game_saved_tv);
			textView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					mEditText.setText(textView.getText());
				}
			});

			final ImageButton itemButton = (ImageButton) convertView.findViewById(R.id.trash_icon);
			itemButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					List<String> items = mItems;
					getListView().invalidateViews();

					mStateRecorder.delete(mGameNameList.get(position));
					mGameNameList = mStateRecorder.getGameStateList();
					items.remove(position);
				}
			});

			final String itemText = mItems.get(position);

			if (itemText != null) {
				textView.setText(itemText);
				textView.setTag("trash");
			}

			return convertView;
		}
	}
}
