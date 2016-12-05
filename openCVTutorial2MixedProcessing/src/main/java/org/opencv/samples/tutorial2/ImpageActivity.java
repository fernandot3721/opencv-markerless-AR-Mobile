package org.opencv.samples.tutorial2;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ImpageActivity extends Activity {

    Button mBtnBack;
    Button mBtnMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impage);

        mBtnBack = (Button) findViewById(R.id.button);
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ImpageActivity.this, Tutorial2Activity.class);
                startActivity(intent);
                finish();
            }
        });

        mBtnMatch = (Button) findViewById(R.id.button2);
        mBtnMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doMatch();
            }
        });
    }

    private void doMatch() {
        Toast.makeText(getApplicationContext(), "Ready to match", Toast.LENGTH_LONG).show();
    }

}
