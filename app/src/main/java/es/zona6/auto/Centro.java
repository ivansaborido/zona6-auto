package es.zona6.auto;

import org.json.JSONException;
import org.json.JSONObject;

/** Un centro de Zona 6 con su semáforo y datos de contacto. */
public class Centro {
    public String nombre = "";
    public String direccion = "";
    public String responsable = "";
    public String telefono = "";
    /** verde, ambar o rojo */
    public String estado = "verde";
    public String nota = "";

    public Centro() { }

    public Centro(String nombre, String direccion) {
        this.nombre = nombre;
        this.direccion = direccion;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("nombre", nombre);
        o.put("direccion", direccion);
        o.put("responsable", responsable);
        o.put("telefono", telefono);
        o.put("estado", estado);
        o.put("nota", nota);
        return o;
    }

    public static Centro fromJson(JSONObject o) {
        Centro c = new Centro();
        c.nombre = o.optString("nombre", "");
        c.direccion = o.optString("direccion", "");
        c.responsable = o.optString("responsable", "");
        c.telefono = o.optString("telefono", "");
        c.estado = normalizarEstado(o.optString("estado", "verde"));
        c.nota = o.optString("nota", "");
        return c;
    }

    public static String normalizarEstado(String s) {
        if (s == null) return "verde";
        String t = s.trim().toLowerCase();
        if (t.startsWith("r")) return "rojo";
        if (t.startsWith("a") || t.startsWith("á")) return "ambar";
        return "verde";
    }

    public String estadoTexto() {
        switch (estado) {
            case "rojo": return "Rojo";
            case "ambar": return "Ámbar";
            default: return "Verde";
        }
    }
}
