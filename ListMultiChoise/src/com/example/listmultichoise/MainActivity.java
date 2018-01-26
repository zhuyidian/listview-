package com.example.listmultichoise;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	public void openChoiceModeMultiple(View v) {
		Intent Intent =new Intent(this, ChoiceModeMultipleActivity.class );
		startActivity(Intent);
	}
	
	public void openChoiceModeMultipleModal(View v) {
		Intent Intent =new Intent(this, ChoiceModeMultipleModalActivity.class );
		startActivity(Intent);
	}	
 
}
