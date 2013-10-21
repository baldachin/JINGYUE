package com.learning.jingyue;

import android.os.Bundle;
import android.provider.BaseColumns;
import android.app.Activity;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.widget.ListView;

public class MainListActivity extends Activity {

	private ListView mListView;
	ListDatabase1 db;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_list);
		
		db = new ListDatabase1(this);
        mListView = (ListView) findViewById(R.id.mainlist);
//        String query = "";
//        String[] columns = new String[] {BaseColumns._ID ,db.D_SOURCE ,db.D_TYPE ,db.D_SUBTYPE ,db.D_NUM ,db.D_WORD ,db.D_DEFINITION};
//        Cursor c = db.getList(null);
//		String[] from = new String[] {db.D_TYPE, db.D_NUM,db.D_WORD, db.D_DEFINITION};
//		int[] to = new int[] {R.id.textView_Type, R.id.textView_Num, R.id.textView_Word ,R.id.textView_Definition};
//        @SuppressWarnings("deprecation")
//		SimpleCursorAdapter list = new SimpleCursorAdapter(this, R.layout.main_list_row, c, from, to);
//        mListView.setAdapter(list);
        
//		db = new ListDatabase(getActivity());
//		String[] columns = new String[]{db.D_SOURCE,db.D_TYPE};
        String[] columns = new String[] {BaseColumns._ID ,db.D_SOURCE ,db.D_TYPE};
		Cursor c = db.getList(columns);
		String[] from = new String[] {db.D_TYPE, db.D_SOURCE};
		int[] to = new int[] {android.R.id.text1,android.R.id.text2};
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_activated_2, c, from, to);
		mListView.setAdapter(adapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_list, menu);
		return true;
	}

}
