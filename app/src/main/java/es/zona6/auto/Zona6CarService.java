package es.zona6.auto;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.car.app.CarAppService;
import androidx.car.app.Screen;
import androidx.car.app.Session;
import androidx.car.app.validation.HostValidator;

/** Punto de entrada que Android Auto abre en la pantalla del coche. */
public class Zona6CarService extends CarAppService {

    @NonNull
    @Override
    public HostValidator createHostValidator() {
        // App de uso propio instalada a mano: se acepta cualquier anfitrión (Android Auto).
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR;
    }

    @NonNull
    @Override
    public Session onCreateSession() {
        return new Session() {
            @NonNull
            @Override
            public Screen onCreateScreen(@NonNull Intent intent) {
                return new ListaCentrosScreen(getCarContext());
            }
        };
    }
}
