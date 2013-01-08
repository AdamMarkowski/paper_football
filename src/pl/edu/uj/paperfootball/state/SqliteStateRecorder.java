package pl.edu.uj.paperfootball.state;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Reference implementation of the {@link StateRecorder} using Sqlite db.
 * 
 * It creates a db file using {@link SaveStateOpenHelper#DATABASE_NAME} as the file name.
 * 
 * @author Artur Stepniewski <a.stepniewsk@samsung.com>
 * 
 * @see StateRecorder
 * @see GameStateContract
 */
public class SqliteStateRecorder extends StateRecorder {

	private final SaveStateOpenHelper mSaveStateOpenHelper;

	/**
	 * Constructor that initializes the {@link #mSaveStateOpenHelper}.
	 * 
	 * @param ctx
	 *            the {@link Context}
	 */
	public SqliteStateRecorder(Context ctx) {
		mSaveStateOpenHelper = new SaveStateOpenHelper(ctx);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save() {
		save(CURRENT_GAMESTATE_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save(String gameStateName) {
		SQLiteDatabase db = mSaveStateOpenHelper.getWritableDatabase();

		// Insert or replace a game state row in the state table

		ContentValues values = new ContentValues();
		values.put(GameStateContract.StateColumns.NAME, gameStateName);
		Cursor c = db.query(GameStateContract.StateColumns.TABLE_NAME, new String[] { BaseColumns._ID },
				GameStateContract.StateColumns.NAME + " = ?", new String[] { gameStateName }, null, null, null);
		long stateId = 0;

		db.beginTransaction();
		try {
			if (c.moveToFirst()) {
				// Update the game state for gameStateName and remove previous moves

				final int columnId = c.getColumnIndex(BaseColumns._ID);
				stateId = c.getLong(columnId);

				db.delete(GameStateContract.MoveColumns.TABLE_NAME, GameStateContract.MoveColumns.STATE_ID + " = "
						+ stateId, null);

			} else {
				// There was no such state previously saved.

				stateId = db.insert(GameStateContract.StateColumns.TABLE_NAME, null, values);
			}

			db.setTransactionSuccessful();

		} finally {
			db.endTransaction();
		}

		c.close();

		// Insert moves for this particular state id

		DatabaseUtils.InsertHelper inserter = new DatabaseUtils.InsertHelper(db,
				GameStateContract.MoveColumns.TABLE_NAME);

		final int columnX = inserter.getColumnIndex(GameStateContract.MoveColumns.X);
		final int columnY = inserter.getColumnIndex(GameStateContract.MoveColumns.Y);
		final int columnStateId = inserter.getColumnIndex(GameStateContract.MoveColumns.STATE_ID);

		db.beginTransaction();
		try {
			final int size = mMoves.size();

			for (int i = 0; i < size; ++i) {

				inserter.prepareForInsert();
				inserter.bind(columnStateId, stateId);

				inserter.bind(columnX, mMoves.get(i));
				++i;
				inserter.bind(columnY, mMoves.get(i));

				inserter.execute();
			}

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			inserter.close();
		}

		db.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void load() {
		load(CURRENT_GAMESTATE_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void load(String gameStateName) {
		SQLiteDatabase db = mSaveStateOpenHelper.getReadableDatabase();

		// Query the database using two tables, e.g.:
		// SELECT x, y from move INNER JOIN state ON state_id = state._id WHERE name = 'current_gamestate';
		Cursor c = db.rawQuery(GameStateContract.MoveColumns.QUERY_INNER_MOVE, new String[] { gameStateName });

		c.moveToFirst();
		final int columnIdX = c.getColumnIndex(GameStateContract.MoveColumns.X);
		final int columnIdY = c.getColumnIndex(GameStateContract.MoveColumns.Y);

		if (!c.isAfterLast()) {
			clear();
		}

		while (!c.isAfterLast()) {
			int x = c.getInt(columnIdX);
			int y = c.getInt(columnIdY);
			addMove(x, y);

			c.moveToNext();
		}

		c.close();

		db.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int delete(String gameStateName) {
		SQLiteDatabase db = mSaveStateOpenHelper.getWritableDatabase();
		int deletedRows = db.delete(GameStateContract.StateColumns.TABLE_NAME, GameStateContract.StateColumns.NAME
				+ " = ?", new String[] { gameStateName });

		db.close();

		return deletedRows;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int deleteAll() {
		SQLiteDatabase db = mSaveStateOpenHelper.getWritableDatabase();
		int deletedRows = db.delete(GameStateContract.StateColumns.TABLE_NAME, null, null);

		db.close();

		return deletedRows;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getGameStateList() {
		SQLiteDatabase db = mSaveStateOpenHelper.getReadableDatabase();

		Cursor c = db.query(GameStateContract.StateColumns.TABLE_NAME, null, null, null, null, null, null);

		List<String> list = new ArrayList<String>(c.getCount());

		c.moveToFirst();
		final int columnName = c.getColumnIndex(GameStateContract.StateColumns.NAME);
		while (!c.isAfterLast()) {
			String name = c.getString(columnName);
			list.add(name);

			c.moveToNext();
		}

		c.close();

		db.close();

		return list;
	}

	/**
	 * Contract class describing the GameState database tables.
	 * 
	 * @author Artur Stepniewski <a.stepniewsk@samsung.com>
	 * 
	 */
	public static class GameStateContract {

		public static final class StateColumns implements BaseColumns {
			/** Name column (STRING) */
			public static final String NAME = "name";

			public static final String TABLE_NAME = "state";
			public static final String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + _ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME + " STRING)";

			public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
		}

		public static final class MoveColumns implements BaseColumns {
			/** Game state id column (INTEGER) */
			public static final String STATE_ID = "state_id";
			/** X move coordinate column (INTEGER) */
			public static final String X = "x";
			/** Y move coordinate column (INTEGER) */
			public static final String Y = "y";

			public static final String TABLE_NAME = "move";
			public static final String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + _ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + X + " INTEGER, " + Y + " INTEGER, " + STATE_ID
					+ " INTEGER REFERENCES " + StateColumns.TABLE_NAME + "(" + StateColumns._ID
					+ ") ON DELETE CASCADE)";
			public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

			public static final String QUERY_INNER_MOVE = "SELECT " + MoveColumns.X + ", " + MoveColumns.Y + " FROM "
					+ MoveColumns.TABLE_NAME + " INNER JOIN " + StateColumns.TABLE_NAME + " ON " + MoveColumns.STATE_ID
					+ " = " + StateColumns.TABLE_NAME + "." + StateColumns._ID + " WHERE " + StateColumns.NAME + " = ?";
		}
	}

	public static class SaveStateOpenHelper extends SQLiteOpenHelper {

		private static final String DATABASE_NAME = "gamestate.db";
		private static final int DATABASE_VERSION = 1;
		private static final String QUERY_ENABLE_FK = "PRAGMA foreign_keys=ON;";

		public SaveStateOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(GameStateContract.StateColumns.TABLE_CREATE);
			db.execSQL(GameStateContract.MoveColumns.TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			db.execSQL(GameStateContract.StateColumns.TABLE_DROP);
			db.execSQL(GameStateContract.MoveColumns.TABLE_DROP);

			onCreate(db);
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);

			if (!db.isReadOnly()) {
				db.execSQL(QUERY_ENABLE_FK);
			}
		}

	}
}
