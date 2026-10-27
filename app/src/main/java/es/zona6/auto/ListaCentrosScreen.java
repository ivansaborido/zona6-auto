package es.zona6.auto;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.CarColor;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;
import androidx.core.graphics.drawable.IconCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.List;

/** Pantalla principal en el coche: lista de centros con su semáforo. */
public class ListaCentrosScreen extends Screen {

    public ListaCentrosScreen(@NonNull CarContext carContext) {
        super(carContext);
        // Al volver a esta pantalla, se releen los datos por si se han cambiado en el móvil.
        getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onStart(@NonNull LifecycleOwner owner) {
                invalidate();
            }
        });
    }

    static CarIcon icono(CarContext ctx, String estado) {
        CarColor color;
        switch (estado) {
            case "rojo": color = CarColor.RED; break;
            case "ambar": color = CarColor.YELLOW; break;
            default: color = CarColor.GREEN; break;
        }
        return new CarIcon.Builder(IconCompat.createWithResource(ctx, R.drawable.ic_dot))
                .setTint(color)
                .build();
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        List<Centro> centros = Almacen.cargar(getCarContext());
        int rojos = 0, ambar = 0;
        ItemList.Builder lista = new ItemList.Builder();
        for (final Centro c : centros) {
            if ("rojo".equals(c.estado)) rojos++;
            if ("ambar".equals(c.estado)) ambar++;
            Row.Builder fila = new Row.Builder()
                    .setTitle(c.nombre)
                    .setImage(icono(getCarContext(), c.estado))
                    .setBrowsable(true)
                    .setOnClickListener(() ->
                            getScreenManager().push(new FichaCentroScreen(getCarContext(), c)));
            String linea = c.nota.isEmpty() ? c.estadoTexto() : c.estadoTexto() + " · " + c.nota;
            fila.addText(linea);
            lista.addItem(fila.build());
        }
        lista.setNoItemsMessage("Sin centros. Añádelos desde el móvil.");

        String titulo = "Zona 6";
        if (rojos > 0 || ambar > 0) titulo = "Zona 6 · " + rojos + " rojo · " + ambar + " ámbar";

        return new ListTemplate.Builder()
                .setTitle(titulo)
                .setHeaderAction(Action.APP_ICON)
                .setSingleList(lista.build())
                .build();
    }
}
