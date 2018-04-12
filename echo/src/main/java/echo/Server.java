package echo;

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

        post("/", (req, res) -> {
            LOGGER.info("request received: {}", req.ip());
            
            res.status(200);
            res.header("content-type", "application/json");
            
            Map<String, String> payload = new HashMap<String, String>(){{
                put("body", req.body());
            }};

            return JSON_MAPPER.writer().withDefaultPrettyPrinter().writeValueAsString(payload);
        });
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
