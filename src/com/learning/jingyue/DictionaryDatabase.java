/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.learning.jingyue;

import android.app.SearchManager;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Contains logic to return specific words from the dictionary, and
 * load the dictionary table when it needs to be created.
 */
public class DictionaryDatabase {
    private static final String TAG = "DictionaryDatabase";

    //The columns we'll include in the dictionary table
    //定义SearchManager.SUGGEST作为搜索框内容
    public static final String D_WORD = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String D_DEFINITION = SearchManager.SUGGEST_COLUMN_TEXT_2;
    public static final String D_TYPE = "type";
    public static final String D_NUM = "num";
//    public static final String D_WORD = "word";
//    public static final String D_DEFINITION = "definition";
    

    //数据库参数
    private static final String DATABASE_NAME = "dictionary";
    private static final String FTS_VIRTUAL_TABLE = "FTSdictionary";
    private static final int DATABASE_VERSION = 1;

    //声明DbHelper
    private final DictionaryOpenHelper mDatabaseOpenHelper;
    //声明哈希map
    private static final HashMap<String,String> mColumnMap = buildColumnMap();

    /**
     * Constructor
     * @param context The Context within which to work, used to create the DB
     */
    //构造函数，取context参数实例DbHelp
    public DictionaryDatabase(Context context) {
        mDatabaseOpenHelper = new DictionaryOpenHelper(context);
    }

    /**
     * Builds a map for all columns that may be requested, which will be given to the 
     * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include 
     * all columns, even if the value is the key. This allows the ContentProvider to request
     * columns w/o the need to know real column names and create the alias itself.
     */
    //哈希表构建方法
    private static HashMap<String,String> buildColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put(D_TYPE, D_TYPE);
        map.put(D_NUM, D_NUM);
        map.put(D_WORD, D_WORD);
        map.put(D_DEFINITION, D_DEFINITION);
        map.put(BaseColumns._ID, "rowid AS " +
                BaseColumns._ID);
        
        //搜索弹出项定义
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        return map;
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
//        String selection = null;
//        String selection = D_WORD + " MATCH ?";
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
     * Performs a database query.
     * @param selection The selection clause
     * @param selectionArgs Selection arguments for "?" components in the selection
     * @param columns The columns to return
     * @return A Cursor over all rows matching the query
     */
    //query����
    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
    	//ͨ��SQLiteQueryBuilderʵ����ɲ�ѯ
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);
        //����ʹ�ù�ϣ��Ϊbuilder����
        builder.setProjectionMap(mColumnMap);

        //ͨ��builder��ѯ���ָ��
        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        //��ָ���߼�
        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }


    /**
     * This creates/opens the database.
     */
    //ʹ��DbHelper�࣬����&������ݿ�
    private static class DictionaryOpenHelper extends SQLiteOpenHelper {

        private final Context mHelperContext;
        private SQLiteDatabase mDatabase;

        /* Note that FTS3 does not support column constraints and thus, you cannot
         * declare a primary key. However, "rowid" is automatically used as a unique
         * identifier, so when making requests, we will use "_id" as an alias for "rowid"
         */
        private static final String FTS_TABLE_CREATE =
                    "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                    " USING fts3 (" +
                    D_TYPE + ", " +
                    D_NUM + ", " +
                    D_WORD + ", " +
                    D_DEFINITION + ");";

        //DbHelper���캯��
        DictionaryOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mHelperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mDatabase = db;
            //ʹ��execSQL��������
            mDatabase.execSQL(FTS_TABLE_CREATE);
            //ʹ��loadDictionary�����������
            loadDictionary();
        }

        /**
         * Starts a thread to load the database table with words
         */
        //loadDictionary����
        private void loadDictionary() {
        	//���߳�
            new Thread(new Runnable() {
                public void run() {
                    try {
                    	//loadWords����
                        loadWords();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

        //loadWords������
        private void loadWords() throws IOException {
            Log.d(TAG, "Loading words...");
            //ͨ��context��ȡresources��
            final Resources resources = mHelperContext.getResources();
            //ʵ��InputStream����ͨ��openRawResource������ȡdefinitions�ļ���
            InputStream inputStream = resources.openRawResource(R.raw.list);
            //ʵ��reader���󣬵���inputstream����
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] strings = TextUtils.split(line, "\t");
                    if (strings.length < 2) continue;
                    long id = addWord(strings[0].trim(), strings[1].trim(), strings[2].trim(), strings[3].trim());
                    if (id < 0) {
                        Log.e(TAG, "unable to add word: " + strings[3].trim());
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
        public long addWord(String type, String num, String word, String definition) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(D_TYPE, type);
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

}
