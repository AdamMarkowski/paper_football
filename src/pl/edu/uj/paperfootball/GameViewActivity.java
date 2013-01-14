package pl.edu.uj.paperfootball;

import java.util.Timer;
import java.util.concurrent.CountDownLatch;

import pl.edu.uj.paperfootball.state.StateRecorder;
import pl.edu.uj.paperfootball.utils.RefreshHandler;
import pl.edu.uj.paperfootball.utils.SavedGamesView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.samm.common.SAMMLibConstants;
import com.samsung.samm.common.SOptionPlay;
import com.samsung.spensdk.applistener.AnimationProcessListener;
import pl.edu.uj.paperfootball.R;

/**
 * Activity with SCanvasView where you can draw game lines.
 */
public class GameViewActivity extends Activity implements
		AnimationProcessListener {

	private static final String TAG = GameViewActivity.class.getSimpleName();

	public static final String EXTRA_GAME_MODE = "ExtraGameMode";
	public static final int TWO_PLAYERS_ONE_PHONE = 3;
	public static final int TWO_PLAYER_ONE_PHONE_LOAD_GAME = 4;
	public static final int LOAD_GAME = 5;
	public static final int SERVER_LOAD_GAME = 6;
	public static final int HELP = 7;

	// Views
	private Button mShowGameReplayButton;
	private TextView mWhoseMove;
	private GameManager mGameManager;
	private GameSCanvasView mGameSCanvasViewTop;
	private ImageView mBallView;
	private ProgressDialog mDialog;

	// Game model
	private CurrentPlaygroundState mCPS;

	public static enum MoveState {
		OPPONENT_MOVE, WAIT_FOR_MY_MOVE, MY_MOVE_SEND_MOVES_VIA_BLUETOOTH
	};

	// Other
	private final Timer mConnectionTimeout;
	private final CountDownLatch mCountDownLatch;
	private RefreshHandler mRefreshHandler;
	private MoveState mMoveState;
	private int mGameMode;
	private Vibrator mVibrator;

	/**
	 * Initialization some of final fields.
	 */
	{
		mConnectionTimeout = new Timer();
		mCountDownLatch = new CountDownLatch(1);
	}

	/**
	 * Starts game with given game mode
	 * 
	 * @param savedInstanceState
	 *            Saved instance state.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		mGameMode = intent.getIntExtra(EXTRA_GAME_MODE, -1);

		initGame();

		switch (mGameMode) {
		case TWO_PLAYERS_ONE_PHONE:
			setMoveState(MoveState.WAIT_FOR_MY_MOVE);
			break;
		case TWO_PLAYER_ONE_PHONE_LOAD_GAME:
			setMoveState(MoveState.WAIT_FOR_MY_MOVE);
			break;
		default:
			Log.e("TAG", "Wrong game mode " + mGameMode);
		}
	}

	/**
	 * Initializes game objects.
	 */
	private void initGame() {

		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		mCPS = new CurrentPlaygroundState(displayMetrics.widthPixels,
				displayMetrics.heightPixels);
		mRefreshHandler = new RefreshHandler(this);

		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

		setContentView(R.layout.main);
		mBallView = (ImageView) findViewById(R.id.canvas_ball);
		mGameSCanvasViewTop = (GameSCanvasView) findViewById(R.id.canvas_view_top);
		mGameSCanvasViewTop.setGrid(mCPS.getGrid());
		mGameSCanvasViewTop.setCountDownLatch(mCountDownLatch);
		mGameSCanvasViewTop.setIsAllowDrawing(true);
		mGameSCanvasViewTop.setBall(this, mBallView);
		mGameSCanvasViewTop.setAnimationProcessListener(this);
		GameCanvasBottom gameCanvasBottom = (GameCanvasBottom) findViewById(R.id.canvas_view_bottom);
		mWhoseMove = (TextView) findViewById(R.id.whose_move);
		mShowGameReplayButton = (Button) findViewById(R.id.show_game_replay_button);
		mShowGameReplayButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showGameReplay();
			}
		});



		mGameManager = new GameManager(mGameSCanvasViewTop, gameCanvasBottom,
				mCPS, mRefreshHandler, this);
		mGameSCanvasViewTop.setBallParams(mCPS.getBallNode()
				.getCanvasCoordinateX(), mCPS.getBallNode()
				.getCanvasCoordinateY());
		mBallView.setVisibility(View.VISIBLE);

		int mNodeVisitNumber = mCPS.getBallNode().getNodeVisitNumber();
		mNodeVisitNumber++;
		mCPS.getBallNode().setNodeVisitNumber(mNodeVisitNumber);
	}

	/**
	 * Saves state to the database.
	 */
	@Override
	protected void onPause() {
		StateRecorder recorder = mGameManager.getStateRecorder();
		recorder.save();
		super.onPause();
	}

	/**
	 * Clears all views, interrupts all game threads.
	 */
	@Override
	protected void onDestroy() {
		dismissProgressDialog();
		clearDatabase();
		cancelConnectionTimeout();
		mCPS.clearChangePlayerCounter();
		mGameManager.onDestroy();
		mGameManager.removePropertyChangeListeners();

		super.onDestroy();
	}

	/**
	 * On back key pressed shows play again dialog when the game is finished or
	 * save game dialog if not.
	 */
	@Override
	public void onBackPressed() {
		mGameSCanvasViewTop.doAnimationStop(true);

		if (mGameManager.isGameFinished()) {
			showPlayAgainDialog();
		} else {
			showSaveGameDialog();
		}
	}

	@Override
	public void onChangeProgress(int arg0) {
		// do nothing
	}

	/**
	 * Called after animation ends.
	 */
	@Override
	public void onPlayComplete() {
		if (mMoveState == MoveState.WAIT_FOR_MY_MOVE) {
			mShowGameReplayButton.setVisibility(View.VISIBLE);
		}
		mGameSCanvasViewTop.setIsAllowDrawing(true);
		mBallView.setVisibility(View.VISIBLE);
		closeOptionsMenu();
	}

	/**
	 * Shows game replay
	 */
	private void showGameReplay() {
			doAnimationPlay();
	}

	/**
	 * Plays history of the game.
	 */
	private void doAnimationPlay() {
		mShowGameReplayButton.setVisibility(View.GONE);
		mBallView.setVisibility(View.INVISIBLE);
		mGameSCanvasViewTop.setIsAllowDrawing(false);
		mGameSCanvasViewTop.setAnimationMode(true);
		mGameSCanvasViewTop.setAnimationSpeed(SOptionPlay.ANIMATION_SPEED_FAST);

		Window window = getWindow();
		if (window != null) {
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		int nAnimationState = mGameSCanvasViewTop.getAnimationState();
		if (nAnimationState == SAMMLibConstants.ANIMATION_STATE_ON_STOP) {
			mGameSCanvasViewTop.doAnimationStart();
		} else if (nAnimationState == SAMMLibConstants.ANIMATION_STATE_ON_PAUSED) {
			mGameSCanvasViewTop.doAnimationResume();
		} else if (nAnimationState == SAMMLibConstants.ANIMATION_STATE_OFF_ANIMATION) {
			Toast.makeText(this, "animation mode = OFF", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * Clears database.
	 */
	private void clearDatabase() {
		StateRecorder stateRecorder = mGameManager.getStateRecorder();
		stateRecorder.delete(StateRecorder.CURRENT_GAMESTATE_NAME);
		stateRecorder.clear();
	}

	/**
	 * Dismisses progress dialog.
	 */
	public void dismissProgressDialog() {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
	}

	/**
	 * Cancels connection timeout.
	 */
	public void cancelConnectionTimeout() {
		mConnectionTimeout.cancel();
	}

	/**
	 * Shows or hide show game replay button.
	 */
	public void toggleShowGameReplayButton() {
		if (mShowGameReplayButton.getVisibility() == View.VISIBLE ? true
				: false) {
			mShowGameReplayButton.setVisibility(View.INVISIBLE);
		} else {
			mShowGameReplayButton.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Vibrates the phone.
	 */
	public void vibrate() {
		int vibrationTimeInMilliseconds = 800;

		if (mVibrator.hasVibrator()) {
			mVibrator.vibrate(vibrationTimeInMilliseconds);
		}
	}

	/**
	 * Returns to the previous move.
	 */
	public void repeatMove() {
		// TODO
	}

	/**
	 * Starts animation on SCanvasView.
	 */
	public void showReplay() {
		dismissProgressDialog();
		doAnimationPlay();
	}



	

	/**
	 * Shows dialog with options to save current game.
	 */
	private void showSaveGameDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.save_game_question))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.yes_button_text),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								final Intent intent = new Intent(
										GameViewActivity.this,
										SavedGamesView.class);
								startActivityForResult(intent, 0);
								finish();
							}
						})
				.setNegativeButton(getString(R.string.no_button_text),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								finish();
							}
						})
				.setNeutralButton(getString(R.string.cancel_button_text),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// do nothing
							}

						});
		builder.create().show();
	}

	/**
	 * Shows dialog with play again option.
	 */
	private void showPlayAgainDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.play_again_question))
				.setCancelable(true)
				.setPositiveButton(getString(R.string.yes_button_text),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								mGameManager.playGameAgain();
							}
						})
				.setNegativeButton(getString(R.string.no_button_text),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								finish();
							}
						});
		builder.create().show();
	}

	/**
	 * Updates the text view with information about whose move is now.
	 * 
	 * @param moveState
	 *            A value that indicates whose move should now be set.
	 */
	public void updateWhoseMoveView(MoveState moveState) {
		String moveText;

		if (moveState == MoveState.OPPONENT_MOVE) {
			moveText = getString(R.string.opponent_move);
		} else {
			moveText = getString(R.string.your_move);
		}

		mWhoseMove.setText(moveText);
	}

	public GameSCanvasView getGameSCanvasViewTop() {
		return mGameSCanvasViewTop;
	}

	public void setMoveState(MoveState moveState) {
		mMoveState = moveState;
	}

	public MoveState getMoveState() {
		return mMoveState;
	}
}