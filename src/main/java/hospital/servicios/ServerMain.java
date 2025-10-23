package hospital.servicios;

public class ServerMain {
    public static void main(String[] args) {
        new HospitalServer(5000).start();
    }
}