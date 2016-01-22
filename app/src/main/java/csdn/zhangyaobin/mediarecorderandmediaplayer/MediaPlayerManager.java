package csdn.zhangyaobin.mediarecorderandmediaplayer;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangyaobin on 15/11/12.
 */
public class MediaPlayerManager {

    //    private MediaPlayer mediaPlayer;
    Context context;
    IUpdateSoundLength iUpdateSoundLength;
    public static MediaPlayerManager mediaPlayerManager;
    List<AnimationDrawable> animationDrawables;

    public static MediaPlayerManager getInstance() {
        if (mediaPlayerManager == null) {
            mediaPlayerManager = new MediaPlayerManager();
        }
        return mediaPlayerManager;
    }

    public MediaPlayer createMediaPlayer(final Context context, final IUpdateSoundLength iUpdateSoundLength, final String soundUrl, final int position) {

        this.context = context;
        this.iUpdateSoundLength = iUpdateSoundLength;
//        if (mediaPlayer == null) {
        MediaPlayer mediaPlayer = new MediaPlayer();
//        }
        if (!TextUtils.isEmpty(soundUrl)) {
            try {
                mediaPlayer.setDataSource(soundUrl);
                mediaPlayer.prepareAsync();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                        Toast.makeText(context, "无效的声音格式", Toast.LENGTH_SHORT).show();
//                    Toast.makeText(context, "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK"
//                            + extra, Toast.LENGTH_SHORT).show();
                        break;
                    case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                        Toast.makeText(context, "无效的声音格式", Toast.LENGTH_SHORT).show();
//                    Toast.makeText(context, "MEDIA_ERROR_SERVER_DIED"
//                            + extra, Toast.LENGTH_SHORT).show();
                        break;
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        Toast.makeText(context, "无效的声音格式", Toast.LENGTH_SHORT).show();
                        break;
                    case -38:
                        Toast.makeText(context, "正在缓冲声音，请稍后", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (iUpdateSoundLength != null) {
                    iUpdateSoundLength.updateSoundLength(position, mp, getMusicTime(mp));
                }
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //播放结束后停止动画并初始化图片的位置
                stopAnimation(position);
            }

        });
        return mediaPlayer;
    }

    public MediaPlayer createMediaPlayer(Context context, IUpdateSoundLength iUpdateSoundLength, String soundUrl) {

        return createMediaPlayer(context, iUpdateSoundLength, soundUrl, 0);
    }


    //暂停动画
    private void stopAnimation(int position) {
        if (animationDrawables != null && position < animationDrawables.size() && animationDrawables.get(position) != null && animationDrawables.get(position).isRunning()) {
//                animationDrawables.get(position).invalidateSelf();
            //第一帧
            animationDrawables.get(position).selectDrawable(0);
            animationDrawables.get(position).stop();
        }
    }

    //得到声音长度
    private String getMusicTime(MediaPlayer mediaPlayer) {
        int musicTime = mediaPlayer.getDuration() / 1000;
        int minute = musicTime / 60;
        return minute == 0 ? musicTime % 60 + "''" : minute + "'" + musicTime % 60 + "''";
    }

    public void prepare(MediaPlayer mediaPlayer, String soundUrl) {
        if (mediaPlayer == null) {
            Toast.makeText(context, "无效播放", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            mediaPlayer.setDataSource(soundUrl);
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //停止播放
    public void stopSound(MediaPlayer mediaPlayer) {
        stopSound(mediaPlayer, 0);

    }

    //停止播放
    public void stopSound(MediaPlayer mediaPlayer, int position) {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                stopAnimation(position);
                mediaPlayer.pause();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //暂停播放所有的正在播放的资源
    public void pauseAllSounds(List<MediaPlayer> mediaPlayerList) {
        for (int i = 0; i < mediaPlayerList.size(); i++) {
            pauseSound(mediaPlayerList.get(i), i);
        }
    }

    //暂停播放
    public void pauseSound(MediaPlayer mediaPlayer, int position) {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(0);
                mediaPlayer.pause();
                stopAnimation(position);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //停止所有播放，点击可以从头重新播放
    public void stopAllSound(List<MediaPlayer> mediaPlayerList) {
        for (int i = 0; i < mediaPlayerList.size(); i++) {
            pauseSound(mediaPlayerList.get(i), i);
        }
    }

    //当有多个声音播放的时候，互斥播放
    public void restartPlaySound(Context context, List<MediaPlayer> mediaPlayerList, MediaPlayer mediaPlayer, int position) {
        for (int i = 0; i < mediaPlayerList.size(); i++) {
            if (mediaPlayer != mediaPlayerList.get(i)) {
                //先暂停其他的声音播放
                pauseSound(mediaPlayerList.get(i), i);
            }
        }
        playSound(context, mediaPlayer, position);
    }

    //开始播放
    public void playSound(Context context, MediaPlayer mediaPlayer) {
        playSound(context, mediaPlayer, 0);
    }

    //开始播放
    public void playSound(Context context, MediaPlayer mediaPlayer, int position) {
        if (mediaPlayer == null) {
            Toast.makeText(context, "无效播放", Toast.LENGTH_SHORT).show();
            return;
        }
        AnimationDrawable anim = null;
        if (animationDrawables != null && position < animationDrawables.size()) {
            anim = animationDrawables.get(position);
        }

        if (mediaPlayer.isPlaying()) {
            //正在播放声音，再点点击回到起始位置，即重新播放
            pauseSound(mediaPlayer, position);
        } else {
            try {
                mediaPlayer.start();
                Log.d("Logger", "anim=" + anim);
                if (anim != null) {
                    anim.start();
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void setSoundWaveAnimationList(List<AnimationDrawable> animationDrawables) {
        this.animationDrawables = animationDrawables;
    }

    public void setSoundWaveAnimation(boolean needClear, AnimationDrawable animationDrawable) {
        if (animationDrawables == null) {
            animationDrawables = new ArrayList<>();
        }
        if (needClear) {
            animationDrawables.clear();
        }
        animationDrawables.add(animationDrawable);
    }

    public void setSoundWaveAnimation(AnimationDrawable animationDrawable) {
        setSoundWaveAnimation(false, animationDrawable);
//        if (animationDrawables == null) {
//            animationDrawables = new ArrayList<>();
//        }
//        animationDrawables.add(animationDrawable);
    }

    //    释放单个
    public void releaseMediaPlayer(MediaPlayer mediaPlayer) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    释放多个
    public void releaseMediaPlayers(List<MediaPlayer> mediaPlayerList) {
        for (MediaPlayer mediaPlayer : mediaPlayerList) {
            releaseMediaPlayer(mediaPlayer);
        }
        if (animationDrawables != null) {
            animationDrawables.clear();
        }
    }

    //释放所有资源，包含mediaPlayer和animationDrawables
    public void releaseAllResource(MediaPlayer mediaPlayer) {
        releaseMediaPlayer(mediaPlayer);
        if (animationDrawables != null) {
            animationDrawables.clear();
        }

    }
}
