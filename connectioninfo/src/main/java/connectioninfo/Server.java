package connectioninfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class Server {

    private final static Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private final static int DEFAULT_PORT = 4567;

    private final static ObjectMapper JSON_MAPPER = new ObjectMapper().registerModules(
            new Jdk8Module(),
            new JavaTimeModule(),
            new ParameterNamesModule()
    );

    public static void main(String... args) {
        port(discoverPort());

        get("/healthz", (req, res) -> "Healthy: OK");
        get("/readyz",  (req, res) -> "Ready: OK");

        get("/", (req, res) -> {
            LOGGER.info("request received: {}", req.ip());

            HttpRequestInfo requestInfo = new HttpRequestInfo(
                    req.requestMethod(),
                    req.pathInfo(),
                    req.queryMap().toMap(),
                    createHeadersMap(req),
                    req.ip());

            res.status(200);
            res.header("content-type", "application/json");

            return JSON_MAPPER.writer().withDefaultPrettyPrinter().writeValueAsString(requestInfo);
        });
    }

    private static Map<String, String> createHeadersMap(Request request) {
        Map<String, String> result = new HashMap<>();

        for (String header : request.headers()) {
            result.put(header, request.headers(header));
        }

        return result;
    }

    private static int discoverPort() {
        String candidatePort = System.getenv("PORT");
        if (candidatePort != null) {
            try {
                return Integer.parseInt(candidatePort);
            } catch (NumberFormatException ex) {
                LOGGER.error("Invalid server listen port value: {}", candidatePort, ex);
                throw ex;
            }
        } else {
            return DEFAULT_PORT;
        }
    }
}

class HttpRequestInfo {

    private final String method;
    private final String path;
    private final Map<String, String[]> queryParams;
    private final Map<String, String> headers;
    private final String remoteAddress;

    HttpRequestInfo(
            String method,
            String path,
            Map<String, String[]> queryParams,
            Map<String, String> headers,
            String remoteAddress
    ) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.headers = headers;
        this.remoteAddress = remoteAddress;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String[]> getQueryParams() {
        return queryParams;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }
}
