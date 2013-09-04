package com.learning.jingyue;


import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends ListActivity {

	SQLiteDatabase db;
	DbHelper dbHelper;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbHelper = new DbHelper(this);
		db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(DbHelper.TABLE, null, null, null, null, null,null);
//		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,R.layout.list, cursor, new String[]{DbHelper.C_TYPE, DbHelper.C_WORD, DbHelper.C_NUM, DbHelper.C_DEFINITION},new int[]{R.id.type, R.id.word, R.id.num, R.id.definition});
//		setListAdapter(adapter);

	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
	}


}
