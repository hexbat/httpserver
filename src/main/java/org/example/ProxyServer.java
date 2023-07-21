package org.example;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;

public class ProxyServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8500), 0);
        HttpContext context = server.createContext("/");
        context.setHandler(ProxyServer::handleRequest);
        server.start();
    }

    private static String getContent(String path) throws IOException {
        BufferedReader reader=null;
        InputStream stream = null;
        HttpsURLConnection connection = null;
        try {
            URL url=new URL(path);
            connection =(HttpsURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("X-Username", "user");
            connection.setRequestProperty("X-Password", "password");

            connection.setReadTimeout(10000);
            connection.connect();
            stream = connection.getInputStream();
            reader= new BufferedReader(new InputStreamReader(stream));
            StringBuilder buf=new StringBuilder();
            String line;
            while ((line=reader.readLine()) != null) {
                buf.append(line).append("\n");
            }
            return(buf.toString());
        }
        finally {
            if (reader != null) {
                reader.close();
            }
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    private static void handleRequest(HttpExchange exchange) throws IOException {
        String url = "https://slack.com/api/users.list";
        String content = getContent(url);
        System.out.println(content);
        JSONObject json = new JSONObject(content);
        String response = json.getString("error");
        System.out.println(response);
        exchange.sendResponseHeaders(200, response.getBytes().length);//response code and length
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}