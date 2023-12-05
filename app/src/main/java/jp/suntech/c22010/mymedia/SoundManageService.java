package jp.suntech.c22010.mymedia;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;

public class SoundManageService extends Service {

    private MediaPlayer _player;
    private static final String CHANNEL_ID = "soundmanagerservice_notification_channel";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        _player = new MediaPlayer();

        String name = getString(R.string.notification_channel_name);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String mediaFileUriStr = "android.resource://" + getPackageName() + "/" + R.raw.semi_sound;

        Uri mediaFileUri = Uri.parse(mediaFileUriStr);
        try{
            _player.setDataSource(SoundManageService.this, mediaFileUri);
            _player.setOnPreparedListener(new PlayerPreparedListener());
            _player.setOnCompletionListener(new PlayerCompletionListener());
            _player.prepareAsync();
        }
        catch(IOException ex){
            Log.e("ServiceSample", "メディアプレーヤー準備時の例外発生");
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        if(_player.isPlaying()){
            _player.stop();
        }

        _player.release();
    }

    //プレーヤーの再生準備が整ったときのリスナクラス
    private class PlayerPreparedListener implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
        }
    }

    //再生が終了したときのリスナクラス
    private class PlayerCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(SoundManageService.this, CHANNEL_ID);
            builder.setSmallIcon(android.R.drawable.ic_dialog_info);
            builder.setContentTitle(getString(R.string.msg_notification_title_finish));
            builder.setContentText(getString(R.string.msg_notification_text_finish));
            Notification notification = builder.build();
            NotificationManagerCompat manager = NotificationManagerCompat.from(SoundManageService.this);
            manager.notify(100, notification);
            stopSelf();
        }
    }
}