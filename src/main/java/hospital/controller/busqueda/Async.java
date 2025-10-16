package hospital.controller.busqueda;

import javafx.application.Platform;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Async {
    public static final ExecutorService EXECUTOR =
            new ThreadPoolExecutor(
                    2,//Hilos
                    4,//va a depender de la capacidad de memoria
                    60L, TimeUnit.SECONDS,//Ocidad
                    new LinkedBlockingQueue<>(),
                    r ->{
                        Thread t = new Thread(r);
                        t.setName("hilo"+t.getId());
                        t.setDaemon(true);
                        return t;
                    }
            );

    public Async() {

    }

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
}
