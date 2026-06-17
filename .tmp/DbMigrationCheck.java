import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class DbMigrationCheck {
    public static void main(String[] args) throws Exception {
        String url = args[0];
        String user = args[1];
        String password = args[2];
        String sqlPath = args[3];

        String sql = Files.readString(Path.of(sqlPath));
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(true);
            try (Statement st = conn.createStatement()) {
                st.execute(sql);
            }

            System.out.println("MIGRATION_APPLIED=OK");

            try (PreparedStatement ps = conn.prepareStatement(
                    """
                    SELECT column_name
                    FROM information_schema.columns
                    WHERE table_name = 'proyectos'
                      AND column_name IN ('responsable_anterior_id', 'responsable_anterior_nombre')
                    ORDER BY column_name
                    """)) {
                try (ResultSet rs = ps.executeQuery()) {
                    List<String> columns = new ArrayList<>();
                    while (rs.next()) {
                        columns.add(rs.getString(1));
                    }
                    System.out.println("COLUMNS=" + String.join(",", columns));
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM proyectos")) {
                rs.next();
                System.out.println("PROJECT_COUNT=" + rs.getInt(1));
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM usuarios")) {
                rs.next();
                System.out.println("USER_COUNT=" + rs.getInt(1));
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                     """
                     SELECT p.id, p.responsable_id, COALESCE(p.responsable_nombre, u.nombre || ' ' || u.apellido) AS responsable_nombre
                     FROM proyectos p
                     LEFT JOIN usuarios u ON u.id = p.responsable_id
                     ORDER BY p.id
                     LIMIT 3
                     """)) {
                int idx = 0;
                while (rs.next()) {
                    idx++;
                    System.out.println("PROJECT_SAMPLE_" + idx + "=" + rs.getLong("id") + "|" + rs.getLong("responsable_id") + "|" + Objects.toString(rs.getString("responsable_nombre"), ""));
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                     """
                     SELECT id, COALESCE(nombre || ' ' || apellido, username) AS nombre
                     FROM usuarios
                     WHERE activo = true
                     ORDER BY id
                     LIMIT 5
                     """)) {
                int idx = 0;
                while (rs.next()) {
                    idx++;
                    System.out.println("USER_SAMPLE_" + idx + "=" + rs.getLong("id") + "|" + Objects.toString(rs.getString("nombre"), ""));
                }
            }
        }
    }
}
