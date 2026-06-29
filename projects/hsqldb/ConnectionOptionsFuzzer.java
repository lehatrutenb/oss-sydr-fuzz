import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConnectionOptionsFuzzer extends TestServer {

	ConnectionOptionsFuzzer(boolean verbose) {
		super(verbose);
	}

	void testOneInput(String fuzzyString) {
		try {
			getConnection(fuzzyString);
		} catch (SQLException ex) {
			/* ignore */
		} catch (IllegalArgumentException ex) {
			/* ??? */
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
		try (TestServer fuzzer = new ConnectionOptionsFuzzer(false)) {
			fuzzer.testOneInput(input);
		}
	}

	public static void inner_func() {

	}
}
