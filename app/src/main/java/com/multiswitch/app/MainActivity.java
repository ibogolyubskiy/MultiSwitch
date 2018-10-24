package com.multiswitch.app;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.multiswitch.MultiSwitch;

public class MainActivity extends AppCompatActivity {

    private String[] tabTexts1 = {"test1", "test2", "test3", "test4"};
    private String[] tabTexts4 = {"First", "Second", "Third"};

    private MultiSwitch.OnSwitchListener onSwitchListener = new MultiSwitch.OnSwitchListener() {
        @Override
        public void onSwitch(int position, String tabText, boolean isSelected) {
            if (toasts.isChecked()) {
                String action = isSelected ? "selected" : "unselected";
                Toast.makeText(MainActivity.this, tabText + " " + action, Toast.LENGTH_SHORT).show();
            }
        }
    };
    private MultiSwitch switch1;
    private MultiSwitch switch2;
    private MultiSwitch switch3;
    private MultiSwitch switch4;
    private MultiSwitch switch5;
    private Switch toasts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/tielanti.ttf");

        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);
        switch3 = findViewById(R.id.switch3);
        switch4 = findViewById(R.id.switch4);
        switch5 = findViewById(R.id.switch5);
        switch5.setEnabled(false);

        Button unselect = findViewById(R.id.unselect);
        Button select = findViewById(R.id.select);
        toasts = findViewById(R.id.toasts);

        switch1.setText(tabTexts1).setTypeface(typeface).setOnSwitchListener(onSwitchListener);
        Integer in[] = new Integer[]{0, 2};
        for (Integer i : in) {
            switch1.setSelectedTab(i);
        }
        switch2.setText("Day", "Night").setOnSwitchListener(onSwitchListener);
        switch3.setSelectedTab(1).setOnSwitchListener(onSwitchListener);
        switch4.setText(tabTexts4).setOnSwitchListener(onSwitchListener);

        unselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch1.clearSelected();
                switch2.clearSelected();
                switch3.clearSelected();
                switch4.clearSelected();
                switch5.clearSelected();
            }
        });

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            for (int i = 0; i < tabTexts1.length; i++) {
                switch1.setSelectedTab(i);
            }
            }
        });
    }

    public MultiSwitch getSwitchMultiButton() {
        return findViewById(R.id.switch1);
    }
}
