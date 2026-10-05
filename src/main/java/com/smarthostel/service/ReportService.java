package com.smarthostel.service;

import com.smarthostel.dao.AttendanceDAO;
import com.smarthostel.dao.ComplaintDAO;
import com.smarthostel.dao.FeeDAO;
import com.smarthostel.dao.LeaveRequestDAO;
import com.smarthostel.dao.RoomDAO;
import com.smarthostel.dao.SecurityLogDAO;
import com.smarthostel.dao.StudentDAO;
import com.smarthostel.dao.VisitorDAO;
import com.smarthostel.dao.WardenDAO;
import com.smarthostel.exception.DatabaseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service aggregating system-wide analytics, statistical summaries, and reporting data.
 */
public class ReportService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final WardenDAO wardenDAO = new WardenDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final FeeDAO feeDAO = new FeeDAO();
    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final VisitorDAO visitorDAO = new VisitorDAO();
    private final LeaveRequestDAO leaveRequestDAO = new LeaveRequestDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final SecurityLogDAO securityLogDAO = new SecurityLogDAO();

    public Map<String, Object> getDashboardMetrics() throws DatabaseException {
        Map<String, Object> metrics = new LinkedHashMap<>();

        int totalStudents = studentDAO.count();
        int totalWardens = wardenDAO.count();
        int totalRooms = roomDAO.countTotalRooms();
        int totalBeds = roomDAO.countTotalBeds();
        int occupiedBeds = roomDAO.countOccupiedBeds();
        double occupancyPct = totalBeds > 0 ? ((double) occupiedBeds / totalBeds) * 100.0 : 0.0;

        double collectedFees = feeDAO.getTotalCollectedFees();
        double pendingFees = feeDAO.getTotalPendingFees();
        int pendingComplaints = complaintDAO.countPending();
        int pendingVisitors = visitorDAO.countPending();
        int pendingLeaves = leaveRequestDAO.countPending();

        String today = LocalDate.now().toString();
        int todayPresent = attendanceDAO.countTodayPresent(today);
        int todayLate = attendanceDAO.countTodayLate(today);
        int securityAlerts = securityLogDAO.countAlerts();

        metrics.put("totalStudents", totalStudents);
        metrics.put("totalWardens", totalWardens);
        metrics.put("totalRooms", totalRooms);
        metrics.put("totalBeds", totalBeds);
        metrics.put("occupiedBeds", occupiedBeds);
        metrics.put("occupancyPercentage", Math.round(occupancyPct * 10.0) / 10.0);
        metrics.put("collectedFees", collectedFees);
        metrics.put("pendingFees", pendingFees);
        metrics.put("pendingComplaints", pendingComplaints);
        metrics.put("pendingVisitors", pendingVisitors);
        metrics.put("pendingLeaves", pendingLeaves);
        metrics.put("todayPresent", todayPresent);
        metrics.put("todayLate", todayLate);
        metrics.put("securityAlerts", securityAlerts);

        return metrics;
    }

    public String generateSummaryReportText() throws DatabaseException {
        Map<String, Object> m = getDashboardMetrics();
        StringBuilder sb = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        sb.append("=====================================================\n");
        sb.append("       SMART HOSTEL MANAGEMENT SYSTEM REPORT         \n");
        sb.append("          Generated At: ").append(timestamp).append("\n");
        sb.append("=====================================================\n\n");

        sb.append("1. CAPACITY & RESIDENCY\n");
        sb.append("   --------------------\n");
        sb.append("   Total Active Students : ").append(m.get("totalStudents")).append("\n");
        sb.append("   Total Wardens On Duty : ").append(m.get("totalWardens")).append("\n");
        sb.append("   Total Hostel Rooms    : ").append(m.get("totalRooms")).append("\n");
        sb.append("   Total Bed Capacity    : ").append(m.get("totalBeds")).append("\n");
        sb.append("   Occupied Beds         : ").append(m.get("occupiedBeds")).append("\n");
        sb.append("   Occupancy Rate        : ").append(m.get("occupancyPercentage")).append("%\n\n");

        sb.append("2. FINANCIAL OVERVIEW\n");
        sb.append("   ------------------\n");
        sb.append(String.format("   Total Fees Collected  : ₹ %,.2f%n", (Double) m.get("collectedFees")));
        sb.append(String.format("   Total Pending Dues    : ₹ %,.2f%n", (Double) m.get("pendingFees")));
        sb.append("\n");

        sb.append("3. OPERATIONS & GRIEVANCES\n");
        sb.append("   -----------------------\n");
        sb.append("   Pending Complaints    : ").append(m.get("pendingComplaints")).append("\n");
        sb.append("   Pending Visitor Passes: ").append(m.get("pendingVisitors")).append("\n");
        sb.append("   Pending Leave Requests: ").append(m.get("pendingLeaves")).append("\n\n");

        sb.append("4. SECURITY & ATTENDANCE\n");
        sb.append("   ---------------------\n");
        sb.append("   Today Students Present: ").append(m.get("todayPresent")).append("\n");
        sb.append("   Late Night Returns    : ").append(m.get("todayLate")).append("\n");
        sb.append("   Security Curfew Alerts: ").append(m.get("securityAlerts")).append("\n\n");

        sb.append("=====================================================\n");
        sb.append("                  END OF REPORT                      \n");
        sb.append("=====================================================\n");

        return sb.toString();
    }
}
