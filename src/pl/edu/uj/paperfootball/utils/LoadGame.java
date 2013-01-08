package pl.edu.uj.paperfootball.utils;

import java.util.List;

import pl.edu.uj.paperfootball.GameViewActivity;
import pl.edu.uj.paperfootball.state.SqliteStateRecorder;
import pl.edu.uj.paperfootball.state.StateRecorder;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import pl.edu.uj.paperfootball.R;

/**
 * Utility class which is responsible for game loading.
 */
public class LoadGame extends Activity {

	private List<String> mGameNames;
	private SqliteStateRecorder mStateRecorder;
	private int mGameMode;

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.saved_games_list);
		ListView listView = (ListView) findViewById(R.id.mylist);

		Intent intent = getIntent();
		mGameMode = intent.getIntExtra(GameViewActivity.EXTRA_GAME_MODE, -1);

		mStateRecorder = new SqliteStateRecorder(this);

		// delete current_game from db
		mStateRecorder.delete(StateRecorder.CURRENT_GAMESTATE_NAME);

		mGameNames = mStateRecorder.getGameStateList();

		// First parameter - Context
		// Second parameter - Layout for the row
		// Third parameter - ID of the TextView to which the data is written
		// Fourth - the Array of data
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				android.R.id.text1, mGameNames);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				String gamName = mGameNames.get(position);
				mStateRecorder.load(gamName);
				mStateRecorder.save();

				switch (mGameMode) {
				case GameViewActivity.TWO_PLAYER_ONE_PHONE_LOAD_GAME:
				case GameViewActivity.SERVER_LOAD_GAME:
					startGameActivity(mGameMode);
					break;
				default:
					throw new IllegalArgumentException("Illegal game mode " + mGameMode);
				}

				finish();
			}
		});
	};

	/**
	 * Starts game view activity with given game mode.
	 * 
	 * @param gameMode
	 *            Mode of the game.
	 * 
	 */
	public void startGameActivity(int gameMode) {
		Intent intent = new Intent(LoadGame.this, GameViewActivity.class);
		intent.putExtra(GameViewActivity.EXTRA_GAME_MODE, gameMode);
		startActivityForResult(intent, 0);
	}
}
