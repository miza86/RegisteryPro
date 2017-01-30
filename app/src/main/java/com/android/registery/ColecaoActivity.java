package com.android.registery;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import com.facebook.FacebookSdk;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ColecaoActivity extends FragmentActivity {
    private Colecao colecao;
    private int comp = 0;
    private SQLiteDatabase bd;
    private GridView gv;
    private ArrayList<Item> lista;
    private String localTexto, nomeBD, imgPath, cidade = "Local não definido";
    private Bancodedados itemBanco;
    private Location localizacao;
    private LatLngBounds bounds;
    private GoogleMap googleMap;
    private AdapterView.AdapterContextMenuInfo info;
    private CollapsingToolbarLayout colapse;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.colecao);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        gv = (GridView) findViewById(R.id.gvcolecao);
        ViewCompat.setNestedScrollingEnabled(gv, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            gv.setNestedScrollingEnabled(true);
        registerForContextMenu(gv);
        colapse = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        // PEGANDO DADOS DO ITEM ESCOLHIDO DA ACTIVITY ANTERIOR E SETANDO TITULO DA VIEW
        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            colecao = (Colecao) getIntent().getSerializableExtra("colecao");
            colapse.setTitle(colecao.getTitulo());
        }
        // EXIBIR REGISTROS DA COLEÇÃO
        nomeBD = "registery";
        bd = openOrCreateDatabase(nomeBD, MODE_PRIVATE, null);
        itemBanco = new Bancodedados();
        lista = itemBanco.listarRegistros(bd, colecao.getTitulo());
        gv.setAdapter(new ItemAdapter(this, lista));

        //exibir mapa com os pontos
        googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView)).getMap();
        double latit = 0, longit = 0, maxLat = 0, minLat = 0, maxLng = 0, minLng = 0;
        LatLng coord = null;
        for (int i = 0; i < lista.size(); i++) {
            if (!lista.get(i).getLocalImagem().equals("Undefined")) {
                String lat = lista.get(i).getLocalImagem().substring(0, lista.get(i).getLocalImagem().lastIndexOf(" ") - 1);
                latit = Double.valueOf(lat);
                String lng = lista.get(i).getLocalImagem().substring(lista.get(i).getLocalImagem().lastIndexOf(" ") + 1,
                        lista.get(i).getLocalImagem().length());
                longit = Double.valueOf(lng);
                coord = new LatLng(latit, longit);
                googleMap.addMarker(new MarkerOptions().position(coord).icon(BitmapDescriptorFactory.defaultMarker()));

                // setando zoom do mapa com os limites das coordenadas
                if (minLat == 0) {
                    minLat = latit;
                    minLng = longit;
                    maxLat = latit;
                    maxLng = longit;
                } else {
                    if (latit < minLat) minLat = latit;
                    if (latit > maxLat) maxLat = latit;
                    if (longit < minLng) minLng = longit;
                    if (longit > maxLng) maxLng = longit;
                }
            }
        }
        if (minLat != maxLat || minLng != maxLng) {
            bounds = new LatLngBounds(new LatLng(minLat, minLng), new LatLng(maxLat, maxLng));
            googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 64));
                }
            });
        } else if (coord != null)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, 18.0f));

        // Escolhendo o item da view e passando para a proxima tela
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                Intent intent = new Intent(ColecaoActivity.this, ImagemActivity.class);
                intent.putExtra("imagem", lista.get(pos));
                startActivity(intent);
            }
        });
        // AÇÃO DO FLOATtING BUTTON PARA TIRAR NOVA FOTO
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // salvando imagem na memoria interna
                imgPath = itemBanco.gerarNomeRegistro(bd, colecao.getTitulo(),
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString());
                File imageFile = new File(imgPath);
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                startActivity(i);

                // salvando diretorio e posição gps da imagem
                for (int j = 0; localTexto == null && j < 3; j++)
                    obterLocal();
                for (int j = 0; localTexto != null && cidade.equals("Local não definido") && j < 3; j++)
                    cidade = verCidade(localizacao);
                if (localTexto == null) {
                    localTexto = "Undefined";
                    Toast.makeText(getApplicationContext(), "Localização não definida.", Toast.LENGTH_SHORT).show();
                }
                lista.add(itemBanco.salvarImagem(bd, colecao.getTitulo(), imgPath, localTexto, cidade));
            }
        });
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        recreate();
        finish();
        startActivity(getIntent());
    }
    @Override
    public void onResume() {
        super.onResume();
    }
    //Obter local quando capturar imagem
    public void obterLocal() {
        if (Build.VERSION.SDK_INT >= 23) { // verificação de versao do Android e permissões
            if (getApplicationContext().checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(ColecaoActivity.this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 12);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Erro de permissão.", Toast.LENGTH_SHORT).show();
        }
        // verificando localização
        LocationManager lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        String provedor = LocationManager.GPS_PROVIDER;
        if (LocationManager.GPS_PROVIDER == null)
            provedor = LocationManager.NETWORK_PROVIDER;
        localizacao = lManager.getLastKnownLocation(provedor);
        if (localizacao != null)
            localTexto = String.format("%.5f", localizacao.getLatitude()).replace(",", ".")
                    + ", " + String.format("%.5f", localizacao.getLongitude()).replace(",", ".");
        LocationListener lListener = new LocationListener() {
            public void onLocationChanged(Location locat) {
                localTexto = String.format("%.5f", locat.getLatitude()).replace(",", ".") +
                        ", " + String.format("%.5f", locat.getLongitude()).replace(",", ".");
                localizacao = locat;
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            public void onProviderEnabled(String provider) {
            }
            public void onProviderDisabled(String provider) {
            }
        };
        lManager.requestLocationUpdates(provedor, 500, 3, lListener);
    }

    // Ver cidade com base nas coordenadas
    public String verCidade(Location location) {
        Geocoder geocoder = new Geocoder(getApplicationContext());
        List myLocation = null;
        try {
            myLocation = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (myLocation != null && myLocation.size() > 0) {
            Address a = (Address) myLocation.get(0);
            return a.getLocality() + ", " + a.getCountryCode();
        } else return "Local não definido";
    }

    // Menu de contexto para Long clique do item selecionado
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_registro, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.deleteRegistro:
                AlertDialog.Builder dialog = new AlertDialog.Builder(ColecaoActivity.this);
                dialog.setTitle("Deseja apagar o registro?");
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        itemBanco.apagarRegistro(bd, lista.get(info.position).getImagemPath());
                        Toast.makeText(getApplicationContext(), "Registro apagado com sucesso.", Toast.LENGTH_SHORT).show();
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
            case R.id.shareRegistro:
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/*");
                Uri registro = Uri.fromFile(new File(lista.get(comp).getImagemPath()));
                share.putExtra(Intent.EXTRA_STREAM, registro);
                startActivity(Intent.createChooser(share, "Compartilhar"));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}

