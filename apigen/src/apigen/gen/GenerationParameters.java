package apigen.gen;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import apigen.adt.ADT;
import apigen.adt.Type;
import apigen.adt.api.types.Module;

public class GenerationParameters {
	private String outputDirectory;
	private String apiName;
	private String prefix;
	private boolean verbose;
	private List<String> inputFiles;

	public GenerationParameters() {
		inputFiles = new LinkedList<String>();
		setApiName("");
	}

	public void parseArguments(List<String> args) {
		Iterator<String> iter = args.iterator();
		while (iter.hasNext()) {
			String arg = iter.next();
			if (arg.startsWith("--input") || arg.startsWith("-i")) {
				shift(iter);
				addInputFile(shiftArgument(iter));
			} else if (arg.startsWith("--output") || arg.startsWith("-o")) {
				shift(iter);
				setOutputDirectory(shiftArgument(iter));
			} else if (arg.startsWith("--name") || arg.startsWith("-n")) {
				shift(iter);
				setApiName(shiftArgument(iter));
			} else if (arg.startsWith("--verbose") || arg.startsWith("-v")) {
				shift(iter);
				setVerbose(true);
			} else {
				throw new IllegalArgumentException("unknown argument: " + arg
						+ " of " + args);
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

	protected String shiftArgument(Iterator<String> args) {
		String result = args.next();
		shift(args);
		return result;
	}

	protected void shift(Iterator<String> args) {
		args.remove();
	}

	public List<String> getInputFiles() {
		return inputFiles;
	}

	public void addInputFile(String fileName) {
		inputFiles.add(fileName);
	}

	public final String getApiName() {
		return apiName;
	}

	public final String getApiExtName(Module m) {
		if (apiName.equals("")) {
			return m.getModulename().getName();
		}
		return apiName;
	}

	public final String getApiExtName(String moduleName) {
		if (apiName.equals("")) {
			return moduleName;
		}
		return apiName;
	}

	public final String getApiExtName(Type type) {
		if (apiName.equals("")) {
			return type.getModuleName();
		}
		return apiName;
	}

	public final String getApiExtName(TypeConverter conv, String typename) {
		if (apiName.equals("")) {
			return ADT.getInstance().getModuleName(conv, typename);
		}
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
		/*
		 * if (getApiName() == null) { throw new IllegalArgumentException("No
		 * API name specified"); }
		 */
		// No more to be tested since it is either "",
		// apiName given in argument or computed with type arg
	}
}
