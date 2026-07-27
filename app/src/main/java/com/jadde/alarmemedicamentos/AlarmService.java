package com.jadde.alarmemedicamentos;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;

public final class AlarmService extends Service {
    public static final String ACTION_STOP =
            "com.jadde.alarmemedicamentos.STOP_ALARM";
    private static final String CHANNEL_ID = "medicine_alarms";
    private static final int NOTIFICATION_ID = 7701;

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopAlarm();
            return START_NOT_STICKY;
        }

        String time = intent == null ? "" : intent.getStringExtra("time");
        String medicine = intent == null ? "Remédio" : intent.getStringExtra("medicine");
        String color = intent == null ? Dose.BLACK : intent.getStringExtra("color");
        int phase = intent == null ? 1 : intent.getIntExtra("phase", 1);

        createNotificationChannel();
        startForeground(
                NOTIFICATION_ID,
                buildNotification(time, medicine, color, phase)
        );
        startSoundAndVibration();
        return START_NOT_STICKY;
    }

    private Notification buildNotification(
            String time,
            String medicine,
            String color,
            int phase
    ) {
        Intent fullScreen = new Intent(this, AlarmActivity.class);
        fullScreen.putExtra("time", time);
        fullScreen.putExtra("medicine", medicine);
        fullScreen.putExtra("color", color);
        fullScreen.putExtra("phase", phase);
        fullScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int immutableFlag =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_IMMUTABLE
                        : 0;
        PendingIntent fullScreenIntent = PendingIntent.getActivity(
                this,
                8101,
                fullScreen,
                PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag
        );

        Intent stopIntent = new Intent(this, AlarmService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this,
                8102,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag
        );

        return new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setColor(ScheduleFactory.colorValue(color))
                .setContentTitle(time + " • " + ScheduleFactory.colorLabel(color))
                .setContentText(medicine)
                .setCategory(Notification.CATEGORY_ALARM)
                .setPriority(Notification.PRIORITY_MAX)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false)
                .setFullScreenIntent(fullScreenIntent, true)
                .setContentIntent(fullScreenIntent)
                .addAction(0, "Já apliquei — parar", stopPendingIntent)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null || manager.getNotificationChannel(CHANNEL_ID) != null) {
            return;
        }

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Alarmes de remédios",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Alarmes do cronograma pós-operatório");
        channel.enableLights(true);
        channel.setLightColor(Color.GREEN);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 700, 350, 700, 350});
        channel.setSound(sound, attributes);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        manager.createNotificationChannel(channel);
    }

    private void startSoundAndVibration() {
        stopSoundAndVibration();
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmSound == null) {
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        mediaPlayer = MediaPlayer.create(this, alarmSound);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 700, 350, 700, 350};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {
                vibrator.vibrate(pattern, 0);
            }
        }
    }

    private void stopSoundAndVibration() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
    }

    private void stopAlarm() {
        stopSoundAndVibration();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        stopSoundAndVibration();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
