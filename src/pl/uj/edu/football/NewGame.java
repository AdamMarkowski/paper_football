package pl.uj.edu.football;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

//	
//
//}
public class NewGame extends Activity {
    DrawView drawView;
    private TextView name1, name2, score1, score2;
	private static int score1_int, score2_int;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set full screen view
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                         WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        drawView = new DrawView(this);
        setContentView(drawView);
		name1 = (TextView)findViewById(R.id.name1); 
		name2 = (TextView)findViewById(R.id.name2);
        drawView.requestFocus();
        Singleton.setContext(getApplicationContext());
    }
    
	public void scoreGoal1(View view){
		score1_int++;
		score1.setText(String.valueOf(score1_int));
	}
	
	public void scoreGoal2(View view){
		score2_int++;
		score2.setText(String.valueOf(score2_int));
	}
}
