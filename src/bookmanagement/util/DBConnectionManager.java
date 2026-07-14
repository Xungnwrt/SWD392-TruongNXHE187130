package bookmanagement.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton — manages SQL Server JDBC connections.
 */
public final class DBConnectionManager {

	private static DBConnectionManager instance;

	private final String url;
	private final String username;
	private final String password;
	private final boolean integratedSecurity;

	private DBConnectionManager() {
		Properties props = loadProperties();
		this.url = props.getProperty("db.url");
		this.username = props.getProperty("db.username", "").trim();
		this.password = props.getProperty("db.password", "");
		this.integratedSecurity = Boolean.parseBoolean(props.getProperty("db.integratedSecurity", "false"))
				|| url.toLowerCase().contains("integratedsecurity=true");
		try {
			Class.forName(props.getProperty("db.driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver"));
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(
					"SQL Server JDBC driver not found. Place mssql-jdbc jar in lib/ folder.", e);
		}
	}

	public static synchronized DBConnectionManager getInstance() {
		if (instance == null) {
			instance = new DBConnectionManager();
		}
		return instance;
	}

	public Connection getConnection() throws SQLException {
		// Windows Authentication: do not pass username/password
		if (integratedSecurity || username.isEmpty()) {
			return DriverManager.getConnection(url);
		}
		return DriverManager.getConnection(url, username, password);
	}

	public void closeQuietly(AutoCloseable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception ignored) {
				// intentionally ignored
			}
		}
	}

	private static Properties loadProperties() {
		Properties props = new Properties();
		String[] candidates = {
				"resources/db.properties",
				"db.properties",
				"../resources/db.properties"
		};
		for (String candidate : candidates) {
			Path path = Paths.get(candidate);
			if (Files.exists(path)) {
				try (InputStream in = Files.newInputStream(path)) {
					props.load(in);
					return props;
				} catch (IOException e) {
					throw new IllegalStateException("Cannot read " + candidate, e);
				}
			}
		}
		try (InputStream in = DBConnectionManager.class.getClassLoader().getResourceAsStream("db.properties")) {
			if (in != null) {
				props.load(in);
				return props;
			}
		} catch (IOException e) {
			throw new IllegalStateException("Cannot read classpath db.properties", e);
		}
		throw new IllegalStateException(
				"db.properties not found. Create resources/db.properties with SQL Server settings.");
	}
}
