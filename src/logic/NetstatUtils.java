package logic;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class NetstatUtils {

    // Ejecuta un comando netstat y devuelve la salida en String
    private static String runCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            reader.close();
            process.waitFor();
        } catch (Exception e) {
            return "Error ejecutando comando: " + e.getMessage();
        }
        return output.toString();
    }

    // netstat -ano
    public static String showActiveConnections() {
        return runCommand("netstat -ano");
    }

    // netstat -e
    public static String showInterfaceStats() {
        return runCommand("netstat -e");
    }

    // netstat -a
    public static String showAllPorts() {
        return runCommand("netstat -a");
    }
}
