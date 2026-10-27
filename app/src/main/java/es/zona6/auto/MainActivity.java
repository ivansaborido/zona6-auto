package es.zona6.auto;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Pantalla del móvil: editar centros, importar CSV y ver cómo activarla en el coche. */
public class MainActivity extends Activity {
    private static final int PEDIR_CSV = 7;
    private static final String AZUL = "#0F4C81";

    private List<Centro> centros;
    private LinearLayout contenedorLista;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        centros = Almacen.cargar(this);

        ScrollView scroll = new ScrollView(this);
        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        int p = dp(16);
        raiz.setPadding(p, p, p, p);
        scroll.addView(raiz);

        TextView titulo = new TextView(this);
        titulo.setText("Zona 6 Auto");
        titulo.setTextSize(24);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        titulo.setTextColor(Color.parseColor(AZUL));
        raiz.addView(titulo);

        TextView sub = new TextView(this);
        sub.setText("Lo que pongas aquí es lo que verás en la pantalla del coche. "
                + "Toca un centro para editarlo.");
        sub.setPadding(0, dp(4), 0, dp(12));
        raiz.addView(sub);

        contenedorLista = new LinearLayout(this);
        contenedorLista.setOrientation(LinearLayout.VERTICAL);
        raiz.addView(contenedorLista);
        pintarLista();

        raiz.addView(boton("+ Añadir centro", v -> editar(null)));
        raiz.addView(boton("Importar CSV", v -> elegirCsv()));
        raiz.addView(boton("Restaurar centros por defecto", v -> {
            centros = Almacen.porDefecto();
            guardarYRepintar();
        }));

        TextView ayuda = new TextView(this);
        ayuda.setPadding(0, dp(20), 0, 0);
        ayuda.setText("CÓMO VERLA EN EL COCHE (solo la primera vez)\n\n"
                + "1. Abre Ajustes del móvil > busca \"Android Auto\".\n"
                + "2. Baja hasta \"Versión\" y tócala 10 veces seguidas. Acepta el aviso.\n"
                + "3. Menú de los tres puntos > \"Ajustes para desarrolladores\".\n"
                + "4. Activa \"Fuentes desconocidas\".\n"
                + "5. En Android Auto > \"Personalizar menú\", comprueba que Zona 6 Auto está marcada.\n"
                + "6. Conecta el USB: el icono aparece en la pantalla del coche.\n\n"
                + "FORMATO DEL CSV (separado por ;)\n"
                + "nombre;direccion;responsable;telefono;estado;nota\n"
                + "Málaga;Calle Ejemplo 1, Málaga;Nombre;600000000;rojo;Saturación 92%\n\n"
                + "Estado: verde, ambar o rojo.");
        raiz.addView(ayuda);

