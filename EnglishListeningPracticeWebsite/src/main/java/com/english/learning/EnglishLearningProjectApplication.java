package com.english.learning;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Properties;

import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@RequiredArgsConstructor
public class EnglishLearningProjectApplication {

    private final Environment env;

    public static void main(String[] args) {
        Properties properties = new Properties();
        configureAvailablePort(properties);

        SpringApplication app = new SpringApplication(EnglishLearningProjectApplication.class);
        // Cách chuẩn của Spring Boot để set property mặc định trước khi chạy
        app.setDefaultProperties(properties); 
        app.run(args);
    }

    private static void configureAvailablePort(Properties props) {
        int port = 8080;
        try (InputStream input = EnglishLearningProjectApplication.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                Properties tempProps = new Properties();
                tempProps.load(input);
                port = Integer.parseInt(tempProps.getProperty("server.port", "8080"));
            }
        } catch (IOException ignored) {}

        if (!isPortAvailable(port)) {
            System.out.println("⚠️ Cổng " + port + " đang bận. Đang tìm cổng khác...");
            while (!isPortAvailable(port)) port++;
            System.out.println("✅ Chuyển sang sử dụng cổng trống: " + port);
        }
        System.setProperty("server.port", String.valueOf(port));
        props.put("server.port", String.valueOf(port));
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String port = env.getProperty("server.port", "8080");
        String ctx = env.getProperty("server.servlet.context-path", "");
        String localIp = getLocalIpAddress();

        System.out.println("\n=======================================================");
        System.out.println("🚀 Ứng dụng English Learning Website đã khởi động!");
        System.out.println("🌍 Web UI:         http://localhost:" + port + ctx);
        System.out.println("📱 Bootstrap:      http://localhost:" + port + ctx + "/api/mobile/bootstrap");
        System.out.println("🤖 Android (emu):  http://10.0.2.2:" + port + ctx + "/api/mobile/catalog/bootstrap-lite");
        System.out.println("📲 Android (LAN):  http://" + localIp + ":" + port + ctx + "/api/mobile/catalog/bootstrap-lite");
        System.out.println("=======================================================\n");

