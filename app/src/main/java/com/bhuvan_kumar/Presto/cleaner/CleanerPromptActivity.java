package com.bhuvan_kumar.Presto.cleaner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.bhuvan_kumar.Presto.R;
import com.bhuvan_kumar.Presto.app.Activity;

public class CleanerPromptActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cleaner_prompt);

        Button button = findViewById(R.id.button1);
        button.setOnClickListener(view -> {
            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
            System.exit(0);
        });
    }
}
