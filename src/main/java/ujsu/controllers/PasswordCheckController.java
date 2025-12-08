package ujsu.controllers;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.*;

@RestController
@RequestMapping("/api/password")
public class PasswordCheckController {
    
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private volatile Process pythonProcess;
    private BufferedWriter processWriter;
    private BufferedReader processReader;
    private BufferedReader processErrorReader;
    private volatile boolean pythonReady = false;
    
    public static class PasswordRequest {
        private String password;
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    @PostConstruct
    public void init() {
        try {
            File pythonScript = new File("smart_password_checker.py");
            File mlModel = new File("smart_password_classifier.pkl");
            
            if (!pythonScript.exists()) {
                pythonScript = new File("password_ml_checker.py");
            }
            if (!mlModel.exists()) {
                mlModel = new File("balanced_password_classifier.pkl");
            }
            
            if (pythonScript.exists() && mlModel.exists()) {
                startPythonProcess(pythonScript.getName());
                pythonReady = true;
            } else {
                pythonReady = false;
            }
            
        } catch (Exception e) {
            pythonReady = false;
        }
    }
    
    @PreDestroy
    public void cleanup() {
        if (pythonProcess != null && pythonProcess.isAlive()) {
            pythonProcess.destroy();
            try {
                pythonProcess.waitFor(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        executor.shutdown();
    }
    
    @PostMapping("/check")
    public Map<String, Object> checkPassword(@RequestBody PasswordRequest request) {
        String password = request.getPassword();
        
        try {
            if (pythonReady && password != null && !password.isEmpty()) {
                try {
                    Map<String, Object> pythonResult = callPythonML(password);
                    return formatResponse(pythonResult, password);
                } catch (Exception e) {
                }
            }
            
            Map<String, Object> localResult = calculateLocalPasswordStrength(password);
            return formatResponse(localResult, password);
            
        } catch (Exception e) {
            return getFallbackResponse(password);
        }
    }
    
    private synchronized void startPythonProcess(String scriptName) throws IOException {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", scriptName);
            pb.directory(new File(".").getAbsoluteFile());
            pb.redirectErrorStream(false);
            
            this.pythonProcess = pb.start();
            
            this.processWriter = new BufferedWriter(
                new OutputStreamWriter(pythonProcess.getOutputStream())
            );
            this.processReader = new BufferedReader(
                new InputStreamReader(pythonProcess.getInputStream())
            );
            this.processErrorReader = new BufferedReader(
                new InputStreamReader(pythonProcess.getErrorStream())
            );
            
            new Thread(() -> {
                try {
                    String line;
                    while ((line = processErrorReader.readLine()) != null) {
                    }
                } catch (IOException e) {
                }
            }).start();
            
            Thread.sleep(1500);
            
            boolean gotResponse = false;
            long startTime = System.currentTimeMillis();
            
            while (!gotResponse && System.currentTimeMillis() - startTime < 5000) {
                if (processReader.ready()) {
                    String line = processReader.readLine();
                    if (line != null) {
                        if (line.contains("DEBUG") || line.contains("загружен") || 
                            line.contains("используется") || line.contains("модель")) {
                            gotResponse = true;
                        }
                    }
                }
                Thread.sleep(100);
            }
            
        } catch (IOException | InterruptedException e) {
            throw new IOException("Не удалось запустить Python процесс", e);
        }
    }
    
    private synchronized Map<String, Object> callPythonML(String password) 
            throws IOException, InterruptedException, TimeoutException {
        
        if (pythonProcess == null || !pythonProcess.isAlive()) {
            startPythonProcess("smart_password_checker.py");
            Thread.sleep(2000);
        }
        
        processWriter.write(password + "\n");
        processWriter.flush();
        
        String response = readWithTimeout(10000);
        
        if (response == null || response.trim().isEmpty()) {
            throw new IOException("Пустой ответ от Python");
        }
        
        String[] parts = response.split(",");
        if (parts.length >= 6) {
            return Map.of(
                "strength", parts[0].trim(),
                "confidence", Double.parseDouble(parts[1].trim()),
                "weakProbability", Double.parseDouble(parts[2].trim()),
                "mediumProbability", Double.parseDouble(parts[3].trim()),
                "strongProbability", Double.parseDouble(parts[4].trim()),
                "score", Integer.parseInt(parts[5].trim())
            );
        } else if (parts.length >= 5) {
            double weakProb = Double.parseDouble(parts[2].trim());
            double mediumProb = Double.parseDouble(parts[3].trim());
            double strongProb = Double.parseDouble(parts[4].trim());
            int score = (int)(strongProb * 100 * 0.5 + mediumProb * 100 * 0.3 + weakProb * 100 * 0.2);
            
            return Map.of(
                "strength", parts[0].trim(),
                "confidence", Double.parseDouble(parts[1].trim()),
                "weakProbability", weakProb,
                "mediumProbability", mediumProb,
                "strongProbability", strongProb,
                "score", Math.min(score, 100)
            );
        } else {
            throw new IOException("Неверный формат ответа от Python: " + response);
        }
    }
    
    private String readWithTimeout(long timeoutMs) throws TimeoutException {
        try {
            Future<String> future = executor.submit(() -> {
                String line = processReader.readLine();
                return line;
            });
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw e;
        } catch (Exception e) {
            return null;
        }
    }
    
    private Map<String, Object> calculateLocalPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return getFallbackResponse(password);
        }
        
        int score = 0;
        if (password.length() >= 12) score += 40;
        else if (password.length() >= 8) score += 30;
        else if (password.length() >= 6) score += 20;
        else score += 10;
        
        boolean hasUpper = !password.equals(password.toLowerCase());
        boolean hasLower = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[^A-Za-z0-9].*");
        
        if (hasUpper) score += 15;
        if (hasLower) score += 15;
        if (hasDigit) score += 20;
        if (hasSpecial) score += 25;
        
        String strength;
        if (score >= 90) strength = "strong";
        else if (score >= 70) strength = "strong";
        else if (score >= 50) strength = "medium";
        else strength = "weak";
        
        return Map.of(
            "strength", strength,
            "score", Math.min(score, 100),
            "confidence", 0.9,
            "weakProbability", strength.equals("weak") ? 0.8 : 0.1,
            "mediumProbability", strength.equals("medium") ? 0.8 : 0.1,
            "strongProbability", strength.equals("strong") ? 0.8 : 0.1
        );
    }
    
    private Map<String, Object> formatResponse(Map<String, Object> data, String password) {
        Map<String, Object> response = new HashMap<>(data);
        response.put("recommendations", new String[]{
            "Проверка выполнена" + (pythonReady ? " с использованием ML модели" : " локально"),
            "Длина пароля: " + (password != null ? password.length() : 0) + " символов"
        });
        return response;
    }
    
    private Map<String, Object> getFallbackResponse(String password) {
        return Map.of(
            "strength", "weak",
            "score", 30,
            "confidence", 0.5,
            "weakProbability", 0.8,
            "mediumProbability", 0.1,
            "strongProbability", 0.1,
            "recommendations", new String[]{
                "Ошибка проверки",
                "Используйте пароль длиной не менее 8 символов"
            }
        );
    }
}