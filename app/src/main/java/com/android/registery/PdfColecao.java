package com.android.registery;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by miza on 15/11/16.
 */

public class PdfColecao {
    private String titulo;
    private SQLiteDatabase bd;
    private String dir;

    public PdfColecao(SQLiteDatabase bd, String titulo, String dir) {
        this.titulo = titulo;
        this.bd = bd;
        this.dir = dir;
    }

    public Document gerarDocumento(String autor) {
        Document doc = new Document();
        Cursor cursor = bd.rawQuery("select * from imagem where titulo = '" + titulo + "'", null);
        cursor.moveToFirst();
        try {
            //configurando a pagina
            PdfWriter.getInstance(doc, new FileOutputStream(dir + "/" + titulo + ".pdf"));
            doc.setMargins(80, 80, 40, 40);
            doc.open();
            doc.setPageSize(PageSize.A4);
            doc.addTitle(titulo);
            doc.addAuthor(autor);
            doc.addCreationDate();

            // configurando a capa
            doc.add(new Paragraph(" "));
            Paragraph topo = new Paragraph("[Registery]", new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.BLACK));
            topo.setAlignment(Element.ALIGN_CENTER);
            doc.add(topo);
            for (int i = 0; i < 16; i++) doc.add(new Paragraph(" "));

            Paragraph tit = new Paragraph(titulo, new Font(Font.FontFamily.HELVETICA, 40, Font.BOLD, BaseColor.BLACK));
            tit.setAlignment(Element.ALIGN_CENTER);
            doc.add(tit);
            for (int i = 0; i < 16; i++) doc.add(new Paragraph(" "));

            Paragraph base = new Paragraph(autor, new Font(Font.FontFamily.HELVETICA, 16, Font.NORMAL, BaseColor.BLACK));
            base.setAlignment(Element.ALIGN_CENTER);
            doc.add(base);

            String data = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            Paragraph dataP = new Paragraph(data, new Font(Font.FontFamily.HELVETICA, 16, Font.NORMAL, BaseColor.BLACK));
            dataP.setAlignment(Element.ALIGN_CENTER);
            doc.add(dataP);
            doc.newPage();

            // inserindo os registros
            for (int i = 1; i <= cursor.getCount(); i++) {
                // titulo do registro
                String registro = cursor.getString(cursor.getColumnIndex("path"));
                registro = registro.substring(registro.lastIndexOf("/") + 1, registro.length());
                System.out.println(registro);
                doc.add(new Paragraph(i + ". " + registro, new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.BLACK)));
                doc.add(new Paragraph(" "));

                // imagem
                String path = cursor.getString(cursor.getColumnIndex("path"));
                File file = new File(path.substring(0, path.lastIndexOf("."))+"med.png");
                Image img;
                if (file.exists())
                    img = Image.getInstance(path.substring(0, path.lastIndexOf("."))+"med.png");
                else  img = Image.getInstance(path);
                float cte = img.getWidth() / img.getHeight();
                if (img.getHeight() < img.getWidth()) {
                    img.setRotationDegrees(90);
                    img.scaleAbsolute(480, (float)480/cte);
                }
                else img.scaleAbsolute((float)480*cte, 480);
                doc.add(img);

                // medidas
                String med = cursor.getString(cursor.getColumnIndex("medida"));
                if (med != null) {
                    ArrayList <String> medidas = new ArrayList<>();
                    medidas.add(med.substring(0, med.indexOf(",")));
                    int index = med.indexOf(",") + 1;

                    for (int j = 0; j < med.length(); j = index + 1) {
                        for (int k = 0; k < 4; k++) {
                            med.substring(index, med.indexOf(",", index));
                            index = med.indexOf(",", index) + 1;
                        }
                        medidas.add(med.substring(index, med.indexOf("/", index)));
                        index = med.indexOf("/", index) + 1;
                    }
                    doc.add(new Paragraph("Unidade: " + medidas.get(0)));
                    PdfPTable tabela = new PdfPTable(5);
                    tabela.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
                    tabela.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                    tabela.setSpacingBefore(5);
                    tabela.addCell(new Paragraph(medidas.get(1), new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.RED)));
                    if (medidas.size() == 2) medidas.add(" ");
                    tabela.addCell(new Paragraph(medidas.get(2), new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.GREEN)));
                    if (medidas.size() == 3) medidas.add(" ");
                    tabela.addCell(new Paragraph(medidas.get(3), new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLUE)));
                    if (medidas.size() == 4) medidas.add(" ");
                    tabela.addCell(new Paragraph(medidas.get(4), new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.YELLOW)));
                    if (medidas.size() == 5) medidas.add(" ");
                    tabela.addCell(new Paragraph(medidas.get(5), new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.MAGENTA)));
                    doc.add(tabela);
                }
                // outras informações da imagem
                doc.add(new Paragraph(" "));
                doc.add(new Paragraph(cursor.getString(cursor.getColumnIndex("data"))));
                doc.add(new Paragraph(cursor.getString(cursor.getColumnIndex("local"))));
                doc.add(new Paragraph(" "));
                doc.add(new Paragraph(cursor.getString(cursor.getColumnIndex("obs"))));
                doc.add(new Paragraph(" "));
                cursor.moveToNext();
                doc.newPage();
            }
        } catch (DocumentException | FileNotFoundException | MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        doc.close();
        return doc;
    }

    public Intent abrirPdf(Context c) {
        File file = new File(dir + "/" + titulo + ".pdf");
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setDataAndType(Uri.fromFile(file), "application/pdf");
        if (c.getPackageManager().queryIntentActivities(target, 0).size() > 0)
            return target;
        target = new Intent(Intent.ACTION_SEND);
        target.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        return target;

        /*Intent email = new Intent(Intent.ACTION_SEND);
        File file = new File(dir, titulo+".pdf");
        Uri path = Uri.fromFile(file);
        email.setType("vnd.android.cursor.dir/email");
        email.putExtra(Intent.EXTRA_EMAIL, new String [] {"israelop1@hotmail.com"});
        email.putExtra(Intent.EXTRA_SUBJECT, "Registery: "+titulo);
        email.putExtra(Intent.EXTRA_STREAM, path);*/
    }

}
