package com.android.registery;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaRecorder;

import java.io.IOException;

public class Audio {
    private String path;
    private MediaRecorder mr;
    private int cod;

    public Audio(String path, int cod) {
        this.path = path;
        this.cod = cod;
    }

    public void gravarAudio(Context contexto) throws IOException {
        mr = new MediaRecorder();
        mr.setAudioSource(MediaRecorder.AudioSource.MIC);
        mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mr.setOutputFile(path);
        try {
            mr.prepare();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
        mr.start();
    }

    public void pararGravacao(SQLiteDatabase bd, int cod) throws IOException {
        mr.stop();
        mr.release();
        bd.execSQL("update imagem set audio = '" + path + "' where codigo = " + cod);
    }
}