        startNgrok(port);
    }

    private void startNgrok(String port) {
        String token = env.getProperty("ngrok.auth-token");
        String domain = env.getProperty("ngrok.domain");
        String path = env.getProperty("ngrok.path", "ngrok");

        if (token == null || domain == null) {
            System.out.println("⚠️ Bỏ qua ngrok: Chưa cấu hình auth-token hoặc domain tĩnh.");
            return;
        }

        String executable = findNgrokExecutable(path);
        System.out.println("🌐 Đang khởi chạy ngrok trên cổng " + port + " (Domain: " + domain + ")");

        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder builder = null;

            if (os.contains("win")) {
                File bat = File.createTempFile("run_ngrok", ".bat");
                bat.deleteOnExit();
                try (FileWriter writer = new FileWriter(bat)) {
                    writer.write("@echo off\n");
                    writer.write("title Ngrok Monitor\n");
                    writer.write("echo Cap quyen ngrok...\n");
                    writer.write("\"" + executable + "\" config add-authtoken " + token + "\n");
                    // Gọi ngrok mở ở một cửa sổ khác
                    writer.write("start \"Ngrok Tunnel\" \"" + executable + "\" http --domain=" + domain + " " + port + "\n");
                    writer.write("echo Ngrok dang chay hop le. He thong dang giam sat Server...\n");
                    writer.write(":monitor\n");
                    writer.write("timeout /t 3 /nobreak >nul\n");
                    // Kiểm tra ngrok còn chạy không (user có thể tắt cửa sổ ngrok)
                    writer.write("tasklist /FI \"IMAGENAME eq ngrok.exe\" 2>nul | findstr /I \"ngrok.exe\" >nul\n");
                    writer.write("if %errorlevel% neq 0 (\n");
                    writer.write("    echo [!] Ngrok da bi tat. Dang thoat giam sat...\n");
                    writer.write("    exit\n");
                    writer.write(")\n");
                    // Kiểm tra Cổng Web (8080/8081) của Spring Boot có còn LISTENING không
                    writer.write("netstat -ano | findstr \"LISTENING\" | findstr \":" + port + "\" >nul\n");
                    writer.write("if %errorlevel% neq 0 (\n");
                    writer.write("    echo [!] Phat hien rong Web Server. Dang ban ha Ngrok...\n");
                    writer.write("    taskkill /F /IM ngrok.exe /T >nul 2>&1\n");
                    writer.write("    exit\n");
                    writer.write(")\n");
                    writer.write("goto monitor\n");
                }
                builder = new ProcessBuilder("cmd.exe", "/c", "start", "/min", "/wait", bat.getName());
                builder.directory(bat.getParentFile());
            } else if (os.contains("mac")) {
                String cmd = "\"" + executable + "\" config add-authtoken " + token + " && \"" + executable + "\" http --domain=" + domain + " " + port;
                builder = new ProcessBuilder("osascript", "-e", "tell application \"Terminal\" to do script \"" + cmd + "\"");
            } else {
                String cmd = "\"" + executable + "\" config add-authtoken " + token + " && \"" + executable + "\" http --domain=" + domain + " " + port;
                builder = new ProcessBuilder("gnome-terminal", "--wait", "--", "bash", "-c", cmd + "; exec bash");
            }
            
            Process process = builder.start();
            
            // Đăng ký sự kiện tắt ngrok khi Web (Spring Boot) bị ngắt
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (os.contains("win")) {
                        Runtime.getRuntime().exec(new String[]{"taskkill", "/F", "/IM", "ngrok.exe", "/T"});
                    } else {
                        Runtime.getRuntime().exec(new String[]{"killall", "ngrok"});
                    }
                    System.out.println("🛑 Đã tự động đóng tiến trình ngrok.");
                } catch (Exception ignored) {}
            }));
            
            Thread monitorThread = new Thread(() -> {
                try {
                    process.waitFor();
                    System.err.println("\n⚠️ [CẢNH BÁO] Terminal ngrok đã bị ngắt!");
                    System.err.println("⚠️ Ứng dụng web vẫn đang chạy ở cổng " + port + ", nhưng kết nối từ bên ngoài (Internet/Mobile) có thể bị gián đoạn.\n");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            monitorThread.setDaemon(true);
            monitorThread.start();
            
        } catch (IOException e) {
            System.err.println("❌ Lỗi khởi chạy ngrok: " + e.getMessage());
        }
    }

    private String findNgrokExecutable(String defaultPath) {
        if (defaultPath.contains(File.separator) && Files.exists(Path.of(defaultPath))) return defaultPath;

        String os = System.getProperty("os.name").toLowerCase();
        // Gọi thẳng qua cmd/bash để mượn khả năng tự phân giải PATH của hệ điều hành
        String[] searchCmd = os.contains("win") ? 
                new String[]{"cmd.exe", "/c", "where " + defaultPath} : 
                new String[]{"bash", "-c", "which " + defaultPath};

        try {
            Process process = new ProcessBuilder(searchCmd).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null && !line.trim().isEmpty()) return line.trim();
            }
        } catch (Exception ignored) {}

        String[] commonPaths = {
                System.getenv("LOCALAPPDATA") + "\\ngrok\\ngrok.exe",
                System.getenv("USERPROFILE") + "\\ngrok.exe",
                "/usr/local/bin/ngrok",
                "/opt/ngrok/ngrok"
        };

        for (String p : commonPaths) {
            if (p != null && Files.exists(Path.of(p))) return p;
        }
        return defaultPath;
    }

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> itfs = NetworkInterface.getNetworkInterfaces(); itfs.hasMoreElements();) {
                NetworkInterface ni = itfs.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
                
                for (Enumeration<InetAddress> addrs = ni.getInetAddresses(); addrs.hasMoreElements();) {
                    InetAddress addr = addrs.nextElement();
                    String ip = addr.getHostAddress();
                    if (ip.contains(".") && !ip.startsWith("127.")) return ip;
                }
            }
        } catch (Exception ignored) {}
        return "127.0.0.1";
    }
}