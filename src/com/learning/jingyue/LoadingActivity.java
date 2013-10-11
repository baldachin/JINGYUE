package com.learning.jingyue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LoadingActivity extends Activity {
	
//	ListDatabase listDB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);
		
//		listDB = new ListDatabase(this);
		
		Intent intent = new Intent(this ,MainListActivity.class);
		startActivity(intent);
		 
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.loading, menu);
//		return true;
//	}

}
