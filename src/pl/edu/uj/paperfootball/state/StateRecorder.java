package pl.edu.uj.paperfootball.state;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the state of moves.
 * 
 * The main purpose of this class is to act as a base for a persistent storage of moves. The move list ({@link #mMoves})
 * is a simple {@link List} of {@link Integer Integers} that contains x and y coordinate values.
 * 
 * @author Artur Stepniewski <a.stepniewsk@samsung.com>
 * 
 */
public abstract class StateRecorder {

	/**
	 * The name of the default game state for {@link #save()} and {@link #load()} methods.
	 */
	public static final String CURRENT_GAMESTATE_NAME = "current_gamestate";

	/* package */List<Integer> mMoves = new ArrayList<Integer>();

	/**
	 * Preserves the movement history in the backend with the {@value #CURRENT_GAMESTATE_NAME} name.
	 * 
	 * @see #save(String)
	 */
	public abstract void save();

	/**
	 * Preserves the movement history in the db.
	 * 
	 * @param gameStateName
	 *            name of the game state
	 */
	public abstract void save(String gameStateName);

	/**
	 * Loads the movement history from the backend with the {@value #CURRENT_GAMESTATE_NAME} name.
	 * 
	 * @see #save(String)
	 */
	public abstract void load();

	/**
	 * Loads the movement history and fills the {@link #mMoves} list.
	 * 
	 * @param gameStateName
	 *            name of the game state
	 */
	public abstract void load(String gameStateName);

	/**
	 * Deletes a game state from the persistent backend storage.
	 * 
	 * @param gameStateName
	 * @return number of deleted rows
	 */
	public abstract int delete(String gameStateName);

	/**
	 * Deletes all game states from the persistent backend storage.
	 * 
	 * @return number of deleted rows
	 */
	public abstract int deleteAll();

	/**
	 * Retrieves the game state name list from the backend.
	 * 
	 * @return new list of game state names
	 */
	public abstract List<String> getGameStateList();

	/**
	 * Adds the move to the current move list.
	 * 
	 * @param x
	 *            x coordinate of the node
	 * @param y
	 *            y coordinate of the node
	 */
	public void addMove(int x, int y) {
		mMoves.add(x);
		mMoves.add(y);
	}

	/**
	 * Returns the number of moves in the current list.
	 * 
	 * @return the number of moves
	 */
	public int size() {
		return mMoves.size();
	}

	/**
	 * Clears the current list of moves.
	 */
	public void clear() {
		mMoves.clear();
	}

	/**
	 * Simple getter method for the {@link Integer} {@link List}.
	 * 
	 * @return copy of move list
	 */
	public List<Integer> getMoves() {
		return new ArrayList<Integer>(mMoves);
	}
}
