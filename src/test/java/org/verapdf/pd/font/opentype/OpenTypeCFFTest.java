package org.verapdf.pd.font.opentype;

import org.junit.Test;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.io.InternalInputStream;
import org.verapdf.pd.font.cff.CFFFontProgram;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * @author Sergey Shemyakov
 */
public class OpenTypeCFFTest {

    private String fontFilePath = "src/test/resources/org/verapdf/font/opentype/ShortStack-Regular.otf";

    @Test
    public void test() throws IOException {
        ASInputStream stream = new InternalInputStream(fontFilePath);
        OpenTypeFontProgram font = new OpenTypeFontProgram(stream, true, false, null);
        font.parseFont();
        assertTrue(font.getFont() instanceof CFFFontProgram);
        assertTrue(!((CFFFontProgram) font.getFont()).isCIDFont());
    }

}
