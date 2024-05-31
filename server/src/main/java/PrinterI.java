import Demo.Response;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import com.zeroc.Ice.Current;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import Demo.CallbackPrx;

public class PrinterI implements Demo.Printer {

    private ExecutorService executor = Server.getExecutor();
    private AtomicInteger activeThreads = Server.getActiveThreads();
    private Map<String, CallbackPrx> registeredClients = Server.getRegisteredClients();

    // Método para manejar múltiples solicitudes concurrentes
    public synchronized Response printString(String s, Current current) {
        System.out.println("Nuevo hilo creado para manejar la solicitud: " + Thread.currentThread().getName());

        String response = "";

        // Evaluar el tipo de mensaje recibido
        switch (evaluateMessage(response)) {
            case 0: // Fibonacci
                response += fibonacci(Integer.parseInt(response));
                break;

            case 1: // Listar interfaces
                response = getLogicInterfaces();
                break;

            case 2: // Escanear puertos
                response = scanPorts(response.substring(10));
                break;

            case 3: // Ejecutar comando
                response = executeCmdlet(response.substring(1));
                break;

            default:
                System.out.println("El mensaje no cumple con ningún formato establecido. Intente nuevamente.");
                break;
        }
        
        System.out.println("Server response: " + response);
        System.out.println("Hilo finalizado de manejar la solicitud: " + Thread.currentThread().getName());
        return new Response(0, "Server response: " + response);
    }

    // Método para evaluar el tipo de mensaje recibido
    

    @Override
    public void listClients(CallbackPrx proxy, Current current) {
        String clientsList = "";
        for (String hostname : registeredClients.keySet()) {
            clientsList += hostname+"\n";
        }
        proxy.responseCallback(clientsList.toString());
    }

    public static String fibonacci(int n) {
        if (n < 0) {
            return "El número debe ser mayor o igual a 0";
        }

        long[] fib = new long[n + 2];
        fib[0] = 0;
        fib[1] = 1;

        for (int i = 2; i <= n; i++) {
            fib[i] = fib[i - 1] + fib[i - 2];
        }

        String fibonacciStr = "";
        for (int i = 0; i <= n; i++) {
            fibonacciStr += fib[i] + " - ";
        }

        fibonacciStr = fibonacciStr.substring(0, fibonacciStr.length() - 3);

        return fibonacciStr;
    }
    public static String getLogicInterfaces() {
        String interfaces = "";
        try {

            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while(networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                interfaces += "Nombre: " + networkInterface.getName() + "\nIPs: " + networkInterface.getInetAddresses() + "\n";
                interfaces += "-----\n\n";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return interfaces;
    }

    public static String scanPorts(String ipAddress) {
        String ports = "";

        for (int port = 1; port <= 65535; port++) {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(ipAddress, port), 500); // Timeout de 1 segundo
                socket.close();
                ports += port + " - ";
            } catch (Exception ex) {
            }
        }

        return ports.substring(0, ports.length() - 3);
    }

    public static String executeCmdlet(String cmdlet) {
        String result = "";

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", cmdlet);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                result += line + "\n";
            }
            reader.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void sendMessage(String recipient, String message, CallbackPrx proxy, Current current) {
        executor.execute(() -> {
            activeThreads.incrementAndGet();
            CallbackPrx recipientProxy = registeredClients.get(recipient);
            if (recipientProxy != null) {
                recipientProxy.responseCallback(message);
            }
            activeThreads.decrementAndGet();
        });
    }


    @Override
    public void broadcastMessage(String message, CallbackPrx proxy, Current current) {
        executor.execute(() -> {
            activeThreads.incrementAndGet();
            for (CallbackPrx clientProxy : registeredClients.values()) {
                clientProxy.responseCallback(message);
            }
            activeThreads.decrementAndGet();
        });
    }

    @Override
    public void register(String hostname, Demo.CallbackPrx proxy, com.zeroc.Ice.Current current) {
        registeredClients.put(hostname, proxy);
    }

    // Método para manejar múltiples solicitudes concurrentes
    public Response printStringAsync(String s, Current current) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            printString(s, current);
        });
        executor.shutdown();
        return new Response(0, "Server response: Procesamiento en curso...");
    }

    private static int evaluateMessage(String message) {
        try {
            int messageInt = Integer.parseInt(message);
            if (messageInt < 0) {
                throw new Exception();
            }
            return 0; // Fibonacci
        } catch (NumberFormatException e) {
            if (message.startsWith("listifs")) {
                return 1; // Listar interfaces
            } else if (message.startsWith("listports")) {
                return 2; // Escanear puertos
            } else if (message.startsWith("!")) {
                return 3; // Ejecutar comando
            } else {
                return -1; // Mensaje no reconocido
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
