package pl.edu.uj.paperfootball;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.List;

import pl.edu.uj.paperfootball.bluetooth.GameThread.MoveState;
import pl.edu.uj.paperfootball.packets.Packet;
import pl.edu.uj.paperfootball.packets.PacketContinueGameViaBluetooth;
import pl.edu.uj.paperfootball.packets.PacketMoveViaBluetooth;
import pl.edu.uj.paperfootball.packets.PacketType;
import pl.edu.uj.paperfootball.state.SqliteStateRecorder;
import pl.edu.uj.paperfootball.state.StateRecorder;
import pl.edu.uj.paperfootball.utils.MoveLineForCanvas;
import pl.edu.uj.paperfootball.utils.Node;
import pl.edu.uj.paperfootball.utils.Point;
import pl.edu.uj.paperfootball.utils.RefreshHandler;
import pl.edu.uj.paperfootball.utils.Node.NodeType;

import android.graphics.Color;
import android.os.Message;
import android.util.FloatMath;

import pl.edu.uj.paperfootball.R;
/**
 * Manages all events related with game such as connecting nodes, refreshing UI etc.
 */
public class GameManager implements PropertyChangeListener {

	private static final float BALL_TOUCH_AREA_RADIUS = 40.0f;
	private static final float MAX_DISTANCE_BETWEEN_NODE_AND_TOUCH_UP = 50.0f;
	private static final Node INCORRECT_NODE = new Node(-1, -1, -1, -1, false, NodeType.NODE_NO_TYPE, -1);

	// Views
	private final GameViewActivity mGameViewActivity;
	private final GameCanvasBottom mGameCanvasBottom;
	private final GameSCanvasView mGameSCanvasViewTop;

	// Other
	private final PropertyChangeSupport mPropertyChangeSupport;
	private final CurrentPlaygroundState mCPS;
	private final StateRecorder mStateRecorder;
	private final RefreshHandler mRefreshHandler;
	private boolean mGameFinished;

	/**
	 * Constructs manager of the game.
	 * 
	 * @param gameSCanvas
	 *            SCanvasView on which to draw.
	 * @param gameCanvasBottom
	 *            Bottom view with field and nodes.
	 * @param cps
	 *            Model of the game - current playground state.
	 * @param refreshHandler
	 *            Handler to communicate with the UI thread.
	 * @param gameViewActivity
	 *            Activity of the game.
	 */
	public GameManager(GameSCanvasView gameSCanvas, GameCanvasBottom gameCanvasBottom, CurrentPlaygroundState cps,
			RefreshHandler refreshHandler, GameViewActivity gameViewActivity) {
		mGameViewActivity = gameViewActivity;
		mRefreshHandler = refreshHandler;
		mGameCanvasBottom = gameCanvasBottom;
		mGameSCanvasViewTop = gameSCanvas;
		mCPS = cps;
		mStateRecorder = new SqliteStateRecorder(gameSCanvas.getContext());
		mPropertyChangeSupport = new PropertyChangeSupport(this);
		mGameFinished = false;

		mGameSCanvasViewTop.setGameManager(this);
		mGameCanvasBottom.setGameManager(this);
	}

	/**
	 * Clears nodes and ball position.
	 */
	public void onDestroy() {
		mCPS.clearNodesConnection();
		mCPS.setBallNodePosition(new Point(4, 6));
		mCPS.clearNodesBouncesStateAndVisitNumber();
	}

	/**
	 * Check whether touch-down was on the ball.
	 * 
	 * @param touchPointDown
	 *            Point indicating the touch down event.
	 * @return Returns true if the touch-down event was on the ball.
	 */
	private boolean isTouchDownOnBall(Point touchPointDown) {
		float distance = getDistanceBetweenPoints(mCPS.getBallCanvasPosition(), touchPointDown);
		return distance > BALL_TOUCH_AREA_RADIUS ? false : true;
	}

