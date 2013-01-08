package pl.edu.uj.paperfootball;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Bottom view that lies under SCanvasView. It has drawn field and nodes.
 */
public class GameCanvasBottom extends View {

	private final Paint mPaint;
	private GameManager mGameManager;
	private Canvas mCanvas;

	/**
	 * Constructs the bottom view.
	 * 
	 * @param context
	 *            Application context.
	 */
	public GameCanvasBottom(Context context) {
		super(context);
		mPaint = initializePaint();
	}

	/**
	 * Constructs the bottom view from XML layout.
	 * 
	 * @param context
	 *            Application context.
	 * @param attrs
	 *            Attributes from XML.
	 */
	public GameCanvasBottom(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = initializePaint();
	}

	/**
	 * Initializes Paint object.
	 * 
	 * @return Pain object.
	 */
	private Paint initializePaint() {
		return new Paint();
	}

	/**
	 * Draw nodes and the field lines.
	 * 
	 * @param canvas
	 *            Canvas on which to draw.
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		mCanvas = canvas;
		mGameManager.drawNodesOnCanvasBottom();
		mGameManager.drawLinesOfFieldOnCanvasBottom();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = View.MeasureSpec.getSize(widthMeasureSpec);
		int height = View.MeasureSpec.getSize(heightMeasureSpec);
		setMeasuredDimension(width, height);
	}

	/**
	 * Draws node.
	 * 
	 * @param xCanvasCoordinate
	 *            X coordinate of the node.
	 * @param yCanvasCoordinate
	 *            Y coordinate of the node.
	 * @param radius
	 *            Radius of the node.
	 * @param nodeColor
	 *            Color of the node.
	 */
	public void drawNode(int xCanvasCoordinate, int yCanvasCoordinate, int radius, int nodeColor) {
		mPaint.setColor(nodeColor);
		mPaint.setAntiAlias(true);
		mCanvas.drawCircle(xCanvasCoordinate, yCanvasCoordinate, radius, mPaint);
	}

	/**
	 * Draws field line.
	 * 
	 * @param xStartCanvas
	 *            X coordinate of where to start to draw.
	 * @param yStartCanvas
	 *            Y coordinate of where to start to draw.
	 * @param xEndCanvas
	 *            X coordinate of where to end to draw.
	 * @param yEndCanvas
	 *            Y coordinate of where to end to draw.
	 * @param lineColor
	 *            Color of the field line.
	 * @param lineWidth
	 *            Width of the field line.
	 */
	public void drawLine(int xStartCanvas, int yStartCanvas, int xEndCanvas, int yEndCanvas, int lineColor,
			int lineWidth) {
		mPaint.setColor(lineColor);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(lineWidth);
		mCanvas.drawLine(xStartCanvas, yStartCanvas, xEndCanvas, yEndCanvas, mPaint);
	}

	public void setGameManager(GameManager gameManager) {
		mGameManager = gameManager;
	}
}