        setContentView(scroll);
    }

    private void pintarLista() {
        contenedorLista.removeAllViews();
        for (int i = 0; i < centros.size(); i++) {
            final Centro c = centros.get(i);
            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setGravity(Gravity.CENTER_VERTICAL);
            fila.setPadding(dp(12), dp(12), dp(12), dp(12));
            GradientDrawable fondo = new GradientDrawable();
            fondo.setColor(Color.parseColor("#F2F5F9"));
            fondo.setCornerRadius(dp(10));
            fila.setBackground(fondo);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = dp(8);
            fila.setLayoutParams(lp);

            View punto = new View(this);
            GradientDrawable circulo = new GradientDrawable();
            circulo.setShape(GradientDrawable.OVAL);
            circulo.setColor(colorEstado(c.estado));
            punto.setBackground(circulo);
            LinearLayout.LayoutParams lpp = new LinearLayout.LayoutParams(dp(16), dp(16));
            lpp.rightMargin = dp(12);
            fila.addView(punto, lpp);

            TextView t = new TextView(this);
            String texto = c.nombre + (c.nota.isEmpty() ? "" : "\n" + c.nota);
            t.setText(texto);
            t.setTextSize(16);
            fila.addView(t);

            fila.setOnClickListener(v -> editar(c));
            contenedorLista.addView(fila);
        }
    }

    private void editar(final Centro existente) {
        final Centro c = existente == null ? new Centro() : existente;
        LinearLayout f = new LinearLayout(this);
        f.setOrientation(LinearLayout.VERTICAL);
        f.setPadding(dp(20), dp(8), dp(20), 0);

        final EditText nombre = campo(f, "Nombre del centro", c.nombre, InputType.TYPE_CLASS_TEXT);
        final EditText dir = campo(f, "Dirección (para el navegador)", c.direccion, InputType.TYPE_CLASS_TEXT);
        final EditText resp = campo(f, "Responsable", c.responsable, InputType.TYPE_CLASS_TEXT);
        final EditText tel = campo(f, "Teléfono", c.telefono, InputType.TYPE_CLASS_PHONE);

        final Spinner estado = new Spinner(this);
        String[] opciones = {"Verde", "Ámbar", "Rojo"};
        estado.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, opciones));
        estado.setSelection("rojo".equals(c.estado) ? 2 : "ambar".equals(c.estado) ? 1 : 0);
        TextView le = new TextView(this);
        le.setText("Semáforo");
        f.addView(le);
        f.addView(estado);

        final EditText nota = campo(f, "Nota corta (se lee en el coche)", c.nota, InputType.TYPE_CLASS_TEXT);

        AlertDialog.Builder d = new AlertDialog.Builder(this)
                .setTitle(existente == null ? "Nuevo centro" : "Editar centro")
                .setView(f)
                .setPositiveButton("Guardar", (di, w) -> {
                    c.nombre = nombre.getText().toString().trim();
                    if (c.nombre.isEmpty()) {
                        Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    c.direccion = dir.getText().toString().trim();
                    c.responsable = resp.getText().toString().trim();
                    c.telefono = tel.getText().toString().trim();
                    int s = estado.getSelectedItemPosition();
                    c.estado = s == 2 ? "rojo" : s == 1 ? "ambar" : "verde";
                    c.nota = nota.getText().toString().trim();
                    if (existente == null) centros.add(c);
                    guardarYRepintar();
                })
                .setNegativeButton("Cancelar", null);
        if (existente != null) {
            d.setNeutralButton("Borrar", (di, w) -> {
                centros.remove(existente);
                guardarYRepintar();
            });
        }
        d.show();
    }

    private void elegirCsv() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"text/csv", "text/comma-separated-values", "text/plain", "application/vnd.ms-excel"});
        startActivityForResult(i, PEDIR_CSV);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req != PEDIR_CSV || res != RESULT_OK || data == null || data.getData() == null) return;
        try {
            Uri uri = data.getData();
            StringBuilder sb = new StringBuilder();
            try (InputStream in = getContentResolver().openInputStream(uri);
                 BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String l;
                while ((l = br.readLine()) != null) sb.append(l).append('\n');
            }
            String texto = sb.toString();
            if (texto.startsWith("﻿")) texto = texto.substring(1);
            List<Centro> nuevos = Almacen.desdeCsv(texto);
            if (nuevos.isEmpty()) {
                Toast.makeText(this, "El CSV no tiene centros válidos", Toast.LENGTH_LONG).show();
                return;
            }
            centros = nuevos;
            guardarYRepintar();
            Toast.makeText(this, nuevos.size() + " centros importados", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo leer el fichero", Toast.LENGTH_LONG).show();
        }
    }

    private void guardarYRepintar() {
        Almacen.guardar(this, centros);
        pintarLista();
    }

    private EditText campo(LinearLayout padre, String etiqueta, String valor, int tipo) {
        TextView l = new TextView(this);
        l.setText(etiqueta);
        l.setPadding(0, dp(8), 0, 0);
        padre.addView(l);
        EditText e = new EditText(this);
        e.setInputType(tipo);
        e.setText(valor);
        padre.addView(e);
        return e;
    }

    private Button boton(String texto, View.OnClickListener al) {
        Button b = new Button(this);
        b.setText(texto);
        b.setAllCaps(false);
        b.setOnClickListener(al);
        return b;
    }

    private int colorEstado(String e) {
        switch (e) {
            case "rojo": return Color.parseColor("#D93025");
            case "ambar": return Color.parseColor("#F2A900");
            default: return Color.parseColor("#1E8E3E");
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
