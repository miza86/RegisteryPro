package com.android.registery;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Bancodedados extends AppCompatActivity {

    public Bancodedados() {
    }

    public SQLiteDatabase conectarBD(SQLiteDatabase bd, String nomeBD) {
        if (nomeBD.equals("registery")) {
            bd.execSQL("CREATE TABLE IF NOT EXISTS colecao (codigo integer primary key autoincrement, " +
                    "titulo text, data text, local text)");
            bd.execSQL("CREATE TABLE IF NOT EXISTS imagem (codigo integer primary key autoincrement, " +
                    "titulo text, data text, local text, obs text, path text, audio text, medida text)");
        }
        return bd;
    }

    public String novaColecao(String tituloColecao, SQLiteDatabase bd, String hora) {
        Cursor cursor = bd.rawQuery("select * from colecao where titulo = '" + tituloColecao + "'", null);
        if (cursor.getCount() == 0) {
            bd.execSQL("insert into colecao (titulo, data, local) values ('" + tituloColecao + "', '" + hora + "', 'Local não definido')");
            cursor = bd.rawQuery("select * from colecao where titulo = '" + tituloColecao + "' order by codigo", null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0)
                if (cursor.getString(cursor.getColumnIndex("data")) != null)
                    return "Coleção criada com sucesso.";
                else return "Erro de data";
            else return "Erro. Tente novamente.";
        } else return "Coleção já existe. Escolha outro nome.";
    }

    public String inserirObservação(String imgPath, String obs, SQLiteDatabase bd) {
        Cursor cursor = bd.rawQuery("select * from imagem where path = '" + imgPath + "'", null);
        if (cursor.getCount() > 0) {
            bd.execSQL("update imagem set obs = '" + obs + "' where path = '" + imgPath + "'");
            return "Observação atualizada.";
        } else return "Erro.";
    }

    // listar colecoes
    public ArrayList<Colecao> listarColecoes(SQLiteDatabase bd, String nomeBD) {
        if (nomeBD.equals("registery")) {
            // selecionando registros da tabela Coleção
            Cursor cursor = bd.rawQuery("select * from colecao order by codigo desc", null);
            cursor.moveToFirst();
            // Atribuindo registros ao objeto Coleção
            ArrayList<Colecao> lista = new ArrayList<>();
            for (int i = 0; i < cursor.getCount(); i++) {
                Colecao colecao = new Colecao(cursor.getString(cursor.getColumnIndex("titulo")),
                        cursor.getString(cursor.getColumnIndex("data")));
                String dataTeste = cursor.getString(cursor.getColumnIndex("data"));
                colecao.setHora(dataTeste);
                colecao.setLocal(cursor.getString(cursor.getColumnIndex("local")));
                // adicionando caminho da primeira imagem
                Cursor cursorImg = bd.rawQuery("select * from imagem where titulo = '" + cursor.getString(cursor.getColumnIndex("titulo")) + "'" +
                        " order by codigo", null);
                if (cursorImg.getCount() > 0) {
                    cursorImg.moveToFirst();
                    colecao.setImagemPath(cursorImg.getString(cursorImg.getColumnIndex("path")));
                }
                // Adicionando coleção a lista
                lista.add(colecao);
                cursor.moveToNext();
            }
            cursor.close();
            return lista;
        } else return null;
    }

    // listar registros da colecao
    public ArrayList<Item> listarRegistros(SQLiteDatabase bd, String titulo) {
        Cursor cursorImg = bd.rawQuery("select * from imagem where titulo = '" + titulo + "' order by codigo desc", null);
        cursorImg.moveToFirst();
        ArrayList<Item> lista = new ArrayList<>();
        for (int i = 0; i < cursorImg.getCount(); i++) {
            File bmp = new File(cursorImg.getString(cursorImg.getColumnIndex("path")));
            if (bmp.length() == 0) {
                bmp.delete();
                bmp = new File(bmp.toString().substring(0, bmp.toString().lastIndexOf(".")) + "tmb.jpg");
                bmp.delete();
                bd.execSQL("delete from imagem where path = '" + cursorImg.getString(cursorImg.getColumnIndex("path")) + "'");
            } else {
                String camImagem = cursorImg.getString(cursorImg.getColumnIndex("path"));
                String titImagem = camImagem.substring(camImagem.lastIndexOf('/') + 1, camImagem.lastIndexOf('.'));
                String local = cursorImg.getString(cursorImg.getColumnIndex("local"));
                String obsImagem = cursorImg.getString(cursorImg.getColumnIndex("obs"));
                String audio = cursorImg.getString(cursorImg.getColumnIndex("audio"));
                Item item = new Item(titImagem, camImagem, cursorImg.getString(cursorImg.getColumnIndex("data")),
                        local, obsImagem, audio);
                lista.add(item);
                cursorImg.moveToNext();
            }
        }
        cursorImg.close();
        return lista;
    }

    public Item salvarImagem(SQLiteDatabase bd, String titulo, String dir, String local, String cidade) {
        // Salvando caminho da imagem e localização no BD
        String hora = new SimpleDateFormat("dd/MM/yy  HH:mm").format(new Date());
        bd.execSQL("insert into imagem (titulo, path, data, local) values " +
                "('" + titulo + "', '" + dir + "', '" + hora + "', '" + local + "')");
        // Setando local da coleção
        Cursor cursor = bd.rawQuery("select * from colecao where titulo = '" + titulo + "'", null);
        cursor.moveToFirst();
        if (cursor.getString(cursor.getColumnIndex("local")) == null)
            bd.execSQL("update colecao set local = '" + cidade + "' where titulo ='" + titulo + "'");
        else if (cursor.getString(cursor.getColumnIndex("local")).equals("Local não definido") && !local.equals("Local não definido"))
            bd.execSQL("update colecao set local = '" + cidade + "' where titulo ='" + titulo + "'");
        Item item = new Item(titulo, dir, hora, local, null, null);
        return new Item(titulo, dir, hora, local, null, null);
    }

    // apagar coleção selecionada
    public void apagarColecao(SQLiteDatabase bd, String titulo) {
        Cursor cursor = bd.rawQuery("select * from imagem where titulo = '" + titulo + "'", null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            apagarRegistro(bd, cursor.getString(cursor.getColumnIndex("path")));
            cursor.moveToNext();
        }
        bd.execSQL("delete from colecao where titulo = '" + titulo + "'");
        bd.execSQL("delete from imagem where titulo = '" + titulo + "'");
    }

    // apagar registro selecionado da coleção
    public void apagarRegistro(SQLiteDatabase bd, String nome) {
        File fl = new File(nome);
        fl.delete();
        File fl2 = new File(nome.substring(0, nome.lastIndexOf(".")) + "tmb.jpg");
        fl2.delete();
        bd.execSQL("delete from imagem where path = '" + nome + "'");
    }

    // apagar audio de registro
    public void apagarAudio(SQLiteDatabase bd, String audio) {
        File fl = new File(audio);
        fl.delete();
        bd.execSQL("update imagem set audio = NULL where audio = '" + audio + "'");
    }

    public void apagarTudo(SQLiteDatabase bd) {
        Cursor cursor = bd.rawQuery("select * from imagem", null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            File fl = new File(cursor.getString(cursor.getColumnIndex("path")));
            fl.delete();
            File fl2 = new File(cursor.getString(cursor.getColumnIndex("path")).substring(0,
                    cursor.getString(cursor.getColumnIndex("path")).lastIndexOf(".")) + "tmb.jpg");
            fl2.delete();
            cursor.moveToNext();
        }
        bd.execSQL("drop table colecao");
        bd.execSQL("drop table imagem");
    }

    public String alterarTitulo(SQLiteDatabase bd, String tituloOld, String tituloNew) {
        Cursor cursor = bd.rawQuery("select * from colecao where titulo = '" + tituloOld + "'", null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0)
            bd.execSQL("update colecao set titulo = '" + tituloNew + "' where titulo = '" + tituloOld + "'");
        cursor = bd.rawQuery("select * from imagem where titulo = '" + tituloOld + "'", null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                String path = cursor.getString(cursor.getColumnIndex("path"));
                String pathNovo = path.replace(tituloOld, tituloNew);
                bd.execSQL("update imagem set path = '" + pathNovo + "' where path = '" + path + "'");
                cursor.moveToNext();
                // alterar nome do arquivo
                File fileOld = new File(path);
                File fileNovo = new File(pathNovo);
                fileOld.renameTo(fileNovo);
                fileOld = new File(path.substring(0, path.lastIndexOf(".")) + "tmb.jpg");
                fileNovo = new File(pathNovo.substring(0, pathNovo.lastIndexOf(".")) + "tmb.jpg");
                fileOld.renameTo(fileNovo);
            }
            bd.execSQL("update imagem set titulo = '" + tituloNew + "' where titulo = '" + tituloOld + "'");
        }
        return "Titulo alterado com sucesso.";
    }

    public String gerarNomeRegistro(SQLiteDatabase bd, String titulo, String dir) {
        Cursor cursor = bd.rawQuery("select * from imagem where titulo = '" + titulo + "'", null);
        String comp = "";
        int quant = cursor.getCount();
        if (quant < 10) comp = "00";
        else if (quant < 100) comp = "0";
        // tratamento de nome do arquivo, para nao repetir
        cursor = bd.rawQuery("select * from imagem where path = '" + dir + "/" + titulo + comp + quant + ".png'", null);
        while (cursor.getCount() > 0) {
            quant++;
            if (quant < 10) comp = "00";
            else if (quant < 100) comp = "0";
            cursor = bd.rawQuery("select * from imagem where path = '" + dir + "/" + titulo + comp + quant + ".png'", null);
        }
        return dir + "/" + titulo + comp + quant + ".png";
    }

    // Inserir medidas
    public String novaMedida(SQLiteDatabase bd, String medida, String dir) {
        Cursor cursor = bd.rawQuery("select * from imagem where path = '" + dir + "'", null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            if (cursor.getString(cursor.getColumnIndex("medida")) != null)
                medida = cursor.getString(cursor.getColumnIndex("medida")) + medida;
            bd.execSQL("update imagem set medida = '" + medida + "' where path = '" + dir + "'");
        }
        return "Medida inserida com sucesso";
    }

    // verificar medidas
    public ArrayList<String> verMedida(SQLiteDatabase bd, String dir, Context c) {
        Cursor cursor = bd.rawQuery("select * from imagem where path = '" + dir + "'", null);
        cursor.moveToFirst();
        String medida = "";
        ArrayList<String> medidas = new ArrayList<>();
        if (cursor.getCount() > 0) {
            if (cursor.getString(cursor.getColumnIndex("medida")) != null) {
                medida = cursor.getString(cursor.getColumnIndex("medida"));
                for (int i = 0; i < medida.length() - 1; i = medida.indexOf("/", i) + 1)
                    medidas.add(medida.substring(i, medida.indexOf("/", i)));
            }
        }
        return medidas;
    }

    public String apagarMedidas(SQLiteDatabase bd, String dir) {
        Cursor cursor = bd.rawQuery("select * from imagem where path = '" + dir + "'", null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            bd.execSQL("update imagem set medida = NULL where path = '" + dir + "'");
        }
        return "Medidas apagadas.";
    }
}