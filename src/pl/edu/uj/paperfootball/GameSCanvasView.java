package pl.edu.uj.paperfootball;

import java.util.concurrent.CountDownLatch;

import pl.edu.uj.paperfootball.utils.MoveLineForCanvas;
import pl.edu.uj.paperfootball.utils.Point;

import junit.framework.Assert;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;

import com.samsung.samm.common.SObjectStroke;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;
import com.samsung.spensdk.applistener.SPenTouchListener;
import pl.edu.uj.paperfootball.R;

/**
 * SCanvasView where you can draw game lines.
 */
public class GameSCanvasView extends SCanvasView {

	private GameManager mGameManager;
	private ImageView mBallView;
	private MarginLayoutParams mBallParams;
	private CountDownLatch mCountDownLatch;

	private boolean mAllowDrawing;
	private int mBallWidth;
	private int mBallHeight;
	private int mGrid;

	/**
	 * SCanvasView initialize listener.
	 */
	private final SCanvasInitializeListener mSCanvasInitializeListener = new SCanvasInitializeListener() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onInitialized() {
			setMultiTouchCancel(false);
			setRemoveLongPressStroke(false);
			setCanvasZoomEnable(false);
			setCanvasPanEnable(false);
			setCanvasSupportPenOnly(true);
			setSPenTouchListener(mSPenTouchListener);

			mGameManager.loadFromStateRecorder();
			mCountDownLatch.countDown();
		}
	};

	/**
	 * S Pen touch listener, which recognizes S Pen and finger touch events.
	 */
	private final SPenTouchListener mSPenTouchListener = new SPenTouchListener() {
		private int mDownX;
		private int mDownY;

		@Override
		public boolean onTouchFinger(View view, MotionEvent event) {
			onTouchPen(view, event);
			return true;
		}

		@Override
		public boolean onTouchPen(View view, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mDownX = (int) event.getX();
				mDownY = (int) event.getY();
			} else if (event.getAction() == MotionEvent.ACTION_UP && mAllowDrawing) {
				mGameManager.performMoveFromSCanvas(new Point(mDownX, mDownY), new Point((int) event.getX(),
						(int) event.getY()));
			}

			return true;
		}

		@Override
		public void onTouchButtonDown(View arg0, MotionEvent arg1) {
			// do nothing
		}

		@Override
		public void onTouchButtonUp(View arg0, MotionEvent arg1) {
			// do nothing
		}

		@Override
		public boolean onTouchPenEraser(View arg0, MotionEvent arg1) {
			// do nothing
			return false;
		}
	};

	/**
	 * Constructs GameSCanvasView.
	 * 
	 * @param context
	 *            Application context.
	 */
	public GameSCanvasView(Context context) {
		super(context);
		setInitializeListener();
	}

	/**
	 * Constructs GameSCanvasView.
	 * 
	 * @param context
	 *            Application context.
	 * @param attrs
	 *            Attributes from XML layout.
	 */
	public GameSCanvasView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setInitializeListener();
	}

	/**
	 * Constructs GameSCanvasView.
	 * 
	 * @param context
	 *            Application context.
	 * @param gameManager
	 *            Game manager object.
	 */
	public GameSCanvasView(Context context, GameManager gameManager) {
		super(context);
		mGameManager = gameManager;
		setInitializeListener();
	}

	/**
	 * Sets SCanvasView initialize listener.
	 */
	private void setInitializeListener() {
		setSCanvasInitializeListener(mSCanvasInitializeListener);
	}

	/**
	 * Draws game line.
	 * 
	 * @param moveLine
	 *            Represents single move line.
	 */
	public void drawLine(MoveLineForCanvas moveLine) {
		Point startPonint = moveLine.getStart();
		Point endPonint = moveLine.getEnd();
		int x1 = startPonint.getX();
		int y1 = startPonint.getY();
		int x2 = endPonint.getX();
		int y2 = endPonint.getY();
		int color = moveLine.getColor();
		int size = moveLine.getWidth();

		int dx = x2 - x1;
		int dy = y2 - y1;
		float pointsAmount;

		int pointsNumInGrid = 40;
		float length = FloatMath.sqrt(dx * dx + dy * dy);
		if (x1 == x2 && y1 == y2) {
			pointsAmount = 2;
		} else {
			pointsAmount = length / mGrid * pointsNumInGrid;
		}

		PointF[] points = new PointF[(int) pointsAmount];
		int ile = points.length;
		float[] nPenPressures = new float[ile];

		float deltaDx = dx / pointsAmount;
		float deltaDy = dy / pointsAmount;

		for (int i = 0; i < points.length; i++) {
			points[i] = new PointF(x1 + deltaDx * i, y1 + deltaDy * i);
			nPenPressures[i] = 1.0f;
		}

		// the pen stroke creation
		SObjectStroke sStrokeObject = new SObjectStroke();
		sStrokeObject.setColor(color);
		sStrokeObject.setStyle(SObjectStroke.SAMM_STROKE_STYLE_PENCIL);
		sStrokeObject.setSize(size);

		sStrokeObject.setPoints(points);
		sStrokeObject.setPressures(nPenPressures);
		sStrokeObject.setMetaData(SObjectStroke.SAMM_METASTATE_PEN);

		insertSAMMStroke(sStrokeObject);
	}

	/**
	 * Sets ball bitmap and sizes.
	 * 
	 * @param context
	 *            Application context.
	 * @param ballView
	 *            ImageView object.
	 */
	public void setBall(Context context, ImageView ballView) {
		mBallView = ballView;
		mBallParams = (MarginLayoutParams) ballView.getLayoutParams();

		BitmapFactory.Options ballOptions = new BitmapFactory.Options();
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ball, ballOptions);
		Assert.assertNotNull(bmp);
		mBallWidth = bmp.getWidth();
		mBallHeight = bmp.getHeight();
	}

	/**
	 * Sets ball layout parameters.
	 * 
	 * @param ballCanvasPositionX
	 *            X coordinate of the position.
	 * @param ballCanvasPositionY
	 *            Y coordinate of the position.
	 */
	public void setBallParams(int ballCanvasPositionX, int ballCanvasPositionY) {
		mBallParams.leftMargin = ballCanvasPositionX - mBallWidth / 2;
		mBallParams.topMargin = ballCanvasPositionY - mBallHeight / 2;
		mBallView.setLayoutParams(mBallParams);
	}

	public void setCountDownLatch(CountDownLatch countDownLatch) {
		mCountDownLatch = countDownLatch;
	}

	public void setGrid(int grid) {
		mGrid = grid;
	}

	public void setGameManager(GameManager gameManager) {
		mGameManager = gameManager;
	}

	public void setIsAllowDrawing(boolean allowDrawing) {
		mAllowDrawing = allowDrawing;
	}
}
