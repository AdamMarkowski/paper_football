package pl.uj.edu.football;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class Settings extends Activity {

	private EditText name1, name2;
	private String tmp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_settings, menu);
		return true;
	}
	
	public void saveSettings(View v){
		name1 = (EditText)findViewById(R.id.name1); 
		name2 = (EditText)findViewById(R.id.name2); 
		
		tmp = name1.getText().toString();
		Singleton.setName1(tmp);
		
		tmp = name2.getText().toString();
		Singleton.setName2(tmp);
		
		this.finish();
		
	}

}
