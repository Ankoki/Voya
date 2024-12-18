package us.byeol.voya.web;

import android.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import mx.kenzie.argo.Json;
import us.byeol.voya.misc.Log;

/**
 * Class to execute web requests. <p>
 * These are handled on the main thread, and any async handling should
 * be dealt with by the executor.
 * See {@link java.util.concurrent.CompletableFuture#supplyAsync(Supplier)}, which could be useful.
 */
public class WebRequest {

    private int connectTimeout = -1, readTimeout = -1;
    private boolean allowRedirects = true;
    private final RequestType type;
    private final URL url;
    private final List<Pair<String, String>> headers = new ArrayList<>(), parameters = new ArrayList<>();
    private final List<Map<String, Object>> data = new ArrayList<>();

    /**
     * Creates a new web request.
     *
     * @param url  the url to execute on.
     * @param type the request type.
     */
    public WebRequest(URL url, RequestType type) {
        this.url = url;
        this.type = type;
    }

    /**
     * Creates a new web request.
     *
     * @param url  the url to execute on.
     * @param type the request type.
     * @throws MalformedURLException if the string given is malformed.
     */
    public WebRequest(String url, RequestType type) throws MalformedURLException {
        this.url = new URL(url);
        this.type = type;
    }

    /**
     * Gets the current request's URL
     *
     * @return the current URL.
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Adds a header to the current request.
     *
     * @param key   the key of the header.
     * @param value the value of the header.
     * @return the current request, for chaining.
     */
    public WebRequest addHeader(String key, String value) {
        this.addHeader(new Pair<>(key, value));
        return this;
    }

    /**
     * Adds a header to the current request.
     *
     * @param pair the pair containing the key first, and the value of the header second.
     * @return the current request, for chaining.
     */
    public WebRequest addHeader(Pair<String, String> pair) {
        this.headers.add(pair);
        return this;
    }

    /**
     * Adds a parameter to the current request.
     *
     * @param key   the key of the parameter.
     * @param value the value of the parameter.
     * @return the current request, for chaining.
     */
    public WebRequest addParameter(String key, String value) {
        this.addParameter(new Pair<>(key, value));
        return this;
    }

    /**
     * Adds a parameter to the current request.
     *
     * @param pair the pair containing the key first, and the value of the parameter second.
     * @return the current request, for chaining.
     */
    public WebRequest addParameter(Pair<String, String> pair) {
        this.parameters.add(pair);
        return this;
    }

    /**
     * Adds a parameter to the current request.
     *
     * @param map the JSON map to add to the request.
     * @return the current request, for chaining.
     */
    public WebRequest addParameter(Map<String, Object> map) {
        this.data.add(map);
        return this;
    }

    /**
     * Sets the connection timeout in milliseconds.
     * 0 represents an infinite time, with no timeout.
     *
     * @param connectTimeout time to wait until timing out when connecting.
     * @return the current request, for chaining.
     */
    public WebRequest setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * Sets the read timeout in milliseconds.
     * 0 represents an infinite time, with no timeout.
     *
     * @param readTimeout time to wait until timing out when reading.
     * @return the current request, for chaining.
     */
    public WebRequest setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * Allows redirects for this request.
     *
     * @param allowRedirects if the request can redirect.
     * @return the current request, for chaining.
     */
    public WebRequest setAllowRedirects(boolean allowRedirects) {
        this.allowRedirects = allowRedirects;
        return this;
    }

    /**
     * Executes the current request.
     *
     * @return an optional containing the request response, if present.
     */
    public CompletableFuture<Optional<String>> execute() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                if (type == RequestType.PATCH) {
                    con.setRequestMethod("POST");
                    con.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                } else con.setRequestMethod(type.getMethod());
                for (Pair<String, String> header : headers)
                    con.setRequestProperty(header.first, header.second);
                if (connectTimeout != -1) con.setConnectTimeout(connectTimeout);
                if (readTimeout != -1) con.setReadTimeout(readTimeout);
                con.setInstanceFollowRedirects(allowRedirects);
                String parameters = this.getEncodedParameters();
                if (parameters != null) {
                    OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
                    out.write(parameters);
                    out.flush();
                    out.close();
                    con.getOutputStream().close();
                }
                InputStream stream = con.getResponseCode() / 100 == 2 ? con.getInputStream() : con.getErrorStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(stream));
                String line;
                StringBuilder content = new StringBuilder();
                while ((line = in.readLine()) != null) content.append(line);
                in.close();
                con.disconnect();
                String response = content.length() > 0 ? content.toString() : null;
                return Optional.ofNullable(response);
            } catch (IOException ex) {
                Log.error(ex);
            } return Optional.empty();
        });
    }

    /**
     * Gets the encoded parameters.
     *
     * @return the encoded parameters. Null if there are no parameters.
     */
    public String getEncodedParameters() throws UnsupportedEncodingException {
        if (parameters.isEmpty() && data.isEmpty()) return null;
        boolean hasJson = !data.isEmpty();
        Map<String, Object> empty = new HashMap<>();
        for (Map<String, Object> json : data)
            empty.putAll(json);
        if (hasJson) {
            for (Pair<String, String> entry : parameters)
                empty.put(entry.first, entry.second);
            List<Pair<String, List<String>>> list = new ArrayList<>();
            for (Map.Entry<String, Object> entry : empty.entrySet())
                if (entry.getValue() instanceof String[] array)
                    if (array.length == 0)
                        list.add(Pair.create(entry.getKey(), new ArrayList<>()));
            for (Pair<String, List<String>> pair : list)
                empty.put(pair.first, pair.second);
            return Json.toJson(empty);
        } else {
            StringBuilder builder = new StringBuilder();
            for (Pair<String, String> entry : parameters) {
                builder.append(URLEncoder.encode(entry.first, "UTF-8"));
                builder.append("=");
                builder.append(URLEncoder.encode(entry.second, "UTF-8"));
                builder.append("&");
            }
            builder.setLength(builder.length() - 1);
            return builder.length() == 0 ? null : builder.toString();
        }
    }

    /**
     * Utility to provide the possible request types.
     */
    public enum RequestType {
        GET("GET"),
        POST("POST"),
        HEAD("HEAD"),
        OPTIONS("OPTIONS"),
        PUT("PUT"),
        DELETE("DELETE"),
        TRACE("TRACE"),
        // Only supported by some hosts.
        PATCH("PATCH");

        final String method;

        RequestType(String method) {
            this.method = method;
        }

        /**
         * Gets the request method as a string.
         *
         * @return the request method.
         */
        public String getMethod() {
            return method;
        }

    }

}