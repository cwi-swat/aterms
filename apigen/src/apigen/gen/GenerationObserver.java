package apigen.gen;

public interface GenerationObserver {
	public void fileCreated(String directory, String fileName, String extension);
}
