package apigen.gen;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GenerationParameters {
	private String outputDirectory;
	private String apiName;
	private String prefix;
	private boolean verbose;
	private List inputFiles;

	public GenerationParameters() {
		inputFiles = new LinkedList();
	}

	public void parseArguments(List args) {
		Iterator iter = args.iterator();
		while (iter.hasNext()) {
			String arg = (String) iter.next();
			if ("--input".startsWith(arg) || "-i".startsWith(arg)) {
				shift(iter);
				addInputFile(shiftArgument(iter));
			}
			else if ("--output".startsWith(arg) || "-o".startsWith(arg)) {
				shift(iter);
				setOutputDirectory(shiftArgument(iter));
			}
			else if ("--name".startsWith(arg) || "-n".startsWith(arg)) {
				shift(iter);
				setApiName(shiftArgument(iter));
			}
			else if ("--verbose".startsWith(arg) || "-v".startsWith(arg)) {
				shift(iter);
				setVerbose(true);
			}
			else {
				throw new IllegalArgumentException("unknown argument: " + arg);
			}
		}
		if (args.size() != 0) {
			throw new IllegalArgumentException("unhandled parameters: " + args);
		}
	}
	
	public String usage() {
		StringBuffer buf = new StringBuffer();
		buf.append("\t-i | --input <in>              <multiple allowed>\n");
		buf.append("\t-o | --output <outputdir>      [\".\"]\n");
		buf.append("\t-n | --name <api name>         <obligatory>\n");
		buf.append("\t-v | --verbose                 [off]\n");
		return buf.toString();
	}

	protected String shiftArgument(Iterator args) {
		String result = (String) args.next();
		shift(args);
		return result;
	}

	protected void shift(Iterator args) {
		args.remove();
	}

	public List getInputFiles() {
		return inputFiles;
	}

	public void addInputFile(String fileName) {
		inputFiles.add(fileName);
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void check() {
		if (getApiName() == null) {
			throw new IllegalArgumentException("No API name specified");
		}
	}
}
