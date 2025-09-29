package logic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class HostProbe {
    public static boolean isValidIPv4(String ip) {
        return ip.matches("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    }

    public static List<HostRecord> probeRange(String startIP, String endIP, int timeoutMs) {
        List<HostRecord> out = new ArrayList<>();
        String[] s = startIP.split("\\.");
        String[] e = endIP.split("\\.");
        int from = Integer.parseInt(s[3]);
        int to = Integer.parseInt(e[3]);
        String base = s[0] + "." + s[1] + "." + s[2] + ".";
        for (int i = from; i <= to; i++) {
            String ip = base + i;
            out.add(pingOne(ip, timeoutMs));
        }
        return out;
    }

    private static HostRecord pingOne(String ip, int timeoutMs) {
        try {
            long t0 = System.currentTimeMillis();
            InetAddress addr = InetAddress.getByName(ip);
            boolean alive = addr.isReachable(timeoutMs);
            long elapsed = System.currentTimeMillis() - t0;
            String host = "";
            if (alive) host = reverseLookup(ip);
            return new HostRecord(ip, host, alive, elapsed);
        } catch (Exception ex) {
            return new HostRecord(ip, "", false, 0);
        }
    }

    private static String reverseLookup(String ip) {
        try {
            ProcessBuilder pb = new ProcessBuilder("nslookup", ip);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("name =")) {
                    return line.split("name =")[1].trim();
                }
            }
        } catch (Exception ignored) {}
        return "";
    }
}