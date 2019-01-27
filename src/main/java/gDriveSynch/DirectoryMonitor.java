package gDriveSynch;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

public class DirectoryMonitor {
	
	Path dir;
	WatchKey key;
	
	DirectoryMonitor (File dir){
		if(!dir.isDirectory()){
			new IllegalArgumentException(dir.getAbsolutePath() + "is not a directory");
		}
		this.dir = dir.toPath();
	}
	
	public void init() throws IOException {
		WatchService wService = FileSystems.getDefault().newWatchService();
		key = dir.register(wService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
	}
	
	public List<WatchEvent<?>> poll() {
		return key.pollEvents();
	}
	
	public void reset() {
		key.reset();
	}
	
	public Path getDir() {
		return dir;
	}

	public void setDir(Path dir) {
		this.dir = dir;
	}

}
