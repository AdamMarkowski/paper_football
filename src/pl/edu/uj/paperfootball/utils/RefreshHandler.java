package pl.edu.uj.paperfootball.utils;

import java.lang.ref.WeakReference;

import pl.edu.uj.paperfootball.GameViewActivity;
import pl.edu.uj.paperfootball.GameViewActivity.MoveState;


import android.os.Handler;
import android.os.Message;
import android.widget.Toast;


/**
 * Handler which handles operations which must be done in UI thread.
 */
public class RefreshHandler extends Handler {

	public static final int GAME_POINT = 1;
	public static final int TOAST_MSG = 2;
	public static final int MOVE_LINE = 3;
	public static final int FINISH_ACTIVITY = 4;
	public static final int SET_MOVE_STATE = 5;
	public static final int DISMISS_DIALOG = 6;
	public static final int BLUETOOTH_ERROR = 7;
	public static final int VIBRATE = 8;
	public static final int REPEAT_MOVE = 9;
	public static final int SHOW_REPLAY = 10;
	public static final int SHOW_CHOOSE_ANSWER = 11;

	private final WeakReference<GameViewActivity> mWeakReference;

	/**
	 * Constructor of the handler.
	 * 
	 * @param activity
	 *            Main game activity.
	 */
	public RefreshHandler(GameViewActivity activity) {
		super();
		mWeakReference = new WeakReference<GameViewActivity>(activity);
	}

	/**
	 * Handles every incoming message.
	 */
	@Override
	public void handleMessage(Message message) {
		GameViewActivity activity = mWeakReference.get();

		switch (message.what) {
		case TOAST_MSG:
			if (activity != null) {
				final String text = (String) message.obj;
				Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
			}
			break;
		case GAME_POINT:
			if (activity != null) {
				final Point point = (Point) message.obj;
				activity.getGameSCanvasViewTop().setBallParams(point.getX(), point.getY());
			}
			break;
		case MOVE_LINE:
			if (activity != null) {
				final MoveLineForCanvas moveLine = (MoveLineForCanvas) message.obj;
				activity.getGameSCanvasViewTop().drawLine(moveLine);
			}
			break;
		case FINISH_ACTIVITY:
			if (activity != null) {
				activity.finish();
			}
			break;
		case SET_MOVE_STATE:
			if (activity != null) {
				final MoveState moveState = (MoveState) message.obj;
				activity.setMoveState(moveState);
				activity.updateWhoseMoveView(moveState);
				activity.toggleShowGameReplayButton();
			}
			break;
		case DISMISS_DIALOG:
			if (activity != null) {
				activity.dismissProgressDialog();
				activity.cancelConnectionTimeout();
			}
			break;
		case BLUETOOTH_ERROR:
			if (activity != null) {
				Toast.makeText(activity, "Bluetooth connection error", Toast.LENGTH_SHORT).show();
				activity.finish();
			}
			break;
		case VIBRATE:
			if (activity != null) {
				activity.vibrate();
			}
			break;
		case REPEAT_MOVE:
			if (activity != null) {
				activity.repeatMove();
			}
			break;
		case SHOW_REPLAY:
			if (activity != null) {
				activity.showReplay();
			}
			break;
		default:
			throw new IllegalArgumentException("Wrong message " + message.what);
		}

		activity = null;
	}
}
