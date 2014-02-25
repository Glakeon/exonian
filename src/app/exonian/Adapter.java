package app.exonian;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Adapter {
	static final String KEY_ROWID = "_id";
	static final String KEY_TITLE = "title";
	static final String KEY_AUTHOR = "author";
	static final String KEY_CONTENT = "content";
	static final String KEY_URL = "url";
	static final String KEY_DATE = "date";
	static final String TAG = "Adapter";

	static final String DATABASE_NAME = "Exonian";
	static final String DATABASE_TABLE = "articles";
	static final int DATABASE_VERSION = 4;

	static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS articles(_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, author TEXT NOT NULL, content TEXT NOT NULL, date TEXT NOT NULL, url TEXT NOT NULL);";

	final Context context;
	DatabaseHelper helper;
	SQLiteDatabase db;

	public Adapter(Context context) {
		this.context = context;
		helper = new DatabaseHelper(context);
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
			db.execSQL("DROP TABLE IF EXISTS articles");
			onCreate(db);
		}
	}

	public Adapter open() throws SQLException {
		db = helper.getWritableDatabase();
		return this;
	}

	public void close() {
		helper.close();
	}

	public long insertArticle(String title, String author, String content,
			String date, String url) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TITLE, title);
		initialValues.put(KEY_AUTHOR, author);
		initialValues.put(KEY_CONTENT, content);
		initialValues.put(KEY_DATE, date);
		initialValues.put(KEY_URL, url);
		long rowid;
		try {
			rowid = db.insert(DATABASE_TABLE, null, initialValues);
			return rowid;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	private Article cursorToArticle(Cursor cursor) {
		Article article = new Article();
		article.setId(cursor.getLong(0));
		article.setTitle(cursor.getString(1));
		article.setAuthor(cursor.getString(2));
		article.setContent(cursor.getString(3));
		article.setContent(cursor.getString(4));
		article.setUrl(cursor.getString(5));
		return article;
	}

	// get an article with an ID
	public Article getArticle(long rowId) throws SQLException {
		Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_TITLE, KEY_AUTHOR, KEY_CONTENT, KEY_DATE, KEY_URL },
				KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return cursorToArticle(mCursor);
	}

	// search for article
	public Article searchArticlesByKey (String key, String value) throws SQLException {
		Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_TITLE, KEY_AUTHOR, KEY_CONTENT, KEY_DATE, KEY_URL },
				key + "=\"" + value + "\"", null, null, null, null, null);
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			return cursorToArticle(mCursor);
		} else {
			return null;
		}
	}

}
