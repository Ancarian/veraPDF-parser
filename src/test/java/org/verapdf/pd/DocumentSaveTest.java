package org.verapdf.pd;

import org.junit.Test;

/**
 * @author Timur Kamalov
 */
public class DocumentSaveTest {

	public static final String FILE_NAME = "002731.pdf";

	@Test
	public void test() throws Exception {
		PDDocument document = new PDDocument("/home/shemyakovsergey/PDFs/signed.pdf");
		document.saveAs("/home/shemyakovsergey/SmallAndValid_saved.pdf");
		System.out.println("Document saved");
	}

}
