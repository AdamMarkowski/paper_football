package pl.edu.uj.paperfootball.utils;

/**
 * Represents view of the move line.
 */
public class MoveLineForCanvas {

	private final Point mStart;
	private final Point mEnd;
	private final int mColor;
	private final int mWidth;

	/**
	 * Constructs move line.
	 * 
	 * @param start
	 *            Indicates where the line starts.
	 * @param end
	 *            Indicates where the line ends.
	 * @param color
	 *            Color of the line.
	 * @param width
	 *            Width of the line.
	 */
	public MoveLineForCanvas(Point start, Point end, int color, int width) {
		super();
		mStart = start;
		mEnd = end;
		mColor = color;
		mWidth = width;
	}

	public Point getStart() {
		return mStart;
	}

	public Point getEnd() {
		return mEnd;
	}

	public int getColor() {
		return mColor;
	}

	public int getWidth() {
		return mWidth;
	}

	@Override
	public String toString() {
		final int expectedStringSize = 60;
		StringBuilder builder = new StringBuilder(expectedStringSize);
		builder.append(mStart.toString()).append(mEnd.toString()).append(" Color: " + mColor + " Width: " + mWidth);
		return builder.toString();
	}
}
