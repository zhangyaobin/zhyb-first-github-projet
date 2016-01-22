package csdn.zhangyaobin.mediarecorderandmediaplayer;

import android.media.MediaPlayer;

/**
 * Created by zhangyaobin on 15/11/5.
 */
public interface IUpdateSoundLength {
    //   更新声音长度
//    可以使用该接口，也可以使用EventBus
    //目前两种测试均ok
    public void updateSoundLength(int position, MediaPlayer medialPlayer, String length);

}
