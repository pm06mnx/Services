package ru.pm06mnx.services;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private View startServiceButton;
    private View nextScreenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startServiceButton = findViewById(R.id.start_service_button);
        nextScreenButton = findViewById(R.id.next_screen_button);

        startServiceButton.setOnClickListener(v -> {
            startService(GenerationService.newIntent(MainActivity.this));
        });

        nextScreenButton.setOnClickListener(v -> {
            Intent intent = SecondActivity.newIntent(this);
            startActivity(intent);
        });
    }
}
