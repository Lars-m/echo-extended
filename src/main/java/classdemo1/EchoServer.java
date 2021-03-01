package classdemo1;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class EchoServer {
    public static final int DEFAULT_PORT = 2345;
    private ServerSocket serverSocket;

    public static Map<String, String> words;
    static {
        words = new HashMap<>();
        words.put("hund", "dog");
        words.put("farve", "color");
        words.put("hus", "house");
    }

    //Protocol
    //Send besked til klient lige efter han er connected

    private boolean handleCommand(String message, PrintWriter pw) {
        String[] parts = message.split("#");
        System.out.println("Size: " + parts.length);
        if (parts.length == 1) {
            if(parts[0].equals("CLOSE")){
                pw.println("CLOSE#");
                return false;
            }
            throw new IllegalArgumentException("Sent request does not obey the protocol");
        } else if (parts.length == 2) {
            String token = parts[0];
            String param = parts[1];
            switch (token) {
                case "UPPER":
                    pw.println(param.toUpperCase());
                    break;
                case "LOWER":
                    pw.println(param.toLowerCase());
                    break;
                case "REVERSE":
                    String reversed = new StringBuffer(param).reverse().toString();
                    pw.println(reversed);
                    break;
                case "TRANSLATE":
                    String translated = words.get(param);
                    translated = translated !=null ? translated : "NOT_FOUND#";
                    pw.println(translated);
                    break;
                default:
                    throw new IllegalArgumentException("Sent request does not obey the protocol");
            }
        }
        return true;
    }

    private void handleClient(Socket socket) throws IOException {
        PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
        Scanner scanner = new Scanner(socket.getInputStream());
        pw.println("Du er connected, send en streng for at få den upper cased, send 'stop' for stop");
        String message = "";// = scanner.nextLine(); //Blocking call
        boolean keepRunning = true;
        try {
            while (keepRunning) {
                message = scanner.nextLine();  //Blocking call
                keepRunning = handleCommand(message, pw);
            }
        } catch (Exception e) {
            System.out.println("UPPPS: "+e.getMessage());;
        }
        pw.println("Connection is closing");
        socket.close();

    }

    private void startServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started, listening on : " + port);

        while (true) {
            System.out.println("Waiting for a client");
            Socket socket = serverSocket.accept();                       //Blocking call
            System.out.println("New client connected");
            handleClient(socket);
        }
    }

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number, using default port :" + DEFAULT_PORT);
            }
        }
        new EchoServer().startServer(port);


    }
}
