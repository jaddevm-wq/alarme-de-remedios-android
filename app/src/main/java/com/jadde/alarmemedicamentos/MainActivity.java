package com.jadde.alarmemedicamentos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class MainActivity extends Activity {
    private final Calendar startDate = Calendar.getInstance();
    private String firstDayFrom = "07:00";
    private String bedtime = "23:30";

    private TextView startDateValue;
    private TextView firstDayValue;
    private TextView bedtimeValue;
    private TextView summary;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startDate.set(Calendar.HOUR_OF_DAY, 12);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);
        setContentView(buildScreen());
        refreshSummary();
        refreshStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private View buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(0xFFEEF4F0);

        LinearLayout root = column();
        root.setPadding(dp(18), 0, dp(18), dp(36));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        LinearLayout hero = column();
        hero.setPadding(dp(20), dp(34), dp(20), dp(34));
        hero.setBackgroundColor(0xFF164D3C);
        root.addView(hero, params(-1, -2, 0, 0, 0, dp(16)));

        TextView eyebrow = text("PÓS-OPERATÓRIO", 13, true, 0xFFB6DFCD);
        hero.addView(eyebrow);
        TextView title = text("Alarme de remédios", 32, true, Color.WHITE);
        hero.addView(title, params(-1, -2, 0, dp(8), 0, 0));
        TextView subtitle = text(
                "Programe os 15 dias do tratamento com um único toque.",
                15,
                false,
                0xFFD6E7DF
        );
        hero.addView(subtitle, params(-1, -2, 0, dp(8), 0, 0));

        LinearLayout setup = card();
        root.addView(setup, params(-1, -2, 0, 0, 0, dp(14)));
        setup.addView(sectionTitle("1", "Configure o tratamento"));

        startDateValue = settingRow(
                setup,
                "Início da Tabela 1",
                dateLabel(startDate.getTime()),
                view -> chooseDate()
        );
        firstDayValue = settingRow(
                setup,
                "No primeiro dia, iniciar a partir de",
                firstDayFrom,
                view -> chooseTime(true)
        );
        bedtimeValue = settingRow(
                setup,
                "Horário de dormir — pomada",
                bedtime,
                view -> chooseTime(false)
        );

        TextView firstDayHint = text(
                "A orientação impressa informa: iniciar a Tabela 1 duas horas após a cirurgia. Confirme este primeiro horário com a equipe médica.",
                12,
                false,
                0xFF755A26
        );
        firstDayHint.setPadding(dp(12), dp(12), dp(12), dp(12));
        firstDayHint.setBackgroundColor(0xFFFFF7E7);
        setup.addView(firstDayHint, params(-1, -2, 0, dp(12), 0, 0));

        LinearLayout legend = card();
        root.addView(legend, params(-1, -2, 0, 0, 0, dp(14)));
        legend.addView(sectionTitle("2", "Confira as cores"));
        legend.addView(legendRow(Dose.BLACK, "PRETO", "Vigadexa ou Facoba"));
        legend.addView(legendRow(Dose.GREEN, "VERDE", "Acular"));
        legend.addView(legendRow(Dose.PURPLE, "ROXO", "Azopt ou Dorzal 2,0%"));
        legend.addView(legendRow(Dose.OINTMENT, "POMADA", "Cylocort ou Tobracort"));

        LinearLayout preview = card();
        root.addView(preview, params(-1, -2, 0, 0, 0, dp(14)));
        preview.addView(sectionTitle("3", "Revise e programe"));
        summary = text("", 14, false, 0xFF43514B);
        summary.setLineSpacing(0, 1.25f);
        preview.addView(summary, params(-1, -2, 0, dp(10), 0, 0));

        Button schedule = actionButton("PROGRAMAR TODOS OS ALARMES", 0xFF1F6F54);
        schedule.setOnClickListener(view -> confirmSchedule());
        preview.addView(schedule, params(-1, dp(58), 0, dp(18), 0, 0));

        Button cancel = actionButton("CANCELAR ALARMES PROGRAMADOS", 0xFF7D3F3F);
        cancel.setOnClickListener(view -> confirmCancel());
        preview.addView(cancel, params(-1, dp(52), 0, dp(10), 0, 0));

        status = text("", 13, true, 0xFF1F6F54);
        status.setGravity(Gravity.CENTER);
        status.setPadding(dp(12), dp(12), dp(12), dp(12));
        root.addView(status, params(-1, -2, 0, 0, 0, dp(14)));

        TextView samsungTip = text(
                "No Galaxy S9+: depois de programar, mantenha o volume de alarmes audível e retire este aplicativo da economia de bateria da Samsung.",
                13,
                false,
                0xFF4C5A54
        );
        samsungTip.setPadding(dp(16), dp(16), dp(16), dp(16));
        samsungTip.setBackgroundColor(Color.WHITE);
        root.addView(samsungTip, params(-1, -2, 0, 0, 0, dp(12)));

        TextView safety = text(
                "Importante: este aplicativo apenas organiza os horários da prescrição fotografada. Confirme data, primeiro horário e qualquer dúvida com a equipe médica antes de programar.",
                12,
                false,
                0xFF694D1B
        );
        safety.setPadding(dp(16), dp(16), dp(16), dp(16));
        safety.setBackgroundColor(0xFFFFF9EC);
        root.addView(safety);

        return scroll;
    }

    private void chooseDate() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (picker, year, month, day) -> {
                    startDate.set(year, month, day, 12, 0, 0);
                    startDate.set(Calendar.MILLISECOND, 0);
                    startDateValue.setText(dateLabel(startDate.getTime()));
                    refreshSummary();
                },
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void chooseTime(boolean firstDay) {
        String current = firstDay ? firstDayFrom : bedtime;
        String[] parts = current.split(":");
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (picker, hour, minute) -> {
                    String selected = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                    if (firstDay) {
                        firstDayFrom = selected;
                        firstDayValue.setText(selected);
                    } else {
                        bedtime = selected;
                        bedtimeValue.setText(selected);
                    }
                    refreshSummary();
                },
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                true
        );
        dialog.show();
    }

    private void confirmSchedule() {
        List<Dose> doses = currentSchedule();
        int futureCount = countFuture(doses);
        if (futureCount == 0) {
            Toast.makeText(
                    this,
                    "Não há horários futuros. Confira a data de início.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Programar " + futureCount + " alarmes?")
                .setMessage(
                        "Tabela 1: 5 dias\n"
                                + "Tabela 2: 10 dias\n\n"
                                + "Primeiro dia a partir de " + firstDayFrom + ".\n"
                                + "Pomada diariamente às " + bedtime + ".\n\n"
                                + "Confirme os dados com a equipe médica antes de continuar."
                )
                .setNegativeButton("Revisar", null)
                .setPositiveButton(
                        "Programar",
                        (dialog, which) -> {
                            int scheduled = AlarmScheduler.scheduleAll(this, doses);
                            refreshStatus();
                            Toast.makeText(
                                    this,
                                    scheduled + " alarmes programados.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                )
                .show();
    }

    private void confirmCancel() {
        if (AlarmScheduler.load(this).isEmpty()) {
            Toast.makeText(this, "Não há alarmes programados.", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Cancelar todos os alarmes?")
                .setMessage("Os lembretes futuros deste aplicativo serão removidos.")
                .setNegativeButton("Manter", null)
                .setPositiveButton(
                        "Cancelar alarmes",
                        (dialog, which) -> {
                            AlarmScheduler.cancelAll(this);
                            refreshStatus();
                        }
                )
                .show();
    }

    private void refreshSummary() {
        Calendar phaseOneEnd = (Calendar) startDate.clone();
        phaseOneEnd.add(Calendar.DAY_OF_MONTH, 4);
        Calendar phaseTwoStart = (Calendar) startDate.clone();
        phaseTwoStart.add(Calendar.DAY_OF_MONTH, 5);
        Calendar phaseTwoEnd = (Calendar) startDate.clone();
        phaseTwoEnd.add(Calendar.DAY_OF_MONTH, 14);

        List<Dose> doses = currentSchedule();
        String value =
                "Tabela 1 — "
                        + dateLabel(startDate.getTime())
                        + " a "
                        + dateLabel(phaseOneEnd.getTime())
                        + "\n"
                        + "Tabela 2 — "
                        + dateLabel(phaseTwoStart.getTime())
                        + " a "
                        + dateLabel(phaseTwoEnd.getTime())
                        + "\n\n"
                        + countFuture(doses)
                        + " alarmes futuros serão programados.";
        if (summary != null) {
            summary.setText(value);
        }
    }

    private void refreshStatus() {
        if (status == null) {
            return;
        }
        int remaining = 0;
        long now = System.currentTimeMillis();
        for (Dose dose : AlarmScheduler.load(this)) {
            if (dose.triggerAtMillis > now) {
                remaining++;
            }
        }
        if (remaining > 0) {
            status.setText("✓ " + remaining + " alarmes futuros estão ativos");
            status.setTextColor(0xFF1F6F54);
            status.setBackgroundColor(0xFFDFF2E9);
        } else {
            status.setText("Nenhum alarme está programado");
            status.setTextColor(0xFF69746F);
            status.setBackgroundColor(0xFFE7ECE9);
        }
    }

    private List<Dose> currentSchedule() {
        return ScheduleFactory.create(
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH),
                firstDayFrom,
                bedtime
        );
    }

    private int countFuture(List<Dose> doses) {
        long now = System.currentTimeMillis();
        int count = 0;
        for (Dose dose : doses) {
            if (dose.triggerAtMillis > now) {
                count++;
            }
        }
        return count;
    }

    private TextView settingRow(
            LinearLayout parent,
            String label,
            String value,
            View.OnClickListener listener
    ) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));
        row.setOnClickListener(listener);

        TextView labelView = text(label, 14, true, 0xFF33423C);
        row.addView(labelView, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView valueView = text(value, 15, true, 0xFF1F6F54);
        valueView.setGravity(Gravity.END);
        valueView.setPadding(dp(12), dp(10), dp(12), dp(10));
        valueView.setBackgroundColor(0xFFF3F7F4);
        row.addView(valueView, new LinearLayout.LayoutParams(-2, -2));
        parent.addView(row);
        return valueView;
    }

    private View legendRow(String color, String name, String medicine) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(9), 0, dp(9));

        View dot = new View(this);
        dot.setBackgroundColor(ScheduleFactory.colorValue(color));
        row.addView(dot, new LinearLayout.LayoutParams(dp(14), dp(14)));

        LinearLayout labels = column();
        labels.setPadding(dp(12), 0, 0, 0);
        labels.addView(text(name, 12, true, 0xFF17221E));
        labels.addView(text(medicine, 13, false, 0xFF69746F));
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1f));
        return row;
    }

    private LinearLayout card() {
        LinearLayout card = column();
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackgroundColor(Color.WHITE);
        card.setElevation(dp(2));
        return card;
    }

    private View sectionTitle(String number, String title) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView badge = text(number, 13, true, 0xFF164D3C);
        badge.setGravity(Gravity.CENTER);
        badge.setBackgroundColor(0xFFDFF2E9);
        row.addView(badge, new LinearLayout.LayoutParams(dp(30), dp(30)));

        TextView titleView = text(title, 19, true, 0xFF17221E);
        row.addView(titleView, params(0, -2, dp(12), 0, 0, 0, 1f));
        return row;
    }

    private Button actionButton(String label, int color) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setBackgroundColor(color);
        return button;
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private TextView text(String value, int size, boolean bold, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return view;
    }

    private String dateLabel(Date date) {
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return format.format(date);
    }

    private LinearLayout.LayoutParams params(
            int width,
            int height,
            int left,
            int top,
            int right,
            int bottom
    ) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.setMargins(left, top, right, bottom);
        return params;
    }

    private LinearLayout.LayoutParams params(
            int width,
            int height,
            int left,
            int top,
            int right,
            int bottom,
            float weight
    ) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height, weight);
        params.setMargins(left, top, right, bottom);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
