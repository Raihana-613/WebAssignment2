
import com.focusflow.db.DBConnection;
import java.io.IOException;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/testdb")
public class TestDBServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");

        try (Connection con = DBConnection.getConnection()) {
            response.getWriter().println(
                "SUCCESS! MySQL database is connected."
            );
        } catch (Exception e) {
            log("Database connection failed", e);
            response.setStatus(500);
            response.getWriter().println(
                "Connection failed: " + e.getMessage()
            );
        }
    }
}
