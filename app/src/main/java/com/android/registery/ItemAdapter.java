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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by miza on 22/10/16.
 */

public class ItemAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Item> lista;

    public ItemAdapter(Context context, ArrayList<Item> lista) {
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
        Item item = lista.get(position);

        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflate.inflate(R.layout.imagens, null);

        TextView titulo = (TextView) layout.findViewById(R.id.tvlista);
        titulo.setText(item.getDataImagem()/*+"\n"+item.getLocalImagem()*/);

        String path = item.getImagemPath();
        path = path.substring(0, path.lastIndexOf(".")) + "tmb.jpg";
        File file = new File(path);
        if (!file.exists()) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(item.getImagemPath());
                OutputStream outStream = new FileOutputStream(path);
                float ratio = ((float) bitmap.getWidth() / (float) bitmap.getHeight());
                bitmap = Bitmap.createScaledBitmap(bitmap, (int) (480 * ratio), 480, false);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outStream);
                outStream.flush();
                outStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ImageView iv = (ImageView) layout.findViewById(R.id.ivlista);
        Bitmap imagem = BitmapFactory.decodeFile(path);
        iv.setImageBitmap(imagem);

        return layout;
    }
}
