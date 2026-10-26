
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

@WebServlet("/tasks")
public class TaskServlet extends HttpServlet {

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

        String sql =
            "SELECT id, title, category, priority, is_completed " +
            "FROM tasks ORDER BY is_completed ASC, id DESC";

        StringBuilder json = new StringBuilder("[");
        boolean first = true;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                if (!first) {
                    json.append(",");
                }

                json.append("{\"id\":").append(rs.getInt("id"))
                    .append(",\"title\":\"")
                    .append(escape(rs.getString("title")))
                    .append("\",\"category\":\"")
                    .append(escape(rs.getString("category") == null
                        ? "General" : rs.getString("category")))
                    .append("\",\"priority\":\"")
                    .append(escape(rs.getString("priority")))
                    .append("\",\"completed\":")
                    .append(rs.getBoolean("is_completed"))
                    .append("}");

                first = false;
            }

            json.append("]");
            response.getWriter().print(json.toString());

        } catch (Exception e) {
            log("Could not load tasks", e);
            response.sendError(500, "Could not load tasks");
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

                String title = request.getParameter("title");
                String category = request.getParameter("category");
                String priority = request.getParameter("priority");

                if (title == null || title.trim().isEmpty()
                        || title.trim().length() > 200) {
                    response.sendError(400, "Enter a valid task");
                    return;
                }

                if (category == null || category.trim().isEmpty()) {
                    category = "General";
                }

                if (category.length() > 100) {
                    response.sendError(400, "Category is too long");
                    return;
                }

                if (!"High".equals(priority)
                        && !"Medium".equals(priority)
                        && !"Low".equals(priority)) {
                    priority = "Medium";
                }

                String sql =
                    "INSERT INTO tasks " +
                    "(title, category, priority) VALUES (?, ?, ?)";

                try (PreparedStatement ps =
                        con.prepareStatement(sql)) {
                    ps.setString(1, title.trim());
                    ps.setString(2, category.trim());
                    ps.setString(3, priority);
                    ps.executeUpdate();
                }

                response.getWriter().print("OK");

            } else if ("toggle".equals(action)) {

                int id = Integer.parseInt(request.getParameter("id"));
                boolean completed =
                    Boolean.parseBoolean(request.getParameter("completed"));

                String sql =
                    "UPDATE tasks SET is_completed = ? WHERE id = ?";

                try (PreparedStatement ps =
                        con.prepareStatement(sql)) {
                    ps.setBoolean(1, completed);
                    ps.setInt(2, id);

                    if (ps.executeUpdate() == 0) {
                        response.sendError(404, "Task not found");
                        return;
                    }
                }

                response.getWriter().print("OK");

            } else if ("delete".equals(action)) {

                int id = Integer.parseInt(request.getParameter("id"));

                String sql = "DELETE FROM tasks WHERE id = ?";

                try (PreparedStatement ps =
                        con.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }

                response.getWriter().print("OK");

            } else {
                response.sendError(400, "Invalid action");
            }

        } catch (Exception e) {
            log("Could not update tasks", e);
            response.sendError(500, "Could not update task");
        }
    }
}
