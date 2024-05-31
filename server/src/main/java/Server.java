import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import Demo.CallbackPrx;

public class Server {

    private static ExecutorService executor = Executors.newCachedThreadPool();
    private static AtomicInteger activeThreads = new AtomicInteger(0);
    private static Map<String, CallbackPrx> registeredClients = new HashMap<>();
    public static void main(String[] args) {
        java.util.List<String> extraArgs = new java.util.ArrayList<String>();

        

        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.server", extraArgs)) {
            if (!extraArgs.isEmpty()) {
                System.err.println("too many arguments");
                for (String v : extraArgs) {
                    System.out.println(v);
                }
            }
            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Printer");
            com.zeroc.Ice.Object object = new PrinterI();
            adapter.add(object, com.zeroc.Ice.Util.stringToIdentity("SimplePrinter"));
            adapter.activate();

            // Crear un grupo de subprocesos para manejar las solicitudes de los clientes
            ExecutorService threadPool = Executors.newFixedThreadPool(10); // Puedes ajustar el tamaño del pool según tus necesidades

            System.out.println("Servidor listo para recibir solicitudes...");

            communicator.waitForShutdown();

            // Apagar el grupo de subprocesos cuando el servidor se cierre
            threadPool.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ExecutorService getExecutor(){
        return executor;
    }
    public static AtomicInteger getActiveThreads(){
        return activeThreads;
    }
    public static Map<String, CallbackPrx> getRegisteredClients() {
        return registeredClients;
    }
}
