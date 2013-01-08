package pl.edu.uj.paperfootball.utils;

/**
 * Model of the single game node.
 */
public class Node {

	private final NodeType mNodeType;
	private final Point mCanvasCoordinates;
	private final Point mPositionInNodesMatrix;
	private boolean mBounces;
	private int mVisitNumber;

	/**
	 * Enumeration representing type of available nodes.
	 */
	public enum NodeType {
		NODE_NO_TYPE, NODE_GOAL, NODE_PITCH, NODE_HORIZONTAL_WALL, NODE_VERTICAL_WALL, NODE_CORNER
	};

	/**
	 * Constructs single node.
	 * 
	 * @param xCanvasCoordinate
	 *            X coordinate of the node.
	 * @param yCanvasCoordinate
	 *            Y coordinate of the node.
	 * @param xPositionInNodesMatrix
	 *            X position in the node matrix.
	 * @param yPositionInNodesMatrix
	 *            Y position in the node matrix.
	 * @param bounces
	 *            Indicates whether node is bounces.
	 * @param nodeType
	 *            Type of the node.
	 * @param visitNumber
	 *            Number of visits at this node.
	 */
	public Node(int xCanvasCoordinate, int yCanvasCoordinate, int xPositionInNodesMatrix, int yPositionInNodesMatrix,
			boolean bounces, NodeType nodeType, int visitNumber) {
		mCanvasCoordinates = new Point(xCanvasCoordinate, yCanvasCoordinate);
		mPositionInNodesMatrix = new Point(xPositionInNodesMatrix, yPositionInNodesMatrix);
		mBounces = bounces;
		mNodeType = nodeType;
		mVisitNumber = visitNumber;
	}

	public boolean isBounces() {
		return mBounces;
	}

	public void setBounces(boolean bounces) {
		mBounces = bounces;
	}

	public NodeType getNodeType() {
		return mNodeType;
	}

	public int getCanvasCoordinateX() {
		return mCanvasCoordinates.getX();
	}

	public int getCanvasCoordinateY() {
		return mCanvasCoordinates.getY();
	}

	public Point getCanvasCoordinates() {
		return mCanvasCoordinates;
	}

	public int getNodePositionX() {
		return mPositionInNodesMatrix.getX();
	}

	public int getNodePositionY() {
		return mPositionInNodesMatrix.getY();
	}

	public int getNodeVisitNumber() {
		return mVisitNumber;
	}

	public void setNodeVisitNumber(int visitNumber) {
		mVisitNumber = visitNumber;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof Node)) {
			return false;
		}

		Node node = (Node) o;

		return mVisitNumber == node.mVisitNumber && mBounces == node.mBounces && mNodeType == node.mNodeType
				&& mCanvasCoordinates.equals(node.mCanvasCoordinates)
				&& mPositionInNodesMatrix.equals(node.mPositionInNodesMatrix);
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 31 * result + (mBounces ? 1 : 0);
		result = 31 * result + mNodeType.hashCode();
		result = 31 * result + mVisitNumber;
		result = 31 * result + mCanvasCoordinates.hashCode();
		result = 31 * result + mPositionInNodesMatrix.hashCode();
		return result;
	}
}
