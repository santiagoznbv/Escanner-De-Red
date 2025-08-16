package logic;

public class HostRecord {
    private final String ip;
    private final String hostname;
    private final boolean alive;
    private final long latencyMs;

    public HostRecord(String ip, String hostname, boolean alive, long latencyMs) {
        this.ip = ip;
        this.hostname = hostname;
        this.alive = alive;
        this.latencyMs = latencyMs;
    }

    public String getIp() { return ip; }
    public String getHostname() { return hostname; }
    public boolean isAlive() { return alive; }
    public long getLatencyMs() { return latencyMs; }
}