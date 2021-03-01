package classdemo1_multithreaded_v2;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

class ClientHandler implements Runnable {

    private  static Map<String, String> words;
    private EchoServerMultithreaded echoServerMain;
    private  PrintWriter pw;
    Socket socket;


    //Provides each instance with a unique id. Simulates the unique userid we will need for the chat-server
    private static int id = 0;

    static {
        words = new HashMap<>();
        words.put("hund", "dog");
        words.put("farve", "color");
        words.put("hus", "house");
    }

    public ClientHandler(Socket s, EchoServerMultithreaded echoServerMain) {
        this.socket = s;
        this.id++;
        this.echoServerMain = echoServerMain;
    }

    public int getId() {
        return id;
    }

    void sendMessage(String msg){
        pw.println ("MSG_ALL#"+msg);
    }

    private boolean handleCommand(String message, PrintWriter pw) {
        String[] parts = message.split("#");
        System.out.println("Size: " + parts.length);
        if (parts.length == 1) {
            if (parts[0].equals("CLOSE")) {
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
                    translated = translated != null ? translated : "NOT_FOUND#";
                    pw.println(translated);
                    break;
                case "ALL":
                    echoServerMain.sendToAll(param);
                    break;
                default:
                    throw new IllegalArgumentException("Sent request does not obey the protocol");
            }
        }
        return true;
    }

    private void handleClient() throws IOException {
        pw = new PrintWriter(socket.getOutputStream(), true);
        Scanner scanner = new Scanner(socket.getInputStream());
        pw.println("Du er connected, send en streng for at få den upper cased, send 'stop' for stop");
        String message="";
        boolean keepRunning = true;
        try {
            while (keepRunning) {
                message = scanner.nextLine();  //Blocking call
                keepRunning = handleCommand(message, pw);
            }
        } catch (Exception e) {
            System.out.println("UPPPS: " + e.getMessage() +" ---> "+message);
        }
        pw.println("Connection is closing");
        socket.close();

    }

    @Override
    public void run() {
        try {
            handleClient();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

public class EchoServerMultithreaded {
    public static final int DEFAULT_PORT = 2345;
    ConcurrentHashMap<Integer, ClientHandler> allClientHandlers;

    void sendToAll(String msg){
         allClientHandlers.values().forEach(clientHandler -> {
             clientHandler.sendMessage(msg);
         });
    }


    private void startServer(int port) throws IOException {
        ServerSocket serverSocket;
        allClientHandlers = new ConcurrentHashMap<>();
        serverSocket = new ServerSocket(port);
        System.out.println("Server started, listening on : " + port);

        while (true) {
            System.out.println("Waiting for a client");
            Socket socket = serverSocket.accept();
            System.out.println("New client connected");
            ClientHandler clientHandler = new ClientHandler(socket,this);
            allClientHandlers.put(clientHandler.getId(),clientHandler);
            new Thread(clientHandler).start();
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
        new classdemo1_multithreaded_v2.EchoServerMultithreaded().startServer(port);


    }
}
