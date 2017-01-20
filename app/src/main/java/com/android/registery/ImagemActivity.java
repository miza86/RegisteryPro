package com.android.registery;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class ImagemActivity extends AppCompatActivity {

    private TextView tituloImagem;
    private TextView obsImagem;
    private TextView dataImagem;
    private ImageView imagemView, recycle;
    private TextView localImagem;
    private Item img;
    private SQLiteDatabase bd;
    private MediaPlayer mp;
    private String audioPath;
    private Cursor cursor;
    private Bancodedados itemBanco;
    private Bundle extra;
    private FloatingActionButton fab;
    private SeekBar seekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_imagem);

        tituloImagem = (TextView) findViewById(R.id.tvimagem);
        dataImagem = (TextView) findViewById(R.id.dataimagem);
        imagemView = (ImageView) findViewById(R.id.imagem);
        obsImagem = (TextView) findViewById(R.id.textoimagem);
        localImagem = (TextView) findViewById(R.id.localimagem);
        fab = (FloatingActionButton) findViewById(R.id.fabAudio);
        seekbar = (SeekBar) findViewById(R.id.seekbar);
        recycle = (ImageView) findViewById(R.id.recycle);

        // pegando dados do registro selecionado e criando view
        criarView();

        // Atualizando observação
        bd = openOrCreateDatabase("registery", MODE_PRIVATE, null);
        itemBanco = new Bancodedados();
        obsImagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(ImagemActivity.this);
                dialog.setTitle("Anotações");
                final EditText texto = new EditText(ImagemActivity.this);
                texto.setText(img.getObs());
                dialog.setView(texto);
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Atualizando obs no BD
                        String mensagemToast = itemBanco.inserirObservação(img.getImagemPath(), texto.getText().toString(), bd);
                        Toast.makeText(getApplicationContext(), mensagemToast, Toast.LENGTH_LONG).show();
                        if (mensagemToast.equals("Observação atualizada.")) {
                            obsImagem.setText(texto.getText().toString());
                            img.setObs(texto.getText().toString());
                            recreate();
                        }
                    }
                });
                dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialog.create();
                dialog.show();
            }
        });
        // ABRINDO AUDIO (FLOATTING BUTTON), caso haja audio associado
        if (audioPath != null) {
            recycle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ImagemActivity.this);
                    dialog.setTitle("Deseja apagar a gravação?");
                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            itemBanco.apagarAudio(bd, audioPath);
                            img.setAudio(null);
                            Toast.makeText(getApplicationContext(), "Áudio apagado com sucesso.", Toast.LENGTH_SHORT).show();
                            recreate();
                        }
                    });
                    dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    dialog.create();
                    dialog.show();
                }
            });
            mp = MediaPlayer.create(this, Uri.parse(audioPath));
            seekbar.setMax(mp.getDuration());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                seekbar.setPadding(50, 8, 200, 8);
            fab.setOnClickListener(new View.OnClickListener() {
                int j = 0;

                @Override
                public void onClick(View view) {
                    if (j == 0) { // play audio
                        fab.setImageResource(R.drawable.fabstop);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            fab.setImageIcon(Icon.createWithResource(getApplicationContext(), R.drawable.fabstop));
                        mp.start();
                        j++;

                        // implementando a barra de navegação de audio
                        final Handler mHandler = new Handler();
                        ImagemActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mp != null) {
                                    int mCurrentPosition = mp.getCurrentPosition();
                                    seekbar.setProgress(mCurrentPosition);
                                }
                                mHandler.postDelayed(this, 0);
                            }
                        });
                        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                            }

                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (mp != null && fromUser) {
                                    mp.seekTo(progress);
                                }
                            }
                        });
                        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mp.stop();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                    fab.setImageIcon(Icon.createWithResource(getApplicationContext(), R.drawable.fabback));
                                j = 0;
                                recreate();
                            }
                        });
                    } else { // parar audio
                        mp.stop();
                        j = 0;
                        recreate();
                    }
                }
            });
        } else {
            // GRAVAÇÃO DE AUDIO, caso não haja audio gravado no registro (FLOATTING BUTTON)
            seekbar.setVisibility(View.INVISIBLE);
            recycle.setVisibility(View.INVISIBLE);
            audioPath = img.getImagemPath().substring(0, img.getImagemPath().lastIndexOf(".")) + ".3gp";
            cursor = bd.rawQuery("select * from imagem where path = '" + img.getImagemPath() + "'", null);
            cursor.moveToFirst();
            fab.setImageResource(R.drawable.fabmic);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                fab.setImageIcon(Icon.createWithResource(getApplicationContext(), R.drawable.fabmic));
            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
            fab.setOnClickListener(new View.OnClickListener() {
                int i = 0;
                Audio audio = new Audio(audioPath, cursor.getInt(cursor.getColumnIndex("codigo")));

                @Override
                public void onClick(View view) {
                    if (i == 0) {
                        try { // gravando
                            audio.gravarAudio(getApplicationContext());
                            fab.setImageResource(R.drawable.fabstop);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                fab.setImageIcon(Icon.createWithResource(getApplicationContext(), R.drawable.fabstop));
                            Toast.makeText(getApplicationContext(), "Gravando áudio.", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        i++;
                    } else {
                        try { // parando gravacao
                            audio.pararGravacao(bd, cursor.getInt(cursor.getColumnIndex("codigo")));
                            img.setAudio(audioPath);
                            recreate();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        // selecionando imagem e abrindo tela de edição
        imagemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ImagemActivity.this, DrawActivity.class);
                intent.putExtra("imagem", img.getImagemPath());
                startActivity(intent);
            }
        });
    }

    // Pegando dados do item selecionado e criando view
    public void criarView() {
        extra = getIntent().getExtras();
        if (extra != null) {
            img = (Item) getIntent().getSerializableExtra("imagem");
            String tit = img.getImagemPath();
            tit = tit.substring(tit.lastIndexOf("/") + 1, tit.lastIndexOf("."));
            tituloImagem.setText(tit);
            dataImagem.setText(img.getDataImagem());
            if (img.getObs() == null)
                obsImagem.setText("Toque para inserir uma anotação.");
            else obsImagem.setText(img.getObs());

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap imagem = BitmapFactory.decodeFile(img.getImagemPath(), options);
            imagemView.setImageBitmap(imagem);
            localImagem.setText(img.getLocalImagem());
            audioPath = img.getAudio();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        seekbar.setProgress(0);
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
