package com.android.registery;

//import com.facebook.FacebookSdk;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Bancodedados itemBanco;
    private SQLiteDatabase bd;
    private ArrayList<Colecao> lista = new ArrayList<>();
    private ListView lv;
    private Toolbar toolbar;
    private String nomeBD;
    private ImageView dots;
    private AdapterView.AdapterContextMenuInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Registery/");
        if (!mediaStorageDir.exists())
            mediaStorageDir.mkdirs();

        //permissões
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getApplicationContext() != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, 1);
                }
            }
        }
        lv = (ListView) findViewById(R.id.lv);
        dots = (ImageView) findViewById(R.id.buttondot);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        registerForContextMenu(lv);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Conectando ao BD
        nomeBD = "registery";
        bd = openOrCreateDatabase(nomeBD, MODE_PRIVATE, null);
        itemBanco = new Bancodedados();
        bd = itemBanco.conectarBD(bd, nomeBD);

        // Listando coleções do BD para lista de objetos e passando para a View
        lista = itemBanco.listarColecoes(bd, nomeBD);
        lv.setDivider(null);
        lv.setAdapter(new ColecaoAdapter(this, lista));

        // Selecionando coleção, passando pra proxima activity
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ColecaoActivity.class);
                intent.putExtra("colecao", lista.get(position));
                startActivity(intent);
            }
        });

        // Segurando o item da coleção
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });
        // Criando nova coleção com Floatting Button
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("Nome da Coleção");
                final EditText texto = new EditText(MainActivity.this);
                dialog.setView(texto);
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Passando a hora e o titulo digitado para criar nova coleção e salvar no BD
                        String hora = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
                        Colecao novaColecao = new Colecao(texto.getText().toString(), hora);
                        String mensagemToast = itemBanco.novaColecao(novaColecao.getTitulo(), bd, hora);
                        Toast.makeText(getApplicationContext(), mensagemToast, Toast.LENGTH_SHORT).show();
                        // Se criada com sucesso, abre nova Activity com a coleção nova
                        if (mensagemToast.equals("Coleção criada com sucesso.")) {
                            Intent intent = new Intent(MainActivity.this, ColecaoActivity.class);
                            intent.putExtra("colecao", novaColecao);
                            startActivity(intent);
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
    }
    // Recarregando coleções na tela
    @Override
    public void onResume() {
        super.onResume();
        lista.clear();
        lista = itemBanco.listarColecoes(bd, nomeBD);
        lv.setAdapter(new ColecaoAdapter(this, lista));
    }
    // Menu de opções
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    // Itens do menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("Deseja apagar todas as coleções?");
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    itemBanco.apagarTudo(bd);
                    Toast.makeText(getApplicationContext(), "Suas coleções foram apagadas.", Toast.LENGTH_SHORT).show();
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
        return true;
    }
    // Menu de contexto para Long Press da coleção selecionada
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_colecao, menu);
    }
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            // mudar nome da coleção
            case R.id.edit:
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("Nome da Coleção");
                final EditText texto = new EditText(MainActivity.this);
                dialog.setView(texto);
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mensagemToast = itemBanco.alterarTitulo(bd, lista.get(info.position).getTitulo(), texto.getText().toString());
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
                return true;
            // apagar coleçao
            case R.id.delete:
                dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("Deseja apagar '" + lista.get(info.position).getTitulo() + "'?");
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        itemBanco.apagarColecao(bd, lista.get(info.position).getTitulo());
                        Toast.makeText(getApplicationContext(), "Coleção apagada com sucesso.", Toast.LENGTH_SHORT).show();
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
                return true;
            // gerar e abrir PDF
            case R.id.pdf:
                final PdfColecao pdf = new PdfColecao(bd, lista.get(info.position).getTitulo(),
                        Environment.getExternalStorageDirectory().toString() + "/Registery");
                dialog = new AlertDialog.Builder(MainActivity.this);
                final EditText text = new EditText(MainActivity.this);
                dialog.setView(text);
                dialog.setTitle("Nome do autor");
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pdf.gerarDocumento(text.getText().toString());
                        startActivity(Intent.createChooser(pdf.abrirPdf(getApplicationContext()), "Abrir/enviar PDF"));
                    }
                });
                dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialog.create().show();
                return true;
            // compartilhar
            case R.id.share:
                ArrayList<Item> registros = itemBanco.listarRegistros(bd, lista.get(info.position).getTitulo());
                ArrayList<Uri> imagens = new ArrayList<>();
                for (int i = 0; i < registros.size(); i++) {
                    imagens.add(Uri.fromFile(new File(registros.get(i).getImagemPath())));
                }
                Intent share = new Intent(Intent.ACTION_SEND_MULTIPLE);
                share.setType("image/*");
                share.putExtra(Intent.EXTRA_STREAM, imagens);
                startActivity(Intent.createChooser(share, "Compartilhar"));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
