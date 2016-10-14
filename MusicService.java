package com.example.tanyairenesheppard.afinally.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.tanyairenesheppard.afinally.R;
import com.example.tanyairenesheppard.afinally.helper.Song;
import com.example.tanyairenesheppard.afinally.R;

import java.util.ArrayList;

/**
 * Created by AcadGildMentor on 6/8/2015.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private MediaPlayer mPlayer;
    private Uri mSongUri;

    private ArrayList<Song> mListSongs;
    private int SONG_POS = 0;

    private final IBinder musicBind = new PlayerBinder();

    private final String ACTION_STOP = "com.acadgild.musicapp.STOP";
    private final String ACTION_NEXT = "com.acadgild.musicapp.NEXT";
    private final String ACTION_PREVIOUS = "com.acadgild.musicapp.PREVIOUS";
    private final String ACTION_PAUSE = "com.acadgild.musicapp.PAUSE";

    private static final int STATE_PAUSED = 1;
    private static final int STATE_PLAYING = 2;
    private int mState = 0;
    private static final int REQUEST_CODE_PAUSE = 101;
    private static final int REQUEST_CODE_PREVIOUS = 102;
    private static final int REQUEST_CODE_NEXT = 103;
    private static final int REQUEST_CODE_STOP = 104;
    public static int NOTIFICATION_ID = 11;
    private Notification.Builder notificationBuilder;
    private Notification mNotification;

    public class PlayerBinder extends Binder {//Service connection to play in background

        public MusicService getService() {
            Log.d("test", "getService()");
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("test", "onBind Called ");
        return musicBind;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Initializing the media player object
        mPlayer = new MediaPlayer();
        initPlayer();
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        notificationBuilder = new Notification.Builder(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                if (action.equals(ACTION_PAUSE)) {
                    playPauseSong();
                } else if (action.equals(ACTION_NEXT)) {
                    nextSong();
                } else if (action.equals(ACTION_PREVIOUS)) {
                    previousSong();
                } else if (action.equals(ACTION_STOP)) {
                    stopSong();
                    stopSelf();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //Stop the Mediaplayer
        mPlayer.stop();
        mPlayer.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlayer.reset();
        try {
            if (SONG_POS != mListSongs.size() - 1) {
                SONG_POS++;
            } else
                SONG_POS = 0;
            mPlayer.setDataSource(getApplicationContext(), mListSongs.get(SONG_POS).getSongUri());
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        mPlayer.prepareAsync();

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mPlayer.start();
    }

    private void initPlayer() {
        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public void startSong(Uri songuri, String songName) {//Set data & start playing music

        mPlayer.reset();
        mState = STATE_PLAYING;
        mSongUri = songuri;
        try {
            mPlayer.setDataSource(getApplicationContext(), mSongUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        mPlayer.prepareAsync();
        updateNotification(songName);
    }

    public void playPauseSong() {

        if (mState == STATE_PAUSED) {
            mState = STATE_PLAYING;
            mPlayer.start();
        } else {
            mState = STATE_PAUSED;
            mPlayer.pause();
        }
    }

    public void stopSong() {
        mPlayer.stop();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
        System.exit(0);
    }

    public void nextSong() {
        startSong(mListSongs.get(SONG_POS + 1).getSongUri(), mListSongs.get(SONG_POS + 1).getSongName());
        SONG_POS++;
    }

    public void previousSong() {
        startSong(mListSongs.get(SONG_POS - 1).getSongUri(), mListSongs.get(SONG_POS - 1).getSongName());
        SONG_POS--;
    }

    public void setSongURI(Uri uri) {
        this.mSongUri = uri;
    }



    /*public void setSongList(ArrayList<Song> listSong, int pos, int notification_id) {
        mListSongs = listSong;
        SONG_POS = pos;
        NOTIFICATION_ID = notification_id;
    }*/

    public void setSelectedSong(int pos, int notification_id) {
        SONG_POS = pos;
        NOTIFICATION_ID = notification_id;
        setSongURI(mListSongs.get(SONG_POS).getSongUri());
        showNotification();
        startSong(mListSongs.get(SONG_POS).getSongUri(), mListSongs.get(SONG_POS).getSongName());
    }

    public void setSongList(ArrayList<Song> listSong) {
        mListSongs = listSong;
    }

    public void showNotification() {
        PendingIntent pendingIntent;
        Intent intent;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        RemoteViews notificationView = new RemoteViews(getPackageName(), R.layout.notification_mediacontroller);

        notificationView.setTextViewText(R.id.notify_song_name, mListSongs.get(SONG_POS).getSongName());

        intent = new Intent(ACTION_STOP);
        pendingIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_STOP, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationView.setOnClickPendingIntent(R.id.notify_btn_stop, pendingIntent);

        intent = new Intent(ACTION_PAUSE);
        pendingIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_PAUSE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationView.setOnClickPendingIntent(R.id.notify_btn_pause, pendingIntent);

        intent = new Intent(ACTION_PREVIOUS);
        pendingIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_PREVIOUS, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationView.setOnClickPendingIntent(R.id.notify_btn_previous, pendingIntent);

        intent = new Intent(ACTION_NEXT);
        pendingIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_NEXT, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationView.setOnClickPendingIntent(R.id.notify_btn_next, pendingIntent);

        mNotification = notificationBuilder
                .setSmallIcon(R.drawable.app_icon).setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setContent(notificationView)
                .setDefaults(Notification.FLAG_NO_CLEAR)
                .build();
        notificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    private void updateNotification(String songName) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotification.contentView.setTextViewText(R.id.notify_song_name, songName);
        notificationManager.notify(NOTIFICATION_ID, mNotification);
    }
}
