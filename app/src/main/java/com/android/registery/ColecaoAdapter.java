package com.android.registery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ColecaoAdapter extends BaseAdapter {

    boolean hoje = false, ontem = false, mesAtual = false, anoAtual = false;
    private Context context;
    private ArrayList<Colecao> lista;

    public ColecaoAdapter(Context context, ArrayList<Colecao> lista) {
        this.context = context;
        this.lista = lista;
    }

    @Override
    public int getCount() {
        return lista.size();
    }

    @Override
    public Object getItem(int position) {
        return lista.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Colecao colecao = lista.get(position);

        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflate.inflate(R.layout.item, null);

        Calendar car = Calendar.getInstance();
        car.add(Calendar.DATE, -1);
        if (colecao.getHora().contains(new SimpleDateFormat("dd/MM/yyyy").format(new Date())) && !hoje) {
            TextView time = new TextView(context);
            time.setText("Hoje");
            hoje = true;
        }
        if (colecao.getHora().contains(new SimpleDateFormat("dd/MM/yyyy").format(car.getTime())) && !ontem) {
            //timeline.setText("Ontem");
            ontem = true;
        }
        if (colecao.getHora().contains(new SimpleDateFormat("/MM/yyyy").format(new Date())) && !mesAtual) {
            //timeline.setText("Este mês");
            mesAtual = true;
        }
        if (colecao.getHora().contains(new SimpleDateFormat("yyyy").format(new Date())) && !anoAtual) {
            //timeline.setText("Este ano");
            anoAtual = true;
        }

        TextView titulo = (TextView) layout.findViewById(R.id.t1);
        titulo.setText(colecao.getTitulo());

        //TextView data = (TextView) layout.findViewById(R.id.t2);
        //data.setText(colecao.getHora().substring(0, 10));

        TextView local = (TextView) layout.findViewById(R.id.t3);
        local.setText(colecao.getLocal());

        ImageView iv = (ImageView) layout.findViewById(R.id.iv);
        if (colecao.getImagemPath() != null) {
            String path = colecao.getImagemPath();
            path = path.substring(0, path.lastIndexOf(".")) + "tmb.jpg";
            Bitmap imagem = BitmapFactory.decodeFile(path);
            iv.setImageBitmap(imagem);
        }
        return layout;
    }
}
