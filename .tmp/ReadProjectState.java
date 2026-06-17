import java.sql.*;

public class ReadProjectState {
    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(args[0], args[1], args[2]);
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT id, responsable_id, responsable_nombre, responsable_anterior_id, responsable_anterior_nombre FROM proyectos WHERE id = ?")) {
            ps.setLong(1, Long.parseLong(args[3]));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("NOT_FOUND");
                    return;
                }
                System.out.println("ID=" + rs.getLong("id"));
                System.out.println("RESPONSABLE_ID=" + rs.getLong("responsable_id"));
                System.out.println("RESPONSABLE_NOMBRE=" + String.valueOf(rs.getString("responsable_nombre")));
                System.out.println("RESPONSABLE_ANTERIOR_ID=" + String.valueOf(rs.getObject("responsable_anterior_id")));
                System.out.println("RESPONSABLE_ANTERIOR_NOMBRE=" + String.valueOf(rs.getString("responsable_anterior_nombre")));
            }
        }
    }
}
