
import com.focusflow.db.DBConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/saveSession")
public class SaveSessionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String sql = "INSERT INTO focus_sessions " +
                     "(session_date, duration_minutes) " +
                     "VALUES (CURDATE(), 25)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.executeUpdate();

            response.setContentType("text/plain");
            response.getWriter().print("OK");

        } catch (Exception e) {
            log("Could not save focus session", e);
            response.sendError(500, "Could not save session");
        }
    }
}
