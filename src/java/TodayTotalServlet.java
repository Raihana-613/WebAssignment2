
import com.focusflow.db.DBConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/todayTotal")
public class TodayTotalServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String sql =
            "SELECT COALESCE(SUM(duration_minutes), 0) " +
            "FROM focus_sessions WHERE session_date = CURDATE()";

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            response.getWriter().print(rs.getInt(1));

        } catch (Exception e) {
            log("Could not retrieve today's total", e);
            response.sendError(500, "Could not retrieve total");
        }
    }
}
