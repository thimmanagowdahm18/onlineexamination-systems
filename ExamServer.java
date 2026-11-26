import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class ExamServer {
    static final Path WEB = Paths.get("web").toAbsolutePath();
    static final String[] QUESTIONS = {
        "Which keyword is used to inherit a class in Java?",
        "Which HTML tag creates a hyperlink?",
        "Which CSS property changes text color?",
        "Which method is the entry point of a Java program?",
        "Which tag is used for the largest heading in HTML?"
    };
    static final String[][] OPTIONS = {
        {"implements", "extends", "inherit", "super"},
        {"<link>", "<href>", "<a>", "<url>"},
        {"font-style", "background", "color", "text-size"},
        {"start()", "run()", "main()", "init()"},
        {"<h6>", "<head>", "<h1>", "<header>"}
    };
    static final int[] ANSWERS = {1, 2, 2, 2, 2};

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/questions", e -> sendJson(e, questionsJson()));
        server.createContext("/api/login", ExamServer::login);
        server.createContext("/api/submit", ExamServer::submit);
        server.createContext("/", ExamServer::staticFiles);
        server.setExecutor(null);
        server.start();
        System.out.println("Online Examination System running at http://localhost:8080");
    }
    static void login(HttpExchange e) throws IOException {
        if (!"POST".equals(e.getRequestMethod())) { send(e, 405, "Use POST", "text/plain"); return; }
        String body = new String(e.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String,String> form = parse(body);
        String name = form.getOrDefault("name", "").trim();
        if (name.isEmpty()) { sendJson(e, "{\"ok\":false,\"message\":\"Enter your name\"}"); return; }
        sendJson(e, "{\"ok\":true,\"name\":\"" + escape(name) + "\"}");
    }
    static void submit(HttpExchange e) throws IOException {
        if (!"POST".equals(e.getRequestMethod())) { send(e, 405, "Use POST", "text/plain"); return; }
        String body = new String(e.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String,String> form = parse(body); int score = 0;
        for (int i=0;i<ANSWERS.length;i++) if (String.valueOf(ANSWERS[i]).equals(form.get("q"+i))) score++;
        sendJson(e, "{\"score\":" + score + ",\"total\":" + ANSWERS.length + ",\"percent\":" + (score*100/ANSWERS.length) + "}");
    }
    static String questionsJson() {
        StringBuilder s = new StringBuilder("{\"questions\":[");
        for(int i=0;i<QUESTIONS.length;i++) {
            if(i>0)s.append(','); s.append("{\"text\":\"").append(escape(QUESTIONS[i])).append("\",\"options\":[");
            for(int j=0;j<4;j++){if(j>0)s.append(',');s.append('\"').append(escape(OPTIONS[i][j])).append('\"');}
            s.append("]}");
        }
        return s.append("]}").toString();
    }
    static Map<String,String> parse(String body) {
        Map<String,String> m = new HashMap<>();
        for(String pair:body.split("&")){String[] p=pair.split("=",2);if(p.length==2)m.put(decode(p[0]),decode(p[1]));}
        return m;
    }
    static String decode(String s){try{return java.net.URLDecoder.decode(s,"UTF-8");}catch(Exception ex){return s;}}
    static String escape(String s){return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n"," ").replace("\r","");}
    static void sendJson(HttpExchange e,String s)throws IOException{send(e,200,s,"application/json; charset=utf-8");}
    static void send(HttpExchange e,int status,String s,String type)throws IOException{byte[] b=s.getBytes(StandardCharsets.UTF_8);e.getResponseHeaders().set("Content-Type",type);e.getResponseHeaders().set("Access-Control-Allow-Origin","*");e.sendResponseHeaders(status,b.length);try(OutputStream o=e.getResponseBody()){o.write(b);}}
    static void staticFiles(HttpExchange e)throws IOException{
        String path=e.getRequestURI().getPath(); if(path.equals("/"))path="/index.html";
        Path file=WEB.resolve(path.substring(1)).normalize();
        if(!file.startsWith(WEB)||!Files.exists(file)||Files.isDirectory(file)){send(e,404,"Not found","text/plain");return;}
        String type=file.toString().endsWith(".css")?"text/css":file.toString().endsWith(".js")?"text/javascript":"text/html";
        byte[] b=Files.readAllBytes(file);e.getResponseHeaders().set("Content-Type",type+"; charset=utf-8");e.sendResponseHeaders(200,b.length);try(OutputStream o=e.getResponseBody()){o.write(b);}
    }
}
