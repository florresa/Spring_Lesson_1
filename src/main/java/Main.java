import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
  public static void main(String[] args) throws IOException {
    AtomicReference<String> message = new AtomicReference<>("");
    final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

      final var server = new Server(9999, validPaths, 64);
      FileHandler filehandler = new FileHandler();

      // добавление хендлеров (обработчиков)
      server.addHandler("GET", "/messages", new Handler() {
          @Override
          public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
              var response = message.get().getBytes();
              responseStream.write((
                      "HTTP/1.1 200 OK\r\n" +
                              "Content-Length: " + response.length + "\r\n" +
                              "Connection: close\r\n" +
                              "\r\n"
              ).getBytes());
              responseStream.write(response);
              responseStream.flush();
          }
      });

      server.addHandler("POST", "/messages", new Handler() {
          public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
              final var in = new BufferedReader(new InputStreamReader(request.getBody()));
              final var body = in.readLine();
              message.set(body);
              responseStream.write((
                      "HTTP/1.1 201 Created\r\n" +
                              "Content-Length: 0\r\n" +
                              "Connection: close\r\n" +
                              "\r\n"
              ).getBytes());
              responseStream.flush();
          }
      });

      server.addHandler("GET", "/classic.html", new Handler() {
          @Override
          public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
              if (request.getPath().equals("/classic.html")) {
                  final var filePath = Path.of(".", "public", request.getPath());
                  final var mimeType = Files.probeContentType(filePath);
                  final var template = Files.readString(filePath);
                  final var content = template.replace(
                          "{time}",
                          LocalDateTime.now().toString()
                  ).getBytes();
                  responseStream.write((
                          "HTTP/1.1 200 OK\r\n" +
                                  "Content-Type: " + mimeType + "\r\n" +
                                  "Content-Length: " + content.length + "\r\n" +
                                  "Connection: close\r\n" +
                                  "\r\n"
                  ).getBytes());
                  responseStream.write(content);
                  responseStream.flush();
              }
          }
      });

      validPaths.forEach(path -> server.addHandler("GET", path, filehandler));

    server.start();
  }
}



