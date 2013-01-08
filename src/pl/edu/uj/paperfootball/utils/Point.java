package pl.edu.uj.paperfootball.utils;

import java.io.Serializable;

/**
 * Simple serializable point with X and Y coordinates.
 */
public class Point implements Serializable {

	private static final long serialVersionUID = 2381244268383472699L;

	private final int mX;
	private final int mY;

	/**
	 * Serializable class representing simple point.
	 * 
	 * @param x
	 *            X coordinate of the point.
	 * @param y
	 *            Y coordinate of the point.
	 */
	public Point(int x, int y) {
		mX = x;
		mY = y;
	}

	/**
	 * Copy constructor.
	 * 
	 * @param point
	 *            The point to copy.
	 */
	public Point(Point point) {
		this(point.mX, point.mY);
	}

	public int getX() {
		return mX;
	}

	public int getY() {
		return mY;
	}

	@Override
	public String toString() {
		final int expectedStringSize = 15;
		StringBuilder builder = new StringBuilder(expectedStringSize);
		builder.append("X: " + mX + " Y: " + mY);
		return builder.toString();
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 31 * result + mX;
		result = 31 * result + mY;
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof Point)) {
			return false;
		}

		Point point = (Point) o;

		return mX == point.mX && mY == point.mY;
	}
}
