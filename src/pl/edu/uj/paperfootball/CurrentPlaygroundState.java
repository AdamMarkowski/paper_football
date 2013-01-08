package pl.edu.uj.paperfootball;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.uj.paperfootball.utils.Node;
import pl.edu.uj.paperfootball.utils.Point;
import pl.edu.uj.paperfootball.utils.Node.NodeType;

import android.graphics.Color;
import android.util.Pair;


/**
 * Holds state of the playground.
 */
public class CurrentPlaygroundState {

	private static final int X_FIELD_ON_CANVAS_START = 40;
	private static final int Y_FIELD_ON_CANVAS_START = 60;
	private static final Node[][] NODES;
	public static final int NODES_WIDTH = 9;
	public static final int NODES_HEIGHT = 13;

	private final Map<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>, Boolean> mMapNodesConnection;
	private final int mGrid;
	private Point mBallNodePosition;
	private int mMoveColor;
	private int mChangePlayerCounter;
	private boolean mNextPlayerMove;

	/**
	 * Initializes static field.
	 */
	static {
		NODES = new Node[NODES_WIDTH][NODES_HEIGHT];
	}

	/**
	 * Constructs playground.
	 * 
	 * @param widthSCanvasTop
	 *            SCanvasView width.
	 * @param heightSCanvasTop
	 *            SCanvasView height.
	 */
	public CurrentPlaygroundState(int widthSCanvasTop, int heightSCanvasTop) {
		mMapNodesConnection = new HashMap<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>, Boolean>(NODES_WIDTH
				* NODES_HEIGHT);
		mBallNodePosition = new Point(4, 6);
		mNextPlayerMove = false;
		mMoveColor = Color.RED;
		mGrid = Math.min(widthSCanvasTop / CurrentPlaygroundState.NODES_WIDTH, heightSCanvasTop
				/ CurrentPlaygroundState.NODES_HEIGHT);

		initializeNodes();
	}

	/**
	 * Initializes nodes.
	 */
	private void initializeNodes() {
		int nodeVisited[] = { 0, 1, 2, 3, 4 };

		for (int j = 0; j <= NODES_HEIGHT / 2; ++j) {
			int xTmp = X_FIELD_ON_CANVAS_START;
			for (int i = 0; i < NODES_WIDTH; ++i) {

				boolean bounces;
				NodeType nodeType;
				int visitNumber;
				if (j == 0) { // out of the pitch
					if (i == NODES_WIDTH / 2) {
						// middle of horizontal, out-of-pitch line is a GOAL node
						bounces = false;
						nodeType = NodeType.NODE_GOAL;
					} else {
						bounces = false;
						nodeType = NodeType.NODE_NO_TYPE;
					}
					visitNumber = nodeVisited[4];
				} else if (j - 1 == 0) {
					// line corresponding to the horizontal wall
					if (i % (NODES_WIDTH - 1) == 0) { // CORNER
						bounces = false;
						nodeType = NodeType.NODE_CORNER;
						visitNumber = nodeVisited[4];
					} else if (i == NODES_WIDTH / 2) {
						// in the middle is a PITCH node
						bounces = false;
						nodeType = NodeType.NODE_PITCH;
						visitNumber = nodeVisited[0];
					} else if (i == NODES_WIDTH / 2 - 1 || i == NODES_WIDTH / 2 + 1) {
						// horizontal wall which touches witch goal
						bounces = true;
						nodeType = NodeType.NODE_HORIZONTAL_WALL;
						visitNumber = nodeVisited[2];
					} else {
						// horizontal wall
						bounces = true;
						nodeType = NodeType.NODE_HORIZONTAL_WALL;
						visitNumber = nodeVisited[3];
					}
				} else {
					if (i % (NODES_WIDTH - 1) == 0) {
						// vertical wall
						bounces = true;
						nodeType = NodeType.NODE_VERTICAL_WALL;
						visitNumber = nodeVisited[3];
					} else {
						// PITCH node
						bounces = false;
						nodeType = NodeType.NODE_PITCH;
						visitNumber = nodeVisited[0];
					}
				}
				NODES[i][j] = new Node(xTmp, Y_FIELD_ON_CANVAS_START + mGrid * j, i, j, bounces, nodeType, visitNumber);
				// creating Nodes from top of pitch
				if (j != NODES_HEIGHT / 2) {
					// from bottom
					NODES[i][NODES_HEIGHT - 1 - j] = new Node(xTmp, Y_FIELD_ON_CANVAS_START + mGrid
							* (NODES_HEIGHT - 1 - j), i, NODES_HEIGHT - 1 - j, bounces, nodeType, visitNumber);
				}
				xTmp += mGrid;
			}
		}
	}

