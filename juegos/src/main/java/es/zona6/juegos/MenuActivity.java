package es.zona6.juegos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/** Menú de juegos. Se ve igual en el móvil y en la pantalla del coche. */
public class MenuActivity extends Activity {
    private static final int PEDIR_HTML = 11;
    private static final String[] COLORES = {"#5B2A86", "#0F4C81", "#1E8E3E", "#D93025", "#F2A900", "#00838F"};

    private List<Biblioteca.Juego> juegos;
    private GridLayout rejilla;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        juegos = Biblioteca.cargar(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#14101C"));
        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        int p = dp(20);
        raiz.setPadding(p, p, p, p);
        scroll.addView(raiz);

        TextView titulo = new TextView(this);
        titulo.setText("Mis Juegos");
        titulo.setTextSize(28);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        titulo.setTextColor(Color.WHITE);
        raiz.addView(titulo);

        TextView sub = new TextView(this);
        sub.setText("Toca un juego para jugar. Mantén pulsado para renombrarlo o borrarlo.");
        sub.setTextColor(Color.parseColor("#B9B3C7"));
        sub.setPadding(0, dp(4), 0, dp(16));
        raiz.addView(sub);

        rejilla = new GridLayout(this);
        raiz.addView(rejilla);

        LinearLayout botones = new LinearLayout(this);
        botones.setOrientation(LinearLayout.HORIZONTAL);
        botones.setPadding(0, dp(16), 0, 0);
        botones.addView(boton("+ Importar juego HTML", v -> elegirHtml()));
        botones.addView(boton("+ Añadir enlace web", v -> pedirEnlace()));
        raiz.addView(botones);

        TextView ayuda = new TextView(this);
        ayuda.setTextColor(Color.parseColor("#B9B3C7"));
        ayuda.setPadding(0, dp(20), 0, 0);
        ayuda.setText("EN EL COCHE: los juegos solo se abren con el coche parado. "
                + "Al arrancar, Android Auto los cierra solo.\n\n"
                + "Importa aquí los archivos .html de tus juegos (por ejemplo, los que tienes en WhatsApp o en Descargas). "
                + "Si un juego está online, añade su enlace.");
        raiz.addView(ayuda);

        setContentView(scroll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pintar();
    }

    private void pintar() {
        rejilla.removeAllViews();
        int ancho = getResources().getDisplayMetrics().widthPixels - dp(40);
        int columnas = Math.max(2, ancho / dp(170));
        rejilla.setColumnCount(columnas);
        int lado = ancho / columnas - dp(12);

        if (juegos.isEmpty()) {
            TextView vacio = new TextView(this);
            vacio.setText("Aún no hay juegos. Pulsa \"Importar juego HTML\".");
            vacio.setTextColor(Color.WHITE);
            vacio.setTextSize(16);
            rejilla.addView(vacio);
            return;
        }

        for (int i = 0; i < juegos.size(); i++) {
            final Biblioteca.Juego j = juegos.get(i);
            TextView t = new TextView(this);
            t.setText(j.nombre);
            t.setTextColor(Color.WHITE);
            t.setTextSize(17);
            t.setTypeface(Typeface.DEFAULT_BOLD);
            t.setGravity(Gravity.CENTER);
            t.setPadding(dp(10), dp(10), dp(10), dp(10));
            GradientDrawable fondo = new GradientDrawable();
            fondo.setColor(Color.parseColor(COLORES[i % COLORES.length]));
            fondo.setCornerRadius(dp(16));
            t.setBackground(fondo);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = lado;
            lp.height = Math.round(lado * 0.7f);
            lp.setMargins(dp(6), dp(6), dp(6), dp(6));
            t.setLayoutParams(lp);
            t.setOnClickListener(v -> jugar(j));
            t.setOnLongClickListener(v -> {
                opciones(j);
                return true;
            });
            rejilla.addView(t);
        }
    }

    private void jugar(Biblioteca.Juego j) {
        if ("archivo".equals(j.tipo) && !new File(j.destino).exists()) {
            Toast.makeText(this, "No encuentro el archivo. Vuelve a importarlo.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent i = new Intent(this, JuegoActivity.class);
        i.putExtra("url", j.url());
        startActivity(i);
    }

    private void opciones(final Biblioteca.Juego j) {
        new AlertDialog.Builder(this)
                .setTitle(j.nombre)
                .setItems(new String[]{"Renombrar", "Borrar"}, (d, w) -> {
                    if (w == 0) renombrar(j);
                    else {
                        if ("archivo".equals(j.tipo)) new File(j.destino).delete();
                        juegos.remove(j);
                        Biblioteca.guardar(this, juegos);
                        pintar();
                    }
                })
                .show();
    }

    private void renombrar(final Biblioteca.Juego j) {
        final EditText e = new EditText(this);
        e.setText(j.nombre);
        new AlertDialog.Builder(this)
                .setTitle("Nombre del juego")
                .setView(e)
                .setPositiveButton("Guardar", (d, w) -> {
                    String n = e.getText().toString().trim();
                    if (!n.isEmpty()) {
                        j.nombre = n;
                        Biblioteca.guardar(this, juegos);
                        pintar();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void pedirEnlace() {
        LinearLayout f = new LinearLayout(this);
        f.setOrientation(LinearLayout.VERTICAL);
        f.setPadding(dp(20), dp(8), dp(20), 0);
        final EditText nombre = new EditText(this);
        nombre.setHint("Nombre del juego");
        final EditText url = new EditText(this);
        url.setHint("https://...");
        url.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        f.addView(nombre);
        f.addView(url);
        new AlertDialog.Builder(this)
                .setTitle("Juego online")
                .setView(f)
                .setPositiveButton("Añadir", (d, w) -> {
                    String u = url.getText().toString().trim();
                    if (u.isEmpty()) return;
                    if (!u.startsWith("http")) u = "https://" + u;
                    Biblioteca.Juego j = new Biblioteca.Juego();
                    j.nombre = nombre.getText().toString().trim().isEmpty() ? "Juego online" : nombre.getText().toString().trim();
                    j.tipo = "enlace";
                    j.destino = u;
                    juegos.add(j);
                    Biblioteca.guardar(this, juegos);
                    pintar();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void elegirHtml() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(i, PEDIR_HTML);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req != PEDIR_HTML || res != RESULT_OK || data == null) return;
        int n = 0;
        if (data.getClipData() != null) {
            for (int k = 0; k < data.getClipData().getItemCount(); k++) {
                if (importar(data.getClipData().getItemAt(k).getUri())) n++;
            }
        } else if (data.getData() != null) {
            if (importar(data.getData())) n++;
        }
        Biblioteca.guardar(this, juegos);
        pintar();
        Toast.makeText(this, n == 0 ? "No se pudo importar" : n + " juego(s) importado(s)", Toast.LENGTH_LONG).show();
    }

    private boolean importar(Uri uri) {
        try {
            String nombreArchivo = nombreDe(uri);
            String base = nombreArchivo.replaceAll("(?i)\\.html?$", "").replace('_', ' ').replace('-', ' ').trim();
            String seguro = System.currentTimeMillis() + "_" + nombreArchivo.replaceAll("[^A-Za-z0-9._-]", "_");
            if (!seguro.toLowerCase().endsWith(".html") && !seguro.toLowerCase().endsWith(".htm")) seguro += ".html";
            File destino = new File(Biblioteca.carpeta(this), seguro);
            try (InputStream in = getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(destino)) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = in.read(buf)) > 0) out.write(buf, 0, r);
            }
            Biblioteca.Juego j = new Biblioteca.Juego();
            j.nombre = base.isEmpty() ? "Juego" : base;
            j.tipo = "archivo";
            j.destino = destino.getAbsolutePath();
            juegos.add(j);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String nombreDe(Uri uri) {
        String n = "juego.html";
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) n = c.getString(idx);
            }
        } catch (Exception ignored) { }
        return n;
    }

    private Button boton(String texto, android.view.View.OnClickListener al) {
        Button b = new Button(this);
        b.setText(texto);
        b.setAllCaps(false);
        b.setOnClickListener(al);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.rightMargin = dp(8);
        b.setLayoutParams(lp);
        return b;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
