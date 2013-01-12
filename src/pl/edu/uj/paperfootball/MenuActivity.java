package pl.edu.uj.paperfootball;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import pl.edu.uj.paperfootball.R;

/**
 * Activity represents main menu of the game.
 */
public class MenuActivity extends Activity {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);
	}

	/**
	 * Called when any menu button is pressed.
	 * 
	 * @param view
	 *            Pressed button view.
	 */
	public void onClick(View view) {
		int gameMode = GameViewActivity.TWO_PLAYERS_ONE_PHONE;

		switch (view.getId()) {
		case R.id.two_players_on_one_phone_button:
//			gameMode = GameViewActivity.TWO_PLAYERS_ONE_PHONE;
			startGame(gameMode);
			break;
		case R.id.help_button:
//			gameMode = GameViewActivity.HELP;
			showGameHelp();
			break;
		default:
			throw new IllegalArgumentException("Wrong game mode button " + view.getId());
		}

		
//		if (gameMode == GameViewActivity.HELP) {
//			showGameHelp();
//		} else {
//			startGame(gameMode);	
//		}		
	}

	/**
	 * Starts game activity or menu activity with options to load or start new game.
	 * 
	 * @param gameMode
	 *            Mode of the game. One of these values: {@value GameViewActivity#SERVER},
	 *            {@value GameViewActivity#CLIENT}, {@value GameViewActivity#TWO_PLAYERS_ONE_PHONE}.
	 */
	private void startGame(int gameMode) {
		Intent intent = null;
		
		intent = new Intent(this, MenuNewGameLoadGameActivity.class);
		intent.putExtra(GameViewActivity.EXTRA_GAME_MODE, gameMode);
		startActivityForResult(intent, 0);
	}
	
	private void showGameHelp() {		
		    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    dialog.setMessage(R.string.game_description);
		    dialog.setPositiveButton(" OK ", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int id) {
		            dialog.dismiss();

		        }
		    });
		    dialog.show();
	}
}