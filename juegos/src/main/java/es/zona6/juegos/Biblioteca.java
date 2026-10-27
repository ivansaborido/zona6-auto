package es.zona6.juegos;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Lista de juegos guardada en el móvil: archivos HTML importados o enlaces web. */
public final class Biblioteca {
    private static final String PREFS = "juegos";
    private static final String CLAVE = "lista";

    public static class Juego {
        public String nombre;
        /** "archivo" o "enlace" */
        public String tipo;
        /** Ruta del archivo copiado o URL */
        public String destino;

        public String url() {
            return "archivo".equals(tipo) ? "file://" + destino : destino;
        }
    }

    private Biblioteca() { }

    public static File carpeta(Context ctx) {
        File d = new File(ctx.getFilesDir(), "juegos");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    public static List<Juego> cargar(Context ctx) {
        List<Juego> l = new ArrayList<>();
        String json = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(CLAVE, "[]");
        try {
            JSONArray a = new JSONArray(json);
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                Juego j = new Juego();
                j.nombre = o.optString("nombre");
                j.tipo = o.optString("tipo");
                j.destino = o.optString("destino");
                l.add(j);
            }
        } catch (Exception ignored) { }
        return l;
    }

    public static void guardar(Context ctx, List<Juego> juegos) {
        JSONArray a = new JSONArray();
        try {
            for (Juego j : juegos) {
                JSONObject o = new JSONObject();
                o.put("nombre", j.nombre);
                o.put("tipo", j.tipo);
                o.put("destino", j.destino);
                a.put(o);
            }
        } catch (Exception ignored) { }
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(CLAVE, a.toString()).apply();
    }
}
