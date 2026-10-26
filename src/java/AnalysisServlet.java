
import com.focusflow.db.DBConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.YearMonth;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/monthlyData")
public class AnalysisServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        YearMonth month = YearMonth.now();
        int days = month.lengthOfMonth();
        int[] totals = new int[days];

        String sql =
            "SELECT DAY(session_date), SUM(duration_minutes) " +
            "FROM focus_sessions " +
            "WHERE YEAR(session_date) = ? " +
            "AND MONTH(session_date) = ? " +
            "GROUP BY DAY(session_date)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, month.getYear());
            ps.setInt(2, month.getMonthValue());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int day = rs.getInt(1);
                    totals[day - 1] = rs.getInt(2);
                }
            }

            StringBuilder json = new StringBuilder();
            json.append("{\"labels\":[");

            for (int i = 1; i <= days; i++) {
                if (i > 1) {
                    json.append(",");
                }
                json.append("\"").append(i).append("\"");
            }

            json.append("],\"hours\":[");

            for (int i = 0; i < days; i++) {
                if (i > 0) {
                    json.append(",");
                }
                json.append(String.format(
                    java.util.Locale.US, "%.2f",
                    totals[i] / 60.0
                ));
            }

            json.append("]}");

            response.getWriter().print(json.toString());

        } catch (Exception e) {
            log("Could not load monthly productivity data", e);
            response.sendError(500, "Could not load chart data");
        }
    }
}