	/**
	 * Returns distance between two game points.
	 * 
	 * @param firstPoint
	 *            The first point.
	 * @param secondPoint
	 *            The second point.
	 * @return Distance between given points.
	 */
	private float getDistanceBetweenPoints(Point firstPoint, Point secondPoint) {
		float dx = firstPoint.getX() - secondPoint.getX();
		float dy = firstPoint.getY() - secondPoint.getY();

		return FloatMath.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Finds closest neighbor node.
	 * 
	 * @param point
	 *            The Point relative to the nearest neighbor is searched.
	 * @return Returns closest node or {@value GameManager#INCORRECT_NODE} if node is incorrect.
	 */
	private Node findClosestNeighborNode(Point point) {
		float distance = 900000.0f;
		float distanceTmp;
		int closestNodeX = 0;
		int closestNodeY = 0;

		Point ballNodePosition = mCPS.getBallNodePosition();
		for (int i = ballNodePosition.getX() - 1; i < ballNodePosition.getX() + 2; i++) {
			if (i < 0 || i >= CurrentPlaygroundState.NODES_WIDTH) {
				continue;
			}
			for (int j = ballNodePosition.getY() - 1; j < ballNodePosition.getY() + 2; j++) {
				if (j == 0 && i != 4 || j < 0 || j >= CurrentPlaygroundState.NODES_HEIGHT
						|| i == ballNodePosition.getX() && j == ballNodePosition.getY()) {
					continue;
				}
				final Node node = mCPS.getNode(i, j);
				distanceTmp = getDistanceBetweenPoints(point, node.getCanvasCoordinates());
				if (distanceTmp < distance) {
					distance = distanceTmp;
					closestNodeX = i;
					closestNodeY = j;
				}
			}
		}

		if (distance < MAX_DISTANCE_BETWEEN_NODE_AND_TOUCH_UP) {
			return mCPS.getNode(closestNodeX, closestNodeY);
		} else {
			return INCORRECT_NODE;
		}
	}

	/**
	 * Checks whether node change is correct.
	 * 
	 * @param currentBallNode
	 *            The node at which the ball it at the moment.
	 * @param newBallNode
	 *            The node to which user wants to move ball.
	 * @return Returns true is node change is correct.
	 */
	private boolean isNodeChangeCorrect(Node currentBallNode, Node newBallNode) {
		boolean moveAllowed = true;

		if (currentBallNode.getNodeType() == NodeType.NODE_HORIZONTAL_WALL
				&& newBallNode.getNodeType() == NodeType.NODE_HORIZONTAL_WALL) {
			moveAllowed = false;
		} else if (currentBallNode.getNodeType() == NodeType.NODE_VERTICAL_WALL
				&& newBallNode.getNodeType() == NodeType.NODE_VERTICAL_WALL) {
			moveAllowed = false;
		} else if (currentBallNode.getNodeType() == NodeType.NODE_CORNER
				&& newBallNode.getNodeType() != NodeType.NODE_PITCH) {
			moveAllowed = false;
		} else if (currentBallNode.getNodeType() != NodeType.NODE_PITCH
				&& newBallNode.getNodeType() == NodeType.NODE_CORNER) {
			moveAllowed = false;
		} else if (newBallNode.getNodeType() == NodeType.NODE_NO_TYPE) {
			moveAllowed = false;
		}

		return moveAllowed;
	}

	/**
	 * Restarts game.
	 */
	public void playGameAgain() {		
		mGameSCanvasViewTop.clearAll(false);
		onDestroy();
		mCPS.clearChangePlayerCounter();
		mCPS.clearNodesConnection();
		mCPS.clearNodesBouncesStateAndVisitNumber();

		Node fiedlCenterNode = mCPS.getNode(4, 6);
		
		Message msg = mRefreshHandler.obtainMessage();
		msg.what = RefreshHandler.GAME_POINT;
		msg.obj = fiedlCenterNode.getCanvasCoordinates();
		mRefreshHandler.sendMessage(msg);
		
		mGameFinished = false;
	}

	/**
	 * Draws nodes on the bottom canvas view.
	 */
	public void drawNodesOnCanvasBottom() {
		final int nodeRadius = 10;
		for (int j = 1; j < CurrentPlaygroundState.NODES_HEIGHT - 1; ++j) {
			for (int i = 0; i < CurrentPlaygroundState.NODES_WIDTH; i++) {
				mGameCanvasBottom.drawNode(mCPS.getNode(i, j).getCanvasCoordinateX(), mCPS.getNode(i, j)
						.getCanvasCoordinateY(), nodeRadius, Color.WHITE);
			}
		}

		final int nodeGoalRadius = 40;
		mGameCanvasBottom.drawNode(mCPS.getNode(4, 0).getCanvasCoordinateX(),
				mCPS.getNode(4, 0).getCanvasCoordinateY(), nodeGoalRadius, Color.BLUE);
		mGameCanvasBottom.drawNode(mCPS.getNode(4, CurrentPlaygroundState.NODES_HEIGHT - 1).getCanvasCoordinateX(),
				mCPS.getNode(4, CurrentPlaygroundState.NODES_HEIGHT - 1).getCanvasCoordinateY(), nodeGoalRadius,
				Color.RED);
	}

	/**
	 * Draws field lines on the bottom canvas view.
	 */
	public void drawLinesOfFieldOnCanvasBottom() {
		final List<Integer> posOfNodesOfFieldLine = Arrays.asList(0, 1, 3, 1, 3, 0, 5, 0, 5, 1, 8, 1, 8, 11, 5, 11, 5,
				12, 3, 12, 3, 11, 0, 11, 0, 1);
		final int length = posOfNodesOfFieldLine.size();

		for (int i = 0; i < length - 2; i += 2) {
			int xStartCanv = mCPS.getNode(posOfNodesOfFieldLine.get(i), posOfNodesOfFieldLine.get(i + 1))
					.getCanvasCoordinateX();
			int yStartCanv = mCPS.getNode(posOfNodesOfFieldLine.get(i), posOfNodesOfFieldLine.get(i + 1))
					.getCanvasCoordinateY();
			int xEndCanv = mCPS.getNode(posOfNodesOfFieldLine.get(i + 2), posOfNodesOfFieldLine.get(i + 3))
					.getCanvasCoordinateX();
			int yEndCanv = mCPS.getNode(posOfNodesOfFieldLine.get(i + 2), posOfNodesOfFieldLine.get(i + 3))
					.getCanvasCoordinateY();
			mGameCanvasBottom.drawLine(xStartCanv, yStartCanv, xEndCanv, yEndCanv, Color.WHITE, 10);
		}
	}

	/**
	 * Called when received move via Bluetooth.
	 * 
	 * @param event
	 *            Event with new property value.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {

		Object newValue = event.getNewValue();
		Packet newBluetoothValue = (Packet) newValue;

		if (newBluetoothValue.getPacketType() == PacketType.NEW_MOVE) {
			PacketMoveViaBluetooth newBluetoothMove = (PacketMoveViaBluetooth) newValue;
			Node newNodeBallPosition = mCPS.getNode(newBluetoothMove.getEndPoint());
			setNodeBouncesAndSetChangePlayer(newNodeBallPosition);
			moveBall(newNodeBallPosition);
		} else if (newBluetoothValue.getPacketType() == PacketType.REQUEST_GAME_LOAD) {
			// this is only for CLIENT
			PacketContinueGameViaBluetooth moveslistPacket = (PacketContinueGameViaBluetooth) newValue;

			List<Integer> moves = moveslistPacket.getPoints();
			int size = moves.size();

			for (int i = 0; i < size; i += 2) {
				int x = moves.get(i);
				int y = moves.get(i + 1);

				Node newBallNode = mCPS.getNode(x, y);
				setNodeBouncesAndSetChangePlayer(newBallNode);
				moveBall(newBallNode);
			}

			if (mCPS.getChangePlayerCounter() % 2 != 0) {
				Message msg = mRefreshHandler.obtainMessage();
				msg.what = RefreshHandler.SET_MOVE_STATE;
				msg.obj = MoveState.WAIT_FOR_MY_MOVE;
				mRefreshHandler.sendMessage(msg);
			}
		}
	}

	/**
	 * Sets node state to bounces and also changes player.
	 * 
	 * @param newBallNode
	 *            New node of the ball.
	 */
	private void setNodeBouncesAndSetChangePlayer(Node newBallNode) {
		final Point ballNodePosition = mCPS.getBallNodePosition();
		Node currentBallNode = mCPS.getNode(ballNodePosition.getX(), ballNodePosition.getY());

		currentBallNode.setBounces(true);
		if (!newBallNode.isBounces()) {
			mCPS.setNextPlayerMove(true);
			mCPS.increaseChangePlayerCounter();
		} else {
			mCPS.setNextPlayerMove(false);
		}
	}

	/**
	 * Moves ball on the canvas view and also sends move to the opponent via Bluetooth connection.
	 * 
	 * @param newBallNode
	 *            New node of the ball.
	 */
	private void moveBallAndSendMoveViaBluetooth(Node newBallNode) {
		boolean correctMove = moveBall(newBallNode);

		if (correctMove || isGameFinished()) {
			mPropertyChangeSupport.firePropertyChange("unused", null,
					new PacketMoveViaBluetooth(mCPS.getBallNodePosition(), new Point(newBallNode.getNodePositionX(),
							newBallNode.getNodePositionY()), mCPS.isNextPlayerMove()));
		}
	}

	/**
	 * Moves ball on the canvas view.
	 * 
	 * @param newBallNode
	 *            New node of the ball.
	 * @return Returns true if the move is performed, or false if game is finished or move is not allowed.
	 */
	private boolean moveBall(Node newBallNode) {

		if (mGameFinished) {
			showGameOverToast();
			return false;
		}

		Node ballNode = mCPS.getBallNode();
		if (mCPS.getNodesConnection(newBallNode, ballNode)) {
			showToast(mGameViewActivity.getString(R.string.move_not_allowed));
			return false;
		}

		// refresh ball position on SCanvas
		Message msg = mRefreshHandler.obtainMessage();
		msg.what = RefreshHandler.GAME_POINT;
		msg.obj = newBallNode.getCanvasCoordinates();
		mRefreshHandler.sendMessage(msg);

		// refresh SCanvas
		Message msg1 = mRefreshHandler.obtainMessage();
		msg1.what = RefreshHandler.MOVE_LINE;
		msg1.obj = new MoveLineForCanvas(mCPS.getBallCanvasPosition(), newBallNode.getCanvasCoordinates(),
				mCPS.getMoveColor(), 10);
		mRefreshHandler.sendMessage(msg1);

		// refresh GameLogic
		mCPS.setNodesConnection(ballNode, newBallNode);
		mCPS.setBallNodePosition(new Point(newBallNode.getNodePositionX(), newBallNode.getNodePositionY()));
		mCPS.setLineColor();

		int mNodeVisitNumber = newBallNode.getNodeVisitNumber();
		int nodeVisitLimit = 4;
		mNodeVisitNumber++;
		newBallNode.setNodeVisitNumber(mNodeVisitNumber);

		if (newBallNode.getNodeType() == NodeType.NODE_GOAL || newBallNode.getNodeType() == NodeType.NODE_CORNER
				|| newBallNode.getNodeVisitNumber() > nodeVisitLimit || mGameFinished) {
			mGameFinished = true;
			showGameOverToast();
			return false;
		}

		// remember move in case of game saving
		mStateRecorder.addMove(newBallNode.getNodePositionX(), newBallNode.getNodePositionY());

		return true;
	}

	/**
	 * Performs move from start point (down) to end point (up).
	 * 
	 * @param touchDownPoint
	 *            The point where user starts move.
	 * @param touchUpPoint
	 *            The point where user ends move.
	 */
	public void performMoveFromSCanvas(Point touchDownPoint, Point touchUpPoint) {
		// in client-server game mode don't allow user to perform move
		// when he is waiting for opponent move (e.g. in OPPONENT_MOVE state)
		if (mGameViewActivity.getMoveState() == MoveState.WAIT_FOR_MY_MOVE) {
			if (!isTouchDownOnBall(touchDownPoint)) {
				return;
			}

			Node newBallNode = findClosestNeighborNode(touchUpPoint);
			if (isNodeChangeCorrect(mCPS.getBallNode(), newBallNode)) {
				// newBallPosition is correct
				setNodeBouncesAndSetChangePlayer(newBallNode);
				moveBallAndSendMoveViaBluetooth(newBallNode);
			} else {
				// newBallPosition is NOT correct
				mCPS.setNextPlayerMove(true);
			}
		}
	}

	/**
	 * Loads game state from database.
	 */
	public void loadFromStateRecorder() {
		// get moves from current_game
		mStateRecorder.load();
		// copy the moves
		List<Integer> moves = mStateRecorder.getMoves();

		// clears moves in state recorder
		mStateRecorder.clear();

		final int size = moves.size();

		if (size % 2 != 0) {
			throw new IllegalStateException("Uneven number of move coordinates (" + size + "). Cannot load state!");
		}

		for (int i = 0; i < size; i += 2) {
			int x = moves.get(i);
			int y = moves.get(i + 1);

			Node newBallNode = mCPS.getNode(x, y);
			setNodeBouncesAndSetChangePlayer(newBallNode);
			moveBallAndSendMoveViaBluetooth(newBallNode);
		}
	}

	/**
	 * Shows toast message on the screen.
	 * 
	 * @param message
	 *            Message to show.
	 */
	private void showToast(String message) {
		Message msg = mRefreshHandler.obtainMessage();
		msg.what = RefreshHandler.TOAST_MSG;
		msg.obj = message;
		mRefreshHandler.sendMessage(msg);
	}

	/**
	 * Removes all property listeners.
	 */
	public void removePropertyChangeListeners() {
		PropertyChangeListener[] listeners = mPropertyChangeSupport.getPropertyChangeListeners();

		for (PropertyChangeListener listener : listeners) {
			mPropertyChangeSupport.removePropertyChangeListener(listener);
		}
	}

	/**
	 * Adds property change listener. It is like observer pattern, where all listeners are notified about any change of
	 * the game model.
	 * 
	 * @param listener
	 *            The object which will be informed of any property change.
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		mPropertyChangeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * Shows toast with game over message.
	 */
	private void showGameOverToast() {
		showToast(mGameViewActivity.getString(R.string.game_finished));
	}

	/**
	 * Sends packet to the background thread.
	 * 
	 * @param packet
	 *            Packet to send.
	 */
	public void sendPacket(Packet packet) {
		mPropertyChangeSupport.firePropertyChange("none", null, packet);
	}

	public boolean isGameFinished() {
		return mGameFinished;
	}

	public StateRecorder getStateRecorder() {
		return mStateRecorder;
	}
}
