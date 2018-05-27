package vkclient.vkclient.util;

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

public class AudioRecorder {

    private MediaRecorder mediaRecorder;
    private String filePath;

    public AudioRecorder(File file) {
        mediaRecorder = new MediaRecorder();
        filePath = file.getAbsolutePath();
    }

    public void startRecording() throws Exception {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(filePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            throw new Exception(e);
        }
        mediaRecorder.start();
    }

    public void stopRecording() throws Exception {
        try {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        } catch (java.lang.Exception e) {
            throw new Exception(e);
        }

    }

    public static class Exception extends java.lang.Exception {
        Exception(java.lang.Exception e) {
            super(e);
        }
    }

}
