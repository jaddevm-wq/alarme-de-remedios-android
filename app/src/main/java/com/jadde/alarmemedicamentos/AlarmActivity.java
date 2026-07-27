package com.jadde.alarmemedicamentos;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class AlarmActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }

        String time = getIntent().getStringExtra("time");
        String medicine = getIntent().getStringExtra("medicine");
        String color = getIntent().getStringExtra("color");
        int phase = getIntent().getIntExtra("phase", 1);
        setContentView(buildScreen(time, medicine, color, phase));
    }

    private View buildScreen(String time, String medicine, String color, int phase) {
        int padding = dp(28);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(padding, dp(40), padding, padding);
        root.setBackgroundColor(0xFFF3F7F4);

        View stripe = new View(this);
        stripe.setBackgroundColor(ScheduleFactory.colorValue(color));
        root.addView(stripe, new LinearLayout.LayoutParams(dp(76), dp(10)));

        TextView eyebrow = text(
                "TABELA " + phase + " • " + ScheduleFactory.colorLabel(color),
                15,
                true,
                ScheduleFactory.colorValue(color)
        );
        LinearLayout.LayoutParams eyebrowParams =
                new LinearLayout.LayoutParams(-2, -2);
        eyebrowParams.topMargin = dp(26);
        root.addView(eyebrow, eyebrowParams);

        TextView timeText = text(time, 58, true, 0xFF17221E);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(-2, -2);
        timeParams.topMargin = dp(14);
        root.addView(timeText, timeParams);

        TextView title = text(medicine, 25, true, 0xFF17221E);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(-1, -2);
        titleParams.topMargin = dp(12);
        root.addView(title, titleParams);

        TextView instruction = text(
                Dose.OINTMENT.equals(color)
                        ? "Aplicar antes de dormir, conforme a prescrição."
                        : "Confira a cor e o nome do frasco antes de aplicar.",
                16,
                false,
                0xFF69746F
        );
        instruction.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams instructionParams =
                new LinearLayout.LayoutParams(-1, -2);
        instructionParams.topMargin = dp(14);
        root.addView(instruction, instructionParams);

        Button stop = new Button(this);
        stop.setText("JÁ APLIQUEI — PARAR ALARME");
        stop.setTextColor(Color.WHITE);
        stop.setTextSize(15);
        stop.setAllCaps(false);
        stop.setBackgroundColor(0xFF1F6F54);
        stop.setOnClickListener(view -> stopAndFinish());
        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(-1, dp(58));
        buttonParams.topMargin = dp(42);
        root.addView(stop, buttonParams);

        TextView warning = text(
                "Este aplicativo organiza os horários fotografados e não substitui a orientação médica.",
                12,
                false,
                0xFF7A6542
        );
        warning.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams warningParams =
                new LinearLayout.LayoutParams(-1, -2);
        warningParams.topMargin = dp(22);
        root.addView(warning, warningParams);
        return root;
    }

    private void stopAndFinish() {
        Intent stop = new Intent(this, AlarmService.class);
        stop.setAction(AlarmService.ACTION_STOP);
        startService(stop);
        finishAndRemoveTask();
    }

    private TextView text(String value, int size, boolean bold, int color) {
        TextView view = new TextView(this);
        view.setText(value == null ? "" : value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) {
            view.setTypeface(view.getTypeface(), android.graphics.Typeface.BOLD);
        }
        return view;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
