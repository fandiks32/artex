package com.example.artext;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.artext.R;
import com.example.artext.TextRecognition.TextReco;

public class MainActivity extends Activity implements OnClickListener {
//hj
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
		setContentView(R.layout.activity_main);
		
		Button about = (Button)findViewById(R.id.aboutButton);
		Button exit = (Button)findViewById(R.id.exitButton);
		Button start = (Button)findViewById(R.id.startButton);
		
		about.setOnClickListener(this);
		exit.setOnClickListener(this);
		start.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
		switch (arg0.getId()) {
		case R.id.aboutButton:
			
			break;
		
		case R.id.startButton:
			Intent myIntent = new Intent();
	        myIntent.setClassName(getPackageName(), getPackageName() + ".TextRecognition.TextReco");
	        startActivity(myIntent);
	        break;
		case R.id.exitButton:
			break;
		}
		
	}

}
