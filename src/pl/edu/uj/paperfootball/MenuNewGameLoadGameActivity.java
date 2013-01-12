package pl.edu.uj.paperfootball;

import pl.edu.uj.paperfootball.utils.LoadGame;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import pl.edu.uj.paperfootball.R;

/**
 * Activity represents menu to load saved games or start a new game.
 */
public class MenuNewGameLoadGameActivity extends Activity {

	private int mGameMode;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_new_game_or_load_game);

		final Intent intent = getIntent();
		mGameMode = intent.getIntExtra(GameViewActivity.EXTRA_GAME_MODE, -1);
	}

	/**
	 * Called when new game or load game button is pressed.
	 * 
	 * @param view
	 *            View of the pressed button.
	 */
	public void onClick(View view) {
		boolean newGame;

		switch (view.getId()) {
		case R.id.new_game_button:
			newGame = true;
			break;
		case R.id.load_game_button:
			newGame = false;
			break;
		default:
			throw new IllegalArgumentException("Wrong game mode button "
					+ view.getId());
		}

		startGame(newGame);
	}

	/**
	 * Starts game activity.
	 * 
	 * @param newGame
	 *            Value that indicates whether we should start a new game or
	 *            load a saved game.
	 */
	private void startGame(boolean newGame) {
		final Intent intent;

		if (newGame) {
			intent = new Intent(this, GameViewActivity.class);
			intent.putExtra(GameViewActivity.EXTRA_GAME_MODE, mGameMode);
		} else {
			intent = new Intent(this, LoadGame.class);

			if (mGameMode == GameViewActivity.TWO_PLAYERS_ONE_PHONE) {
				// LOAD_GAME
				intent.putExtra(GameViewActivity.EXTRA_GAME_MODE,
						GameViewActivity.TWO_PLAYER_ONE_PHONE_LOAD_GAME);
			} else {

			}
		}

		startActivityForResult(intent, 0);
	}
}