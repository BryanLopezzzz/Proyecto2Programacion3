package hospital.controller.busqueda;

import javafx.application.Platform;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Async {
    public static final ExecutorService EXECUTOR =
            new ThreadPoolExecutor(
                    2,// Hilos mínimos
                    4, // Hilos máximos
                    60L, TimeUnit.SECONDS,//Ocidad
                    new LinkedBlockingQueue<>(), // Landa
                    r ->{
                        Thread t = new Thread(r);
                        t.setName("Hilo en Hospital"+t.getId());
                        t.setDaemon(true);
                        return t;
                    }
            );

    private Async() {

    }
    //  Ejecuta una tarea con resultado de forma asíncrona
    //Crear una tarea con resultado fuera del hilo principal de la interfaz grafica.
    public static <T> void Run(Supplier<T> supplier, Consumer<T> consumer, Consumer<Throwable> errorConsumer) {
        EXECUTOR.submit(() -> {
            try {
                T result = supplier.get();
                if(consumer != null) {
                    Platform.runLater(()->consumer.accept(result));
                }

            }catch (Throwable ex){
                if(errorConsumer != null){
                    Platform.runLater(()->errorConsumer.accept(ex));
                }
            }
        });
    }
    //Ejecuta una tarea sin resultado de forma asíncrona
    public static void runVoid(Runnable action, Runnable onSuccess, Consumer<Throwable> onError) {
        EXECUTOR.submit(() -> {
            try {
                action.run();
                if (onSuccess != null) {
                    Platform.runLater(onSuccess);
                }
            } catch (Throwable ex) {
                if (onError != null) {
                    Platform.runLater(() -> onError.accept(ex));
                }
            }
        });
    }
    //Esto cierra la conexión como lo menciono el profe para rendimiento
    public static void cerrarConexion() {
        EXECUTOR.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
