package apigen.adt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import apigen.adt.api.ADTFactory;
import apigen.adt.api.Entries;
import apigen.gen.GenerationParameters;
import aterm.ATermList;
import aterm.ParseError;

public class ADTReader {

	public static ADT readADT(GenerationParameters params) {
		Iterator iter = params.getInputFiles().iterator();
		String fileName = "";
		try {
			ADTFactory factory = new ADTFactory();
			ATermList all = factory.getEmpty();
			while (iter.hasNext()) {
				fileName = (String) iter.next();
				FileInputStream fis = new FileInputStream(fileName);
				all = all.concat((ATermList) factory.readFromFile(fis));
			}
			Entries entries = factory.EntriesFromTerm(all);
			return new ADT(entries);
		}
		catch (FileNotFoundException e) {
			System.err.println("Error: File not found: " + fileName);
		}
		catch (IOException e) {
			System.err.println("Error: Could not read ADT from input: " + fileName);
		}
		catch (ParseError e) {
			System.err.println("Error: A parse error occurred in the ADT file:" + e);
		}
        catch (ADTException e) {
            System.err.println("Error: ");
            System.err.println(e);
        }
		
		return null;
	}
}
