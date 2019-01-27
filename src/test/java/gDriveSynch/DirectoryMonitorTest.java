package gDriveSynch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class DirectoryMonitorTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	DirectoryMonitor sut;

	// fragile monitoring methods
	@Test
	public void test() throws Exception {
		File dir = temporaryFolder.newFolder();
		sut = new DirectoryMonitor(dir);

		sut.init();

		assertThat("none events find", sut.poll(), emptyIterable());

		File file = new File(dir, "test");
		writeOnFile(file, "asd");
		List<WatchEvent<?>> events = sut.poll();
		events.forEach(e ->{
			@SuppressWarnings("unchecked")
			WatchEvent<Path> ev = (WatchEvent<Path>) e;
			System.out.println(e.kind() + " " + ev.context().toAbsolutePath().toString());
		});
		assertThat("creation event", events, hasSize(1));
	}

	private File writeOnFile(File file, String content) throws IOException {
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(content);
		}
		return file;
	}
}
