package com.android.registery;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class DrawActivity extends Activity {

    private String path, size, med = "";
    private SQLiteDatabase bd;
    private float mX, mY, fX, fY, x1, x2, y1, y2;
    private Bitmap bmp, bmp2;
    private BitmapFactory.Options opt = new BitmapFactory.Options();
    private Bancodedados itemBanco;
    private ArrayList<String> medidas;
    private Paint paint;
    private ImageView iv;
    private TextView t1, t2, t3, t4, t5, t6;
    private Canvas canvas;
    private FloatingActionButton ll;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.draw_activity);

        iv = (ImageView) findViewById(R.id.imagemView);
        bd = openOrCreateDatabase("registery", MODE_PRIVATE, null);
        path = (String) getIntent().getSerializableExtra("imagem");
        ll = (FloatingActionButton) findViewById(R.id.fab2);
        t1 = (TextView) findViewById(R.id.tcor1);
        t2 = (TextView) findViewById(R.id.tcor2);
        t3 = (TextView) findViewById(R.id.tcor3);
        t4 = (TextView) findViewById(R.id.tcor4);
        t5 = (TextView) findViewById(R.id.tcor5);
        t6 = (TextView) findViewById(R.id.tcor6);

        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(DrawActivity.this);
                dialog.setTitle("Apagar todas as medidas?");
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mensagemToast = itemBanco.apagarMedidas(bd, path);
                        Toast.makeText(getApplicationContext(), mensagemToast, Toast.LENGTH_SHORT).show();
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
        // exibir e configurar imagem para desenhar
        Display display = getWindowManager().getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);

        opt = new BitmapFactory.Options();
        opt.inMutable = true;
        // abrindo imagem (com medida ou sem medida se nao tiver)
        File file = new File(path.substring(0, path.lastIndexOf("."))+"med.png");
        if (file.exists()) {
            bmp = BitmapFactory.decodeFile(path.substring(0, path.lastIndexOf(".")) + "med.png", opt);
            bmp2 = BitmapFactory.decodeFile(path.substring(0, path.lastIndexOf(".")) + "med.png", opt);
        }
        else {
            bmp = BitmapFactory.decodeFile(path, opt);
            bmp2 = BitmapFactory.decodeFile(path, opt);
        }
        // adaptando toque ao tamanho da tela e imagem
        float cte = (float) bmp.getWidth() / (float) bmp.getHeight();
        if (bmp.getHeight() > bmp.getWidth()) {
            bmp = Bitmap.createScaledBitmap(bmp, p.x - (int) (128f * cte), p.y - 128, false);
            bmp2 = Bitmap.createScaledBitmap(bmp, p.x - (int) (128f * cte), p.y - 128, false);
        }
        else {
            bmp = Bitmap.createScaledBitmap(bmp, p.x, (int)((float)p.x/cte), false);
            bmp2 = Bitmap.createScaledBitmap(bmp, p.x, (int)((float)p.x/cte), false);
        }
        iv.setImageBitmap(bmp);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        canvas = new Canvas(bmp);

        // verificando as medidas salvas no BD
        itemBanco = new Bancodedados();
        medidas = itemBanco.verMedida(bd, path, getApplicationContext());
        for (int i = 0; i < medidas.size(); i++) {
            int index = 0;
            if (i == 0) {
                t6.setText(medidas.get(i).substring(index, medidas.get(i).indexOf(",", index)));
                index = medidas.get(i).indexOf(",", index) + 1;
            }
            mX = Float.parseFloat(medidas.get(i).substring(index, medidas.get(i).indexOf(",", index) - 1));
            index = medidas.get(i).indexOf(",", index) + 1;
            mY = Float.parseFloat(medidas.get(i).substring(index, medidas.get(i).indexOf(",", index) - 1));
            index = medidas.get(i).indexOf(",", index) + 1;
            fX = Float.parseFloat(medidas.get(i).substring(index, medidas.get(i).indexOf(",", index) - 1));
            index = medidas.get(i).indexOf(",", index) + 1;
            fY = Float.parseFloat(medidas.get(i).substring(index, medidas.get(i).indexOf(",", index) - 1));
            index = medidas.get(i).indexOf(",", index) + 1;
            size = medidas.get(i).substring(index);

            //mudando cor da linha para cada desenho
            switch (i) {
                case 0:
                    t1.setText(size);
                    paint.setColor(Color.GREEN);
                    break;
                case 1:
                    t2.setText(size);
                    paint.setColor(Color.BLUE);
                    break;
                case 2:
                    t3.setText(size);
                    paint.setColor(Color.YELLOW);
                    break;
                case 3:
                    t4.setText(size);
                    paint.setColor(Color.MAGENTA);
                    break;
                case 4:
                    t5.setText(size);
                    paint.setColor(Color.RED);
                    break;
                default: paint.setColor(Color.RED);;
            }
            //canvas.drawLine(mX, mY, fX, fY, paint);
        }
        // desenhando linhas para inserir medidas
        iv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mX = event.getX();
                    mY = event.getY();
                    x1 = mX;
                    y1 = mY;
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    x2 = event.getX();
                    y2 = event.getY();
                    canvas.drawLine(x1, y1, x2, y2, paint);
                    iv.invalidate();
                    x1 = x2;
                    y1 = y2;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    fX = event.getX();
                    fY = event.getY();
                    if (mX != fX || fY != mY) {
                        iv.setImageBitmap(bmp2);
                        canvas.setBitmap(bmp2);
                        canvas.drawLine(mX, mY, fX, fY, paint);
                        iv.invalidate();
                        if (medidas.size() < 5) {
                            // se for a primeira medida (solicitar unidade)
                            if (medidas.size() == 0) {
                                String[] unidades = {"mm", "cm", "in", "ft", "m"};
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(DrawActivity.this,
                                        android.R.layout.simple_spinner_dropdown_item, unidades);
                                final Spinner spn = new Spinner(DrawActivity.this);
                                spn.setPadding(64, 0, 64, 0);
                                spn.setAdapter(adapter);
                                AlertDialog.Builder alert = new AlertDialog.Builder(DrawActivity.this);
                                alert.setTitle("Unidade de medida");
                                alert.setView(spn);
                                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    // Salvando medida no BD
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        med = spn.getSelectedItem().toString() + ",";
                                        medidas();
                                    }
                                });
                                alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        recreate();
                                    }
                                });
                                alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        recreate();
                                    }
                                });
                                alert.create();
                                alert.show();
                            } else medidas();
                        } else {
                            Toast.makeText(getApplicationContext(), "Máximo 5 medidas.", Toast.LENGTH_SHORT).show();
                            recreate();
                        }
                    }
                }
                return true;
            }
        });
    }
    // salvando a medida
    public void medidas() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(DrawActivity.this);
        dialog.setTitle("Valor da medida");
        final EditText texto = new EditText(DrawActivity.this);
        KeyListener key = DigitsKeyListener.getInstance("0123456789.,");
        texto.setKeyListener(key);
        dialog.setView(texto);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            // Salvando medida no BD
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String medida = texto.getText().toString().replace(",", ".");
                medida = med + mX + "," + mY + "," + fX + "," + fY + "," + medida + "/";
                String mensagemToast = itemBanco.novaMedida(bd, medida, path);

                // salvando imagem com as medidas
                File file = new File(path.substring(0, path.lastIndexOf("."))+"med.png");
                try {
                    bmp2.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), mensagemToast, Toast.LENGTH_LONG).show();
                recreate();
            }
        });
        dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                recreate();
            }
        });
        dialog.create();
        dialog.show();
    }
}
