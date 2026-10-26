
import com.focusflow.db.DBConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/habits")
public class HabitServlet extends HttpServlet {

    private String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        YearMonth month = YearMonth.now();

        try {
            String requestedMonth = request.getParameter("month");
            if (requestedMonth != null &&
                    requestedMonth.matches("\\d{4}-\\d{2}")) {
                month = YearMonth.parse(requestedMonth);
            }
        } catch (Exception e) {
            response.sendError(400, "Invalid month");
            return;
        }

        StringBuilder json = new StringBuilder();

        json.append("{\"year\":").append(month.getYear())
            .append(",\"month\":").append(month.getMonthValue())
            .append(",\"days\":").append(month.lengthOfMonth())
            .append(",\"habits\":[");

        String habitSql =
            "SELECT id, habit_name FROM habits " +
            "WHERE is_active = TRUE ORDER BY id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(habitSql);
             ResultSet rs = ps.executeQuery()) {

            boolean first = true;

            while (rs.next()) {
                if (!first) {
                    json.append(",");
                }

                json.append("{\"id\":").append(rs.getInt("id"))
                    .append(",\"name\":\"")
                    .append(escape(rs.getString("habit_name")))
                    .append("\"}");

                first = false;
            }

            json.append("],\"logs\":[");

            String logSql =
                "SELECT habit_id, habit_date, status " +
                "FROM habit_logs WHERE habit_date >= ? " +
                "AND habit_date < ?";

            try (PreparedStatement logPs =
                    con.prepareStatement(logSql)) {

                logPs.setDate(1, Date.valueOf(
                    month.atDay(1)));
                logPs.setDate(2, Date.valueOf(
                    month.plusMonths(1).atDay(1)));

                try (ResultSet logRs = logPs.executeQuery()) {
                    first = true;

                    while (logRs.next()) {
                        if (!first) {
                            json.append(",");
                        }

                        json.append("{\"habitId\":")
                            .append(logRs.getInt("habit_id"))
                            .append(",\"date\":\"")
                            .append(logRs.getDate("habit_date"))
                            .append("\",\"status\":\"")
                            .append(logRs.getString("status"))
                            .append("\"}");

                        first = false;
                    }
                }
            }

            json.append("]}");
            response.getWriter().print(json.toString());

        } catch (Exception e) {
            log("Could not load habits", e);
            response.sendError(500, "Could not load habits");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        try (Connection con = DBConnection.getConnection()) {

            if ("add".equals(action)) {
                String name = request.getParameter("habitName");

                if (name == null || name.trim().isEmpty()
                        || name.trim().length() > 100) {
                    response.sendError(400, "Enter a habit name");
                    return;
                }

                String sql =
                    "INSERT INTO habits (habit_name) VALUES (?)";

                try (PreparedStatement ps =
                        con.prepareStatement(sql)) {
                    ps.setString(1, name.trim());
                    ps.executeUpdate();
                }

                response.getWriter().print("OK");

            } else if ("update".equals(action)) {

                int habitId = Integer.parseInt(
                    request.getParameter("habitId"));

                LocalDate date = LocalDate.parse(
                    request.getParameter("date"));

                String status = request.getParameter("status");

                if (!"PENDING".equals(status)
                        && !"DONE".equals(status)
                        && !"MISSED".equals(status)) {
                    response.sendError(400, "Invalid status");
                    return;
                }

                String sql =
                    "INSERT INTO habit_logs " +
                    "(habit_id, habit_date, status) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE status = ?";

                try (PreparedStatement ps =
                        con.prepareStatement(sql)) {
                    ps.setInt(1, habitId);
                    ps.setDate(2, Date.valueOf(date));
                    ps.setString(3, status);
                    ps.setString(4, status);
                    ps.executeUpdate();
                }

                response.getWriter().print("OK");

            } else {
                response.sendError(400, "Invalid action");
            }

        } catch (Exception e) {
            log("Could not update habit", e);
            response.sendError(500, "Could not update habit");
        }
    }
}
