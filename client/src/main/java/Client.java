import Demo.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        java.util.List<String> extraArgs = new java.util.ArrayList<>();

        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.client", extraArgs)) {
            Response response = null;
            Demo.PrinterPrx service = Demo.PrinterPrx.checkedCast(
                    communicator.propertyToProxy("Printer.Proxy")).ice_twoway().ice_secure(false);

            if (service == null) {
                throw new Error("Invalid proxy");
            }

            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Callback.Client");
            adapter.add(new CallbackImp(), com.zeroc.Ice.Util.stringToIdentity("callbackReceiver"));
            adapter.activate();

            Demo.CallbackPrx callback =  Demo.CallbackPrx.uncheckedCast(adapter.createProxy(
                com.zeroc.Ice.Util.stringToIdentity("callbackReceiver")));

            String hostname = System.getProperty("user.name") + "/" + java.net.InetAddress.getLocalHost().getHostName() + ": ";

            service.register(hostname, callback);

            try {
                File inputFile = new File("input.txt");

                Scanner fileScanner = new Scanner(inputFile);
                long startTime = System.currentTimeMillis(); // Tiempo de inicio de la solicitud
                while (fileScanner.hasNextLine()) {
                    String message = fileScanner.nextLine();
                    if (message.startsWith("to ")) {
                        String[] parts = message.split("/");
                        service.sendMessage(parts[1], hostname+": "+parts[2], callback);
                    } else if (message.startsWith("BC")) {
                        service.broadcastMessage(message, callback);
                    } else if (message.startsWith("register")) {
                        service.register(hostname, callback);
                    } else if (message.startsWith("list clients")) {
                        service.listClients(callback);; // Listar clientes registrados
                    }else{
                        response = service.printString(hostname + message);
                        System.out.println("El servidor responde: " + response.value);
                    }
                    
                    fileScanner.close();
                    in.close();
                    
                    long endTime = System.currentTimeMillis(); // Tiempo de finalización de la solicitud
                    System.out.println("Tiempo de procesamiento: " + (endTime - startTime) + " milisegundos");
                }

            } catch (FileNotFoundException e) {
                // e.printStackTrace();

                String message = "";

                while(!message.equals("exit")) {
                    System.out.println("\nEscriba el mensaje que quiere enviar al server:");

                    message = in.nextLine();

                    if (message.startsWith("to ")) {
                        String[] parts = message.split(" ");
                        service.sendMessage(parts[1], hostname+": "+parts[2], callback);
                    } else if (message.startsWith("BC")) {
                        service.broadcastMessage(message, callback);
                    } else if (message.startsWith("register")) {
                        service.register(hostname, callback);
                    } else if (message.startsWith("list clients")) {
                        service.listClients(callback); // Listar clientes registrados
                    }else{
                        response = service.printString(hostname + message);
                        System.out.println("El servidor responde: " + response.value);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        
    }

    
}