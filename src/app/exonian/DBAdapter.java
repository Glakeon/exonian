package app.exonian;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
	static final String KEY_ROWID = "_id";
	static final String KEY_TITLE = "title";
	static final String KEY_AUTHOR = "author";
	static final String KEY_CONTENT = "content";
	static final String TAG = "DBAdapter";

	static final String DATABASE_NAME = "ExonianDB";
	static final String DATABASE_TABLE = "articles";
	static final int DATABASE_VERSION = 2;

	static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS articles (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "title TEXT NOT NULL UNIQUE, author TEXT NOT NULL, content TEXT NOT NULL);";

	final Context context;

	DatabaseHelper DBHelper;
	SQLiteDatabase db;

	public DBAdapter(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL(DATABASE_CREATE);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS contacts");
			onCreate(db);
		}
	}

	// ---opens the database---
	public DBAdapter open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}

	// ---closes the database---
	public void close() {
		DBHelper.close();
	}

	// ---insert a contact into the database---
	public long insertArticle(String title, String author, String content) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TITLE, title);
		initialValues.put(KEY_AUTHOR, author);
		initialValues.put(KEY_CONTENT, content);
		return db.insertWithOnConflict(DATABASE_TABLE, null, initialValues, SQLiteDatabase.CONFLICT_IGNORE);
	}

	private Article cursorToArticle(Cursor cursor) {
		Article article = new Article();
		article.setId(cursor.getLong(0));
		article.setTitle(cursor.getString(1));
		article.setAuthor(cursor.getString(2));
		article.setContent(cursor.getString(3));
		return article;
	}

	// ---retrieves a particular contact---
	public Article getArticle(long rowId) throws SQLException {
		Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_TITLE, KEY_AUTHOR, KEY_CONTENT }, KEY_ROWID + "=" + rowId
				, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return cursorToArticle(mCursor);
	}

}
