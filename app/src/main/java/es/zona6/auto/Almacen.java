package es.zona6.auto;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/** Guarda la lista de centros en el móvil; la usan la pantalla del móvil y la del coche. */
public final class Almacen {
    private static final String PREFS = "zona6";
    private static final String CLAVE = "centros";

    private Almacen() { }

    public static List<Centro> porDefecto() {
        List<Centro> l = new ArrayList<>();
        l.add(new Centro("Sevilla A01", "Sevilla"));
        l.add(new Centro("Málaga", "Málaga"));
        l.add(new Centro("Cádiz", "Cádiz"));
        l.add(new Centro("Algeciras", "Algeciras"));
        l.add(new Centro("Huelva", "Huelva"));
        l.add(new Centro("Córdoba", "Córdoba"));
        l.add(new Centro("Granada", "Granada"));
        l.add(new Centro("Almería", "Almería"));
        l.add(new Centro("Jaén A71", "Jaén"));
        return l;
    }

    public static List<Centro> cargar(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String json = p.getString(CLAVE, null);
        if (json == null) return porDefecto();
        List<Centro> l = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(json);
            for (int i = 0; i < a.length(); i++) l.add(Centro.fromJson(a.getJSONObject(i)));
        } catch (JSONException e) {
            return porDefecto();
        }
        return l;
    }

    public static void guardar(Context ctx, List<Centro> centros) {
        JSONArray a = new JSONArray();
        try {
            for (Centro c : centros) a.put(c.toJson());
        } catch (JSONException e) {
            return;
        }
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(CLAVE, a.toString()).apply();
    }

    /**
     * Lee un CSV con cabecera: nombre;direccion;responsable;telefono;estado;nota
     * Admite punto y coma o coma como separador.
     */
    public static List<Centro> desdeCsv(String texto) {
        List<Centro> l = new ArrayList<>();
        String[] lineas = texto.replace("\r", "").split("\n");
        if (lineas.length == 0) return l;
        String sep = lineas[0].contains(";") ? ";" : ",";
        for (int i = 1; i < lineas.length; i++) {
            String linea = lineas[i].trim();
            if (linea.isEmpty()) continue;
            String[] c = linea.split(sep, -1);
            Centro x = new Centro();
            x.nombre = campo(c, 0);
            if (x.nombre.isEmpty()) continue;
            x.direccion = campo(c, 1);
            x.responsable = campo(c, 2);
            x.telefono = campo(c, 3);
            x.estado = Centro.normalizarEstado(campo(c, 4));
            x.nota = campo(c, 5);
            l.add(x);
        }
        return l;
    }

    private static String campo(String[] c, int i) {
        if (i >= c.length) return "";
        return c[i].trim().replace("\"", "");
    }
}
