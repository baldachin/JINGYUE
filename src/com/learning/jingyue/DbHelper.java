package com.learning.jingyue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;

import android.database.sqlite.SQLiteDatabase;

import android.database.sqlite.SQLiteOpenHelper;

import android.provider.BaseColumns;

import android.text.TextUtils;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {

	static final String TAG = "DbHelper";

	static final String DB_NAME = "jingyue.db";

	static final int DB_VERSION = 5;

	static final String TABLE = "list";

	static final String C_ID = BaseColumns._ID;

	static final String C_TYPE = "type";
	static final String C_WORD = "word";
	static final String C_NUM = "num";
	static final String C_DEFINITION = "definition";
	
	private SQLiteDatabase mDatabase;
	
	Context context;

	// Constructor

	public DbHelper(Context context) {

		super(context, DB_NAME, null, DB_VERSION);

		this.context = context;

	}

	// Called only once, first time the DB is created

	@Override
	public void onCreate(SQLiteDatabase db) {

		String sql = "create table " + TABLE + " (" 
				+ C_ID
				+ " int primary key, "

				+ C_TYPE + " text, " 
				+ C_WORD + " text, " 
				+ C_NUM + " text, " 
				+ C_DEFINITION + " text)"; 

		mDatabase = db;
		mDatabase.execSQL(sql);

		loadDictionary();
		Log.d(TAG, "onCreated sql: " + sql);

	}

	// Called whenever newVersion != oldVersion

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { // http://dev.icybear.net/learning-android-cn/images/8.png

		// Typically do ALTER TABLE statements, but...we're just in development,

		// so:

		db.execSQL("drop table if exists " + TABLE); // drops the old database

		Log.d(TAG, "onUpgraded");

		onCreate(db); // run onCreate to get new database

	}

    private void loadDictionary() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    loadWords();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void loadWords() throws IOException {
        Log.d(TAG, "Loading words...");
        final Resources resources = context.getResources();
        InputStream inputStream = resources.openRawResource(R.raw.list);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] strings = TextUtils.split(line, "\t");
                if (strings.length < 2) continue;
                long id = addWord(strings[0].trim(), strings[1].trim(), strings[2].trim(), strings[3].trim());
                if (id < 0) {
                    Log.e(TAG, "unable to add word: " + strings[1].trim());
                }
            }
        } finally {
            reader.close();
        }
        Log.d(TAG, "DONE loading words.");
    }

    /**
     * Add a word to the dictionary.
     * @return rowId or -1 if failed
     */
    public long addWord(String type, String word, String num, String definition) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(C_TYPE, type);
        initialValues.put(C_WORD, word);
        initialValues.put(C_NUM, num);
        initialValues.put(C_DEFINITION, definition);

        return mDatabase.insert(TABLE, null, initialValues);
    }
	
	
}
