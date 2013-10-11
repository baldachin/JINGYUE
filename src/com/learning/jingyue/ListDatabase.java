package com.learning.jingyue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class ListDatabase {
	
	private static final String TAG = "ListDatabase";
	
//	数据库字段定义
	public static final String D_SOURCE = "source";
	public static final String D_TYPE = "type";
	public static final String D_SUBTYPE = "subtype";
	public static final String D_NUM = "num";
	public static final String D_WORD = "word";
	public static final String D_DEFINITION = "definition";
	
//	数据库字段HashMap映射（使用buildColumnMap方法）
    private static final HashMap<String,String> mColumnMap = buildColumnMap();
	
//  数据库基础参数定义
	private static final String DB_NAME = "JingYueList";
	private static final String FTS_VIRTUAL_TABLE = "FTSlist_jian";
	private static final int DB_VERSION = 1;
	
//	DBHelp声明
	private final DBHelp mDbHelp;
	
//	buildColumnMap方法（在此绑定BaseColumns._ID实现_id字段）
    private static HashMap<String,String> buildColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put(D_SOURCE, D_SOURCE);
        map.put(D_TYPE, D_TYPE);
        map.put(D_SUBTYPE, D_SUBTYPE);
        map.put(D_NUM, D_NUM);
        map.put(D_WORD, D_WORD);
        map.put(D_DEFINITION, D_DEFINITION);
        map.put(BaseColumns._ID, "rowid AS " +
                BaseColumns._ID);
        
        return map;
    }
	
//  构造方法，实例DBHelp并获取数据库实现
	public ListDatabase (Context context){
		mDbHelp = new DBHelp(context);
//		mDbHelp.getReadableDatabase();
	}
	
//	私有静态类DBHelp，扩展SQLiteOpenHelper类
	private static class DBHelp extends SQLiteOpenHelper {
        
//		声明Context及数据库
		private final Context mHelperContext;
        private SQLiteDatabase mDatabase;
        
//      定义SQL建表命令，使用VIRTUAL TABLE fts3格式
        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                " USING fts3 (" +
                D_SOURCE + ", " +
                D_TYPE + ", " +
                D_SUBTYPE + ", " +
                D_NUM + ", " +
                D_WORD + ", " +
                D_DEFINITION + ");";
        
//      DBHelp构造方法，指定参数并实例context参数
		public DBHelp(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
            mHelperContext = context;

		}

//		建表，实例数据库并执行SQL命令
		@Override
		public void onCreate(SQLiteDatabase db) {
			mDatabase = db;
			db.execSQL(FTS_TABLE_CREATE);
//			导入数据
			loadDictionary();
		}

//		导入数据方法
		private void loadDictionary() {
            new Thread(new Runnable() {
                public void run() {
                    try {
                    	//loadWords方法
                        loadWords();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            }).start();
		}
		
		private void loadWords() throws IOException {
            Log.d(TAG, "Loading words...");
            //ͨ通过context获取resources资源
            final Resources resources = mHelperContext.getResources();
            //通过InputStream对象调用openRawResource获取raw资源
            InputStream inputStream = resources.openRawResource(R.raw.jingyue_jian);
            //BufferedReader获取InputStream资源
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] strings = TextUtils.split(line, "\t");
                    if (strings.length < 2) continue;
                    long id = addWord(strings[0].trim(), strings[1].trim(), strings[2].trim(), strings[3].trim(), strings[4].trim(), strings[5].trim());
                    if (id < 0) {
                        Log.e(TAG, "unable to add word: " + strings[4].trim());
                    }
                }
            } finally {
                reader.close();
            }
            Log.d(TAG, "DONE loading words.");
		}
		
        public long addWord(String source, String type, String subtype, String num, String word, String definition) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(D_SOURCE, source);
            initialValues.put(D_TYPE, type);
            initialValues.put(D_SUBTYPE, subtype);
            initialValues.put(D_NUM, num);
            initialValues.put(D_WORD, word);
            initialValues.put(D_DEFINITION, definition);

            return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);

		}

	}
	
    /**
     * Performs a database query.
     * @param selection The selection clause
     * @param selectionArgs Selection arguments for "?" components in the selection
     * @param columns The columns to return
     * @return A Cursor over all rows matching the query
     */
    //query方法
    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
    	//ͨ通过SQLiteQueryBuilder实例配置query
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);
        //使用哈希表映像字段
        builder.setProjectionMap(mColumnMap);

        //ͨ通过builder实现query方法，使用方法参数获得指针
        Cursor cursor = builder.query(mDbHelp.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        //指针状态返回
        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }
    
    public Cursor getList() {

        return query(null, null, null);
    }
        
    /**
     * Returns a Cursor over all words that match the given query
     *
     * @param query The string to search for
     * @param columns The columns to include, if null then all are included
     * @return Cursor over all words that match, or null if none found.
     */
    //通过query参数获得全文检索结果
    public Cursor getWordMatches(String query, String[] columns) {
        String selection = FTS_VIRTUAL_TABLE + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};

        return query(selection, selectionArgs, columns);

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE <KEY_WORD> MATCH 'query*'
         * which is an FTS3 search for the query text (plus a wildcard) inside the word column.
         *
         * - "rowid" is the unique id for all rows but we need this value for the "_id" column in
         *    order for the Adapters to work, so the columns need to make "_id" an alias for "rowid"
         * - "rowid" also needs to be used by the SUGGEST_COLUMN_INTENT_DATA alias in order
         *   for suggestions to carry the proper intent data.
         *   These aliases are defined in the DictionaryProvider when queries are made.
         * - This can be revised to also search the definition text with FTS3 by changing
         *   the selection clause to use FTS_VIRTUAL_TABLE instead of KEY_WORD (to search across
         *   the entire table, but sorting the relevance could be difficult.
         */
    }
    
    /**
     * Returns a Cursor positioned at the word specified by rowId
     *
     * @param rowId id of word to retrieve
     * @param columns The columns to include, if null then all are included
     * @return Cursor positioned to matching word, or null if not found.
     */
    //通过明确指定rowId获得单一数据
    public Cursor getWord(String rowId, String[] columns) {
    	String selection = "rowid = ?";
    	String[] selectionArgs = new String[] {rowId};
    	
    	return query(selection, selectionArgs, columns);
    	
    	/* This builds a query that looks like:
    	 *     SELECT <columns> FROM <table> WHERE rowid = <rowId>
    	 */
    }

}