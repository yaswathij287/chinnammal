package com.smarthostel.service;

import com.smarthostel.dao.SecurityLogDAO;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.model.SecurityLog;

import java.util.List;

/**
 * Service managing gate entries, verification methods, and security alerts.
 */
public class SecurityService {

    private final SecurityLogDAO securityLogDAO;

    public SecurityService() {
        this.securityLogDAO = new SecurityLogDAO();
    }

    public SecurityService(SecurityLogDAO securityLogDAO) {
        this.securityLogDAO = securityLogDAO;
    }

    public SecurityLog logEvent(Integer studentId, String eventType, String gate, String verificationMethod) throws DatabaseException {
        SecurityLog log = new SecurityLog(studentId, eventType, gate, verificationMethod);
        return securityLogDAO.addLog(log);
    }

    public List<SecurityLog> getAllLogs() throws DatabaseException {
        return securityLogDAO.getAllLogs();
    }

    public List<SecurityLog> getRecentLogs(int limit) throws DatabaseException {
        return securityLogDAO.getRecentLogs(limit);
    }

    public int getSecurityAlertsCount() throws DatabaseException {
        return securityLogDAO.countAlerts();
    }
}
