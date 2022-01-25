package com.example.appmusicmp3;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class PlayMP3Activity extends Activity {
    private TextView tvTitle, tvArtist, tvTimeStart, tvTimeEnd;
    private ImageButton btnPlay, btnNext, btnPrev, btnBack;
    private SeekBar sbTime;
    private CheckBox cbLike, cbRandom, cbRepeat;
    private Song songPlaying;

    private BroadcastReceiver autoNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Tags.ACTION_SERVICE_AUTO_NEXT.equals(intent.getAction())) {
                setDataView();
                setTimeFinish();
                updateTime();
            }
        }
    };

    private BroadcastReceiver notificationPlayPause = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Tags.NOTIFICATION_PLAY_PAUSE.equals(intent.getAction())) {
                if (MusicBuilder.g().getMediaPlayer().isPlaying()) {
                    btnPlay.setImageResource(R.drawable.icons_pause);
                } else {
                    btnPlay.setImageResource(R.drawable.icons_play);
                }
            }
        }
    };

    private BroadcastReceiver notificationNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Tags.NOTIFICATION_NEXT.equals(intent.getAction())) {
                setDataView();
                setTimeFinish();
                updateTime();
            }
        }
    };

    private BroadcastReceiver notificationPrev = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Tags.NOTIFICATION_PREV.equals(intent.getAction())) {
                setDataView();
                setTimeFinish();
                updateTime();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_mp3);
        registerReceiver();
        findView();
        initAction();
        setDataViewIntent(getIntent());
        setTimeFinish();
        updateTime();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            setDataViewIntent(intent);
            setTimeFinish();
            updateTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDataViewIntent(Intent intent) {
        if (intent.getBooleanExtra(Tags.IS_FROM_SERVICE, false)) {
            songPlaying = MusicBuilder.g().getSongPlaying();
        } else {
            songPlaying = (Song) intent.getSerializableExtra(Tags.EXTRA_MP3_SONG);
        }
        if (MusicBuilder.g().getSongPlaying() == null) {
            return;
        }
        if (MusicBuilder.g().getMediaPlayer().isPlaying()) {
            btnPlay.setImageResource(R.drawable.icons_pause);
        } else {
            btnPlay.setImageResource(R.drawable.icons_play);
        }
        if (MusicBuilder.g().isRandom()) {
            cbRandom.setChecked(true);
        } else {
            cbRandom.setChecked(false);
        }
        if (MusicBuilder.g().isRepeat()) {
            cbRepeat.setChecked(true);
        } else {
            cbRepeat.setChecked(false);
        }
        if (checkFavorite(MusicBuilder.g().getSongPlaying())) {
            cbLike.setChecked(true);
        } else {
            cbLike.setChecked(false);
        }
        tvTitle.setText(songPlaying.getTitle());
        tvArtist.setText(songPlaying.getArtist());
    }

    private void setDataView() {
        songPlaying = MusicBuilder.g().getSongPlaying();
        tvTitle.setText(songPlaying.getTitle());
        tvArtist.setText(songPlaying.getArtist());
        btnPlay.setImageResource(R.drawable.icons_pause);
        if (checkFavorite(MusicBuilder.g().getSongPlaying())) {
            cbLike.setChecked(true);
        } else {
            cbLike.setChecked(false);

        }
    }

    private void setTimeFinish() {
        if (MusicBuilder.g().getSongPlaying() == null) {
            return;
        }
        Log.d("check", "set time finish");
        SimpleDateFormat formatTime = new SimpleDateFormat("mm:ss");
        tvTimeEnd.setText(formatTime.format(MusicBuilder.g().getMediaPlayer().getDuration()));
        // gán max sbTime = MusicBuilder.g().getMediaPlayer().getDuration()
        sbTime.setMax(MusicBuilder.g().getMediaPlayer().getDuration());
    }

    private void updateTime() {
        Log.d("check", "update time");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (MusicBuilder.g().getMediaPlayer() == null) {
                    return;
                }
                SimpleDateFormat formatTime = new SimpleDateFormat("mm:ss");
                try {
                    tvTimeStart.setText(formatTime.format(MusicBuilder.g().getMediaPlayer().getCurrentPosition()));
                    sbTime.setProgress(MusicBuilder.g().getMediaPlayer().getCurrentPosition());
                    handler.postDelayed(this, 200);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 100);
    }

    private void initAction() {

        // play-pause
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicBuilder.g().getMediaPlayer() == null) {
                    return;
                }
                if (MusicBuilder.g().getMediaPlayer().isPlaying()) {
                    MusicBuilder.g().pause();
                    btnPlay.setImageResource(R.drawable.icons_play);
                } else {
                    MusicBuilder.g().play();
                    btnPlay.setImageResource(R.drawable.icons_pause);
                }
                Intent intent = new Intent(Tags.ACTION_MP3_PLAY_PAUSE);
                sendBroadcast(intent);
            }
        });

        // next
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicBuilder.g().getMediaPlayer() == null) {
                    return;
                }
                MusicBuilder.g().stop();
                MusicBuilder.g().nextSong();
                MusicBuilder.g().initMediaPlayer(PlayMP3Activity.this, MusicBuilder.g().getSongPlaying());
                MusicBuilder.g().play();
                setDataView();
                setTimeFinish();
                updateTime();

                Intent intent = new Intent(Tags.ACTION_MP3_NEXT);
                sendBroadcast(intent);
            }
        });

        //prev
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicBuilder.g().getMediaPlayer() == null) {
                    return;
                }
                MusicBuilder.g().stop();
                MusicBuilder.g().preSong();
                MusicBuilder.g().initMediaPlayer(PlayMP3Activity.this, MusicBuilder.g().getSongPlaying());
                setDataView();
                MusicBuilder.g().play();
                setTimeFinish();
                updateTime();

                Intent intent = new Intent(Tags.ACTION_MP3_PREV);
                sendBroadcast(intent);
            }
        });
        // random
        cbRandom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cbRepeat.setChecked(false);
                    MusicBuilder.g().setRepeat(false);
                    MusicBuilder.g().setRandom(true);
                    MusicBuilder.g().setListShuffle();
                } else {
                    MusicBuilder.g().setRandom(false);
                }
            }
        });
        // repeat
        cbRepeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cbRandom.setChecked(false);
                    MusicBuilder.g().setRepeat(true);
                    MusicBuilder.g().setRandom(false);
                } else {
                    MusicBuilder.g().setRepeat(false);
                }
            }
        });
        // favorite
        cbLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (checkFavorite(MusicBuilder.g().getSongPlaying())) {
                        return;
                    }
                    DatabaseSong.getInstance(PlayMP3Activity.this).daoSong().insertSong(MusicBuilder.g().getSongPlaying());
                } else {
                    ArrayList<Song> list = new ArrayList<>();
                    list = (ArrayList<Song>) DatabaseSong.getInstance(PlayMP3Activity.this).daoSong().getListSong();
                    for (int i = 0; i < list.size(); i++) {
                        if (MusicBuilder.g().getSongPlaying().getId().equals(list.get(i).getId())) {
                            DatabaseSong.getInstance(PlayMP3Activity.this).daoSong().deleteSong(list.get(i));
                            break;
                        }
                    }

                }
                Log.d("cbLike", DatabaseSong.getInstance(PlayMP3Activity.this).daoSong().getListSong().toString());
            }
        });

        // touch seekbar
        sbTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MusicBuilder.g().getMediaPlayer().seekTo(sbTime.getProgress());
            }
        });

        // back
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private boolean checkFavorite(Song song) {
        ArrayList<Song> list = (ArrayList<Song>) DatabaseSong.getInstance(PlayMP3Activity.this).daoSong().checkSong(song.getId());
        return list != null && !list.isEmpty();
    }

    private void registerReceiver() {
        IntentFilter filterAutoNext = new IntentFilter(Tags.ACTION_SERVICE_AUTO_NEXT);
        registerReceiver(autoNext, filterAutoNext);

        IntentFilter filterNotificationPlayPause = new IntentFilter(Tags.NOTIFICATION_PLAY_PAUSE);
        registerReceiver(notificationPlayPause, filterNotificationPlayPause);

        IntentFilter filterNotificationNext = new IntentFilter(Tags.NOTIFICATION_NEXT);
        registerReceiver(notificationNext, filterNotificationNext);

        IntentFilter filterNotificationPrev = new IntentFilter(Tags.NOTIFICATION_PREV);
        registerReceiver(notificationPrev, filterNotificationPrev);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(autoNext);
        unregisterReceiver(notificationPlayPause);
        unregisterReceiver(notificationNext);
        unregisterReceiver(notificationPrev);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void findView() {
        tvTitle = findViewById(R.id.tvPlayTitle);
        tvTitle.setSelected(true);
        tvArtist = findViewById(R.id.tvPlayArtist);
        tvTimeStart = findViewById(R.id.tvTimeStart);
        tvTimeEnd = findViewById(R.id.tvTimeEnd);
        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnBack = findViewById(R.id.btnBack);
        cbRandom = findViewById(R.id.cbRandom);
        cbRepeat = findViewById(R.id.cbRepeat);
        sbTime = findViewById(R.id.sbTime);
        cbLike = findViewById(R.id.cbLike);
    }
}