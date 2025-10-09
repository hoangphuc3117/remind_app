package ppapps.phapamnhacnho.modules.mediaplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ppapps.phapamnhacnho.R;
import ppapps.phapamnhacnho.basemodules.util.AppUtil;

/**
 * Created by PhucHN on 4/9/2017
 */

public class MyPlayer {
    public static final int PLAY_FILE = 100;

    public static final int PLAY_FOLDER = 101;

    private MediaPlayer mMediaPlayer;

    private int mPlayType = PLAY_FILE;

    private String mPlayFile;

    private Context mContext;

    private List<File> mListFileMp3;

    private int mFileIndex;

    private int mPlayingPosition;

    private int mNewFileIndex;

    public MyPlayer(Context context) {
        mContext = context;
    }

    private void setupPlayFile(String fileName, int playingPosition) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        mPlayingPosition = playingPosition;
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setLooping(true);
        playSong(fileName, mPlayingPosition);
    }

    private void setupPlayFolder(String fileFolder, int fileIndex, int playingPosition) {
        File file = new File(fileFolder);
        if (file.isDirectory()) {
            mListFileMp3 = AppUtil.removeNotMp3FileInListFile(file.listFiles());
            mFileIndex = fileIndex;
            mNewFileIndex = fileIndex;
            mPlayingPosition = playingPosition;

            if (mListFileMp3.size() == 0) {
                mMediaPlayer = MediaPlayer.create(mContext, R.raw.gamestartup);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.start();
            } else {
                if (mMediaPlayer == null) {
                    mMediaPlayer = new MediaPlayer();
                }
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                playSong(mListFileMp3.get(mFileIndex).getPath(), mPlayingPosition);
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mNewFileIndex++;
                        if (mNewFileIndex >= mListFileMp3.size()) {
                            mNewFileIndex = 0;
                        }
                        playSong(mListFileMp3.get(mNewFileIndex).getPath(), mPlayingPosition);
                    }
                });
            }
        }
    }

    public int getNewFileIndex() {
        return mNewFileIndex;
    }

    public int getFileIndex() {
        return mFileIndex;
    }

    public int getPlayingPosition() {
        return mPlayingPosition;
    }

    public void updatePlayingPosition() {
        if (mMediaPlayer != null) {
            mPlayingPosition = mMediaPlayer.getCurrentPosition();
        }
    }

    public void initialMediaPlayer(int currentSong, int playingPosition) {
        if (mPlayFile == null)
            return;

        if (mPlayType == PLAY_FILE) {
            setupPlayFile(mPlayFile, playingPosition);
        } else {
            setupPlayFolder(mPlayFile, currentSong, playingPosition);
        }
    }

    public void resetPlayer() {
        if (mPlayFile == null)
            return;

        if (mPlayType == PLAY_FILE) {
            setupPlayFile(mPlayFile, mPlayingPosition);
        } else {
            setupPlayFolder(mPlayFile, mFileIndex, mPlayingPosition);
        }
    }

    public String getFileName() {
        if (mListFileMp3 != null && mFileIndex < mListFileMp3.size())
            return mListFileMp3.get(mFileIndex).getPath();
        else
            return "";
    }

    private void playSong(String filePath, int currentPosition) {
        mMediaPlayer.reset();
        try {
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            mMediaPlayer.seekTo(currentPosition);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // dừng phát nhạc
    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    public void setPlayType(int playType) {
        mPlayType = playType;
    }

    public void release(){
        if(mMediaPlayer != null){
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void setPlayFile(String playFile) {
        mPlayFile = playFile;
    }
}
