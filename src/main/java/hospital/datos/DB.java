package hospital.datos;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DB {
    private static HikariDataSource ds;

    static {
        try(InputStream in = DB.class.getClassLoader().getResourceAsStream("db.properties")) {
          Properties p = new Properties();
          p.load(in);

          HikariConfig cfg = new HikariConfig();
          cfg.setJdbcUrl(p.getProperty("db.url"));
            cfg.setUsername(p.getProperty("db.user"));
            cfg.setPassword(p.getProperty("db.password"));
            cfg.setMaximumPoolSize(Integer.parseInt(p.getProperty("db.pool.size", "10")));
            cfg.setPoolName("MyAppPool");

            cfg.setMinimumIdle(2);
            cfg.setConnectionTimeout(10000);
            cfg.setIdleTimeout(600000);
            cfg.setMaxLifetime(1800000);

            ds = new HikariDataSource(cfg);
        }
        catch (Exception e) {
            throw new RuntimeException("No se pudo iniciar el pool de conexiones: " + e.getMessage(), e);
        }
    }
    private DB(){}

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
