package es.zona6.auto;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.CarToast;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.Pane;
import androidx.car.app.model.PaneTemplate;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;

/** Ficha de un centro en el coche, con botones para navegar y llamar. */
public class FichaCentroScreen extends Screen {
    private final Centro centro;

    public FichaCentroScreen(@NonNull CarContext carContext, Centro centro) {
        super(carContext);
        this.centro = centro;
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        Pane.Builder panel = new Pane.Builder();

        panel.addRow(new Row.Builder()
                .setTitle("Estado: " + centro.estadoTexto())
                .setImage(ListaCentrosScreen.icono(getCarContext(), centro.estado))
                .addText(centro.nota.isEmpty() ? "Sin notas" : centro.nota)
                .build());

        if (!centro.responsable.isEmpty() || !centro.telefono.isEmpty()) {
            Row.Builder r = new Row.Builder().setTitle(
                    centro.responsable.isEmpty() ? "Responsable" : centro.responsable);
            if (!centro.telefono.isEmpty()) r.addText(centro.telefono);
            panel.addRow(r.build());
        }

        if (!centro.direccion.isEmpty()) {
            panel.addRow(new Row.Builder().setTitle("Dirección").addText(centro.direccion).build());
        }

        panel.addAction(new Action.Builder()
                .setTitle("Navegar")
                .setOnClickListener(this::navegar)
                .build());

        if (!centro.telefono.isEmpty()) {
            panel.addAction(new Action.Builder()
                    .setTitle("Llamar")
                    .setOnClickListener(this::llamar)
                    .build());
        }

        return new PaneTemplate.Builder(panel.build())
                .setTitle(centro.nombre)
                .setHeaderAction(Action.BACK)
                .build();
    }

    private void navegar() {
        String destino = centro.direccion.isEmpty() ? centro.nombre : centro.direccion;
        try {
            Intent i = new Intent(CarContext.ACTION_NAVIGATE,
                    Uri.parse("geo:0,0?q=" + Uri.encode(destino)));
            getCarContext().startCarApp(i);
        } catch (Exception e) {
            CarToast.makeText(getCarContext(), "No hay app de navegación disponible",
                    CarToast.LENGTH_LONG).show();
        }
    }

    private void llamar() {
        try {
            Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + centro.telefono));
            getCarContext().startCarApp(i);
        } catch (Exception e) {
            CarToast.makeText(getCarContext(), "No se pudo iniciar la llamada",
                    CarToast.LENGTH_LONG).show();
        }
    }
}
