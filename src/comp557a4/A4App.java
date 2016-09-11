/** Neil Edelman -- 110121860 */

package comp557a4;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * Provided code for assignment 4
 */
public class A4App {

	public static final String directory = "a4data/";

	/**
	 * entry point for application
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String fn;
		if(args.length > 2) fn = args[1];
		else fn = "pandorica.xml"/*/"foo.xml"*/;
		try {
			System.out.print("Loading "+fn+"\n");
			InputStream inputStream = new FileInputStream(new File(directory + fn));
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(inputStream);
			Scene scene = new Scene(document.getDocumentElement());
			scene.render();
			System.out.print("Done.\n");
		} catch (Exception e) {
			throw new RuntimeException("Failed!", e);
		}
	}
}
