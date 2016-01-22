package csdn.zhangyaobin.mediarecorderandmediaplayer;

import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, IUpdateSoundLength {

    Button sound_show;

    ImageView sound_delete;
    ImageView sound_mic;
    RelativeLayout sound_delete_layout;
    ImageView sound_recording;


    MediaPlayer mediaPlayer;
    //    MediaPlayer mediaPlayerIntro1, mediaPlayerIntro2, mediaPlayerIntro3;
    MediaRecorder recorder;
    //    String storeLoc = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyRecordings/";
    String soundUrl;
    AnimationDrawable animationDrawable;
    //都放在list中方便拓展多个mediaplayer
    List<MediaPlayer> mediaPlayerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        bindListener();
        animationDrawable = (AnimationDrawable) sound_recording.getDrawable();
        if (checkSdCardExist()) {
            filePath = Environment.getExternalStorageDirectory() + "/MyRecordings.amr";
        } else {
            filePath = "/data/data/" + getPackageName() + "/MyRecordings.amr";
        }
        mediaPlayer = MediaPlayerManager.getInstance().createMediaPlayer(this, this, "", 0);
//        } else {
//            mediaPlayer = MediaPlayerManager.getInstance().createMediaPlayer(this, this, soundUrl, 3);
//        }

        mediaPlayerList.add(mediaPlayer);
        List<AnimationDrawable> animList = new ArrayList<>();
        AnimationDrawable anim4 = (AnimationDrawable) sound_show.getCompoundDrawables()[0];
        animList.add(anim4);

        MediaPlayerManager.getInstance().setSoundWaveAnimationList(animList);
    }

    private void initView() {
        sound_show = (Button) findViewById(R.id.sound_show);
        sound_delete = (ImageView) findViewById(R.id.sound_delete);
        sound_mic = (ImageView) findViewById(R.id.sound_mic);
        sound_recording = (ImageView) findViewById(R.id.sound_recording);
        sound_delete_layout = (RelativeLayout) findViewById(R.id.sound_delete_layout);
    }

    private boolean checkSdCardExist() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    String filePath;

    private void initializeAudio() {
        recorder = new MediaRecorder();// new出MediaRecorder对象
        // 设置MediaRecorder的音频源为麦克风
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // 设置MediaRecorder录制的音频格式
        recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
        // 设置MediaRecorder录制音频的编码为amr.
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        // 设置录制好的音频文件保存路径
        recorder.setOutputFile(filePath);
    }


    private void startRecord() {

        try {
            recorder.prepare();// 准备录制
            recorder.start();// 开始录制
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleStartRecord() {
        //停止所有播放
        MediaPlayerManager.getInstance().stopAllSound(mediaPlayerList);
        //判断是否录音，如果录音，则在退出该界面时上传七牛
        if (!isRecorded) {
            isRecorded = true;
        }
        sound_mic.setImageResource(R.drawable.sound_mic2);
        sound_show.setText("");
        //开启倒计时
        countDown();
        //每次录音必须实例话对象 第二次不能成功录音
        initializeAudio();
        startRecord();
        // 必须reset，否则不能成功播放
        try {
            mediaPlayer.reset();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        sound_recording.setVisibility(View.VISIBLE);
        animationDrawable.start();
        sound_delete_layout.setVisibility(View.VISIBLE);
    }

    private void handleStopRecord() {
        if (recorder == null) {
            return;
        }
        sound_mic.setImageResource(R.drawable.sound_mic1);
        try {
            recorder.stop();
            recorder.release();
            recorder = null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        MediaPlayerManager.getInstance().prepare(mediaPlayer, filePath);
        sound_recording.setVisibility(View.GONE);
        animationDrawable.stop();
    }

    private void bindListener() {
        sound_show.setOnClickListener(this);
        sound_delete.setOnClickListener(this);
        sound_mic.setOnTouchListener(

                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent motionEvent) {
                        if (MotionEvent.ACTION_DOWN == motionEvent.getAction()) {
                            //开始录制
                            handleStartRecord();
                        } else if (MotionEvent.ACTION_MOVE == motionEvent.getAction()) {
//                  不做处理(后续可以做上滑取消)
                        } else if (MotionEvent.ACTION_UP == motionEvent.getAction()) {
                            //结束录制
                            handleStopRecord();
                        }
                        return true;
                    }
                }
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaPlayerManager.getInstance().pauseAllSounds(mediaPlayerList);
    }

    private void countDown() {
    }


    private boolean isRecorded = false;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.sound_show:
                if (fileExists(false)) {
                    MediaPlayerManager.getInstance().restartPlaySound(this, mediaPlayerList, mediaPlayer, 0);
                } else {
                    Toast.makeText(MainActivity.this, "请先录制声音", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.sound_delete:
                //先暂定播放
                MediaPlayerManager.getInstance().stopSound(mediaPlayer, 0);
                deleteFile();
                sound_delete_layout.setVisibility(View.INVISIBLE);
                break;
            default:
                break;
        }
    }

    //删除音频
    private void deleteFile() {
        soundUrl = "";
        sound_show.setText("");
        fileExists(true);
    }

    //判断是否文件存在
    private boolean fileExists(boolean needDelete) {
        File file = new File(filePath);

        if (file.exists()) {
            if (needDelete) {
                file.delete();
            }
            return true;
        }
        return false;
    }

    @Override
    public void updateSoundLength(int position, MediaPlayer mediaPlayer, String length) {
        if (mediaPlayer != null) {
            sound_show.setText(length);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaPlayerManager.getInstance().releaseMediaPlayers(mediaPlayerList);
    }
}
