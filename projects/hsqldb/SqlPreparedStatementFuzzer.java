import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SqlPreparedStatementFuzzer extends TestServer {

	SqlPreparedStatementFuzzer(boolean verbose) {
		super(verbose);
	}

	void testOneInput(String fuzzyString) {
		try (Connection connection = getConnection()) {
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE TestTable SET value=? WHERE key=1");
			preparedStatement.setString(1, fuzzyString);
			preparedStatement.executeUpdate();
		} catch (SQLException ex) {
			/* ignore */
		}
	}

	public static void main(String[] args) {
		try {
			fuzzerTestOneInput(Files.readString(Path.of(args[0])));
		} catch (IOException e) {
			return;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void fuzzerTestOneInput(String input) throws Exception {
		try (TestServer fuzzer = new SqlPreparedStatementFuzzer(false)) {
			fuzzer.testOneInput(input);
		}
	}

	public static void inner_func() {

	}
}