	/**
	 * Checks if two nodes are connected to each other.
	 * 
	 * @param firstNode
	 *            The first node.
	 * @param secondNode
	 *            The second node.
	 * @return Returns true if two given nodes are connected to each other.
	 */
	public boolean getNodesConnection(Node firstNode, Node secondNode) {
		int xDif = Math.abs(firstNode.getNodePositionX() - secondNode.getNodePositionX());
		int yDif = Math.abs(firstNode.getNodePositionY() - secondNode.getNodePositionY());

		if (xDif > 1 || yDif > 1 || xDif < 0.1 && yDif < 0.1) {
			return true;
		}

		Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> key = Pair.create(
				Pair.create(firstNode.getNodePositionX(), firstNode.getNodePositionY()),
				Pair.create(secondNode.getNodePositionX(), secondNode.getNodePositionY()));

		return mMapNodesConnection.get(key) != null;
	}

	/**
	 * Sets connection between two different nodes.
	 * 
	 * @param firstNode
	 *            The first node.
	 * @param secondNode
	 *            The second node.
	 */
	public void setNodesConnection(Node firstNode, Node secondNode) {
		Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> nodesPair = Pair.create(
				Pair.create(firstNode.getNodePositionX(), firstNode.getNodePositionY()),
				Pair.create(secondNode.getNodePositionX(), secondNode.getNodePositionY()));

		Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> nodesPair2 = Pair.create(
				Pair.create(secondNode.getNodePositionX(), secondNode.getNodePositionY()),
				Pair.create(firstNode.getNodePositionX(), firstNode.getNodePositionY()));

		mMapNodesConnection.put(nodesPair, true);
		mMapNodesConnection.put(nodesPair2, true);
	}

	/**
	 * Sets color of the game line.
	 */
	public void setLineColor() {
		if (mNextPlayerMove) {
			mMoveColor = mMoveColor == Color.RED ? Color.BLUE : Color.RED;
		}
	}

	/**
	 * Clears bounces state and visit number of all nodes.
	 */
	public void clearNodesBouncesStateAndVisitNumber() {
		int nodeVisited[] = { 0, 1, 2, 3, 4 };

		for (int j = 2; j < NODES_HEIGHT - 2; ++j) {
			for (int i = 1; i < NODES_WIDTH - 1; ++i) {
				getNode(i, j).setBounces(false);
				getNode(i, j).setNodeVisitNumber(nodeVisited[1]);
			}
		}

		int k = 1, l = 11;
		for (int i = 0; i < CurrentPlaygroundState.NODES_WIDTH; i++) {
			if (i != NODES_WIDTH / 2) {
				getNode(i, k).setNodeVisitNumber(nodeVisited[3]);
				getNode(i, l).setNodeVisitNumber(nodeVisited[3]);
			}
		}
		
		int h = 0, i = 8;
		for (int j = 2; j < CurrentPlaygroundState.NODES_HEIGHT - 2; ++j) {
			if (i != NODES_WIDTH / 2) {
				getNode(h, j).setNodeVisitNumber(nodeVisited[3]);
				getNode(i, j).setNodeVisitNumber(nodeVisited[3]);
			}			
		}

		// clear bounces state of node that is in front of the goal
		getNode(NODES_WIDTH / 2, 1).setBounces(false);
		getNode(NODES_WIDTH / 2, NODES_HEIGHT - 2).setBounces(false);
	}

	public int getGrid() {
		return mGrid;
	}

	public boolean isNextPlayerMove() {
		return mNextPlayerMove;
	}

	public void setNextPlayerMove(boolean nextPlayerMove) {
		mNextPlayerMove = nextPlayerMove;
	}

	public int getMoveColor() {
		return mMoveColor;
	}

	public void clearNodesConnection() {
		mMapNodesConnection.clear();
	}

	public int getChangePlayerCounter() {
		return mChangePlayerCounter;
	}

	public void clearChangePlayerCounter() {
		mChangePlayerCounter = 0;
	}

	public void increaseChangePlayerCounter() {
		mChangePlayerCounter++;
	}

	public Point getBallNodePosition() {
		return mBallNodePosition;
	}

	public Node getBallNode() {
		return NODES[mBallNodePosition.getX()][mBallNodePosition.getY()];
	}

	public Point getBallCanvasPosition() {
		return NODES[mBallNodePosition.getX()][mBallNodePosition.getY()].getCanvasCoordinates();
	}

	public void setBallNodePosition(Point mBallNodePosition) {
		this.mBallNodePosition = mBallNodePosition;
	}

	public Node getNode(int x, int y) {
		return NODES[x][y];
	}

	public Node getNode(Point point) {
		return NODES[point.getX()][point.getY()];
	}
}