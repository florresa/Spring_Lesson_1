import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private int port;
    List<String> validPaths;
    ExecutorService threadPool;
    Map<String, Handler> handlers = new ConcurrentHashMap<>();

    public Server(int port, List<String> validPaths, int threadsQuantity) {
        this.port = port;
        this.validPaths = validPaths;
        threadPool = Executors.newFixedThreadPool(threadsQuantity);
    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.put(method + path, handler);
    }


    public void start() throws IOException {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    final var socket = serverSocket.accept();
                    threadPool.execute(() -> processRequest(socket));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void processRequest(Socket socket) {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {

            InputStream stream = socket.getInputStream();

            // первая строчка из запроса, проверяем что она сформирована по шаблону GET /path HTTP/1.1
            final var firstLine = in.readLine();
            final var parts = firstLine.split(" ");

            if (parts.length != 3) {
                out.write((
                        "HTTP/1.1 400 Bad Request\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                return;
            }

            Map<String, String> headers = new HashMap<>();
            while (true) {
                String line = in.readLine();
                if (line.isEmpty()) {
                    break;
                }
                final var headersParts = line.split(": ");
                headers.put(headersParts[0], headersParts[1]);
            }


            // формируем Request
            Request request = new Request(parts[0], parts[1], headers, stream);
            var handler = handlers.get(request.getMethod() + request.getPath());

            if (handler == null) {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                return;
            }

            handler.handle(request, out);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


