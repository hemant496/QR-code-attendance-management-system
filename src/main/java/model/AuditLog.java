package model;

import java.sql.Timestamp;

public class AuditLog {
    private int logId;
    private String userId;
    private String userRole;
    private String action;
    private Timestamp timestamp;

    public AuditLog() {}

    public AuditLog(int logId, String userId, String userRole, String action, Timestamp timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.userRole = userRole;
        this.action = action;
        this.timestamp = timestamp;
    }

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
