import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Request {
    private String method;
    private String path;
    private Map<String, String> headers;
    private InputStream body;
    private List<NameValuePair> queryParams;

    public Request() {
    }

    public Request(String method, String path, Map<String, String> headers, InputStream body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
        this.queryParams = URLEncodedUtils.parse(path, StandardCharsets.UTF_8);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public InputStream getBody() {
        return body;
    }

    public List<NameValuePair> getQueryParam(String name) {
        return queryParams.stream().filter(p ->p.getName().equals(name)).collect(Collectors.toList());
    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }
}
