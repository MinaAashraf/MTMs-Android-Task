package com.mtms_task.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import com.mtms_task.R;

public class SplachActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splach);
        ImageView box = findViewById(R.id.box) , car = findViewById(R.id.car) ;
        TextView title = findViewById(R.id.title);

        box.animate().translationY(0).setDuration(1000);
        title.animate().translationY(0).setDuration(1000);
        car.animate().translationX(2000).setStartDelay(1250).setDuration(3500);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplachActivity.this,RequestsActivity.class));
                finish();
            }
        }, 3500);
    }
}