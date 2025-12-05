package ujsu.controllers;

import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.util.Map;
import java.util.concurrent.*;

@RestController
@RequestMapping("/api/password")
public class PasswordCheckController {
    
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private volatile Process pythonProcess;
    private BufferedWriter processWriter;
    private BufferedReader processReader;
    
    public void init() {
        startPythonProcess();
    }
    
    public void cleanup() {
        if (pythonProcess != null && pythonProcess.isAlive()) {
            pythonProcess.destroy();
        }
        executor.shutdown();
    }
    
    @GetMapping("/check")
    public Map<String, Object> checkPassword(@RequestParam String password) {
        long startTime = System.currentTimeMillis();
        
        try {
            Map<String, Object> result = callPythonModelFast(password);
            long endTime = System.currentTimeMillis();
            System.out.println("Response time: " + (endTime - startTime) + "ms");
            return result;
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return getFallbackResponse(password);
        }
    }
    
    private synchronized void startPythonProcess() {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "password_predict.py");
            pb.directory(new File(".").getAbsoluteFile());
            this.pythonProcess = pb.start();
            
            this.processWriter = new BufferedWriter(
                new OutputStreamWriter(pythonProcess.getOutputStream())
            );
            this.processReader = new BufferedReader(
                new InputStreamReader(pythonProcess.getInputStream())
            );
            
            System.out.println("Python process started and ready!");
            
        } catch (IOException e) {
            System.err.println("Failed to start Python process: " + e.getMessage());
        }
    }
    
    private synchronized Map<String, Object> callPythonModelFast(String password) 
            throws IOException, InterruptedException {
        
        // Перезапускаем процесс если он умер
        if (pythonProcess == null || !pythonProcess.isAlive()) {
            startPythonProcess();
            Thread.sleep(100); // Даем время на запуск
        }
        
        // Отправляем пароль
        processWriter.write(password + "\n");
        processWriter.flush();
        
        // Читаем ответ с таймаутом
        String response = readWithTimeout(5000); // 5 секунд таймаут
        
        if (response != null && response.contains(",")) {
            String[] parts = response.split(",");
            
            return Map.of(
                "strength", parts[0].trim(),
                "confidence", Double.parseDouble(parts[1].trim()),
                "weakProbability", Double.parseDouble(parts[2].trim()),
                "mediumProbability", Double.parseDouble(parts[3].trim()),
                "strongProbability", Double.parseDouble(parts[4].trim())
            );
        }
        
        throw new IOException("Invalid response from Python: " + response);
    }
    
    private String readWithTimeout(long timeoutMs) {
        try {
            Future<String> future = executor.submit(() -> processReader.readLine());
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            System.err.println("Read timeout - restarting process");
            restartPythonProcess();
            return null;
        } catch (Exception e) {
            System.err.println("Read error: " + e.getMessage());
            return null;
        }
    }
    
    private synchronized void restartPythonProcess() {
        if (pythonProcess != null && pythonProcess.isAlive()) {
            pythonProcess.destroy();
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        startPythonProcess();
    }
    
    private Map<String, Object> getFallbackResponse(String password) {
        // Быстрый fallback на основе простых правил
        int length = password.length();
        boolean hasUpper = !password.equals(password.toLowerCase());
        boolean hasLower = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = !password.matches("[A-Za-z0-9]*");
        
        int score = 0;
        if (length >= 8) score++;
        if (length >= 12) score++;
        if (hasUpper && hasLower) score++;
        if (hasDigit) score++;
        if (hasSpecial) score++;
        
        String strength;
        double weakProb, mediumProb, strongProb;
        
        if (score >= 4) {
            strength = "strong";
            weakProb = 0.1;
            mediumProb = 0.2;
            strongProb = 0.7;
        } else if (score >= 2) {
            strength = "medium";
            weakProb = 0.3;
            mediumProb = 0.5;
            strongProb = 0.2;
        } else {
            strength = "weak";
            weakProb = 0.7;
            mediumProb = 0.2;
            strongProb = 0.1;
        }
        
        return Map.of(
            "strength", strength,
            "confidence", 0.8,
            "weakProbability", weakProb,
            "mediumProbability", mediumProb,
            "strongProbability", strongProb
        );
    }
}