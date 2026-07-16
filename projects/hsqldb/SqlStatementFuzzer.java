import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SqlStatementFuzzer extends TestServer {

	SqlStatementFuzzer(boolean verbose) {
		super(verbose);
	}

	void testOneInput(String fuzzyString) {
		try {
			getConnection().createStatement().execute(fuzzyString);
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
		if (input == null || input.isEmpty()) {
			return;
		}
		try (TestServer fuzzer = new SqlStatementFuzzer(false)) {
			fuzzer.testOneInput(input);
		}
	}

	public static void inner_func() {

	}
}
