package apigen.adt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import apigen.adt.api.Factory;
import apigen.adt.api.types.Modules;
import apigen.gen.GenerationParameters;
import aterm.ParseError;
import aterm.pure.PureFactory;

public class ADTReader {

	public static ADT readADT(GenerationParameters params) {
		Iterator iter = params.getInputFiles().iterator();
		String fileName = "";
		try {
			Factory factory = new Factory(new PureFactory());
			Modules all = factory.makeModules();
			//Entries all = factory.makeEntries();
			while (iter.hasNext()) {
				fileName = (String) iter.next();
				FileInputStream fis = new FileInputStream(fileName);
				try {
					all = all.concat(factory.ModulesFromFile(fis));
				} catch (IllegalArgumentException ex) {
					fis.close();
          if (params.getApiName() == null) {
            throw new IllegalArgumentException("No API name specified");
          }
          fis = new FileInputStream(fileName);
					
          all = factory.makeModules(
                                    factory.makeModule_Modulentry(
									factory.makeModuleName_Name(""),
									factory.makeImports(),
									factory.EntriesFromFile(fis)));
				
				}
			}
			return ADT.initialize(all);
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
