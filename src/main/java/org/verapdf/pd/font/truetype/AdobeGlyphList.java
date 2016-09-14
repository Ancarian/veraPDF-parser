package org.verapdf.pd.font.truetype;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Class provides methods to work with Adobe Glyph List.
 *
 * @author Sergey Shemyakov
 */
public class AdobeGlyphList {

    private static final Logger LOGGER = Logger.getLogger(AdobeGlyphList.class);

    private static final Map<String, AGLUnicode> MAPPING =
            new HashMap<>();
    private static final String AGL_FILE = "/font/AdobeGlyphList.txt";
    private static final AGLUnicode EMPTY = new AGLUnicode(-1);

    static {
        try {
            File aglFile;
            URL res = AdobeGlyphList.class.getResource(AGL_FILE);
            if (res.toString().startsWith("jar:")) {
                InputStream input = AdobeGlyphList.class.getResourceAsStream(AGL_FILE);
                aglFile = File.createTempFile("tempfile", ".tmp");
                OutputStream out = new FileOutputStream(aglFile);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                aglFile.deleteOnExit();
            } else {
                aglFile = new File(res.getFile());
            }

            if (!aglFile.exists()) {
                throw new IOException("Error: File " + aglFile + " not found!");
            }

            RandomAccessFile stream = new RandomAccessFile(aglFile, "r");
            String line;
            line = stream.readLine();
            do {
                String[] words = line.split(" ");
                int symbolCode = Integer.parseInt(words[1], 16);
                if (words.length == 2) {
                    MAPPING.put(words[0], new AGLUnicode(symbolCode));
                    line = stream.readLine();
                    continue;
                } else {
                    int[] diacritic = new int[words.length - 2];
                    for (int i = 0; i < diacritic.length; ++i) {
                        diacritic[i] = Integer.parseInt(words[i + 2], 16);
                    }
                    MAPPING.put(words[0], new AGLUnicode(symbolCode, diacritic));
                }
                line = stream.readLine();
            } while (line != null);
        } catch (IOException e) {
            LOGGER.debug("Error in opening Adobe Glyph List file", e);
        }
    }

    /**
     * Returns AGLUnicode for glyph with specified name according to Adobe Glyph
     * List.
     *
     * @param glyphName is name of required glyph.
     * @return code for requested glyph.
     */
    public static AGLUnicode get(String glyphName) {
        AGLUnicode res = MAPPING.get(glyphName);
        if (res == null) {
            LOGGER.debug("Cannot find glyph " + glyphName + " in Adobe Glyph List");
            return EMPTY;
        }
        return res;
    }

    /**
     * Checks if Adobe Glyph List contains given glyph.
     *
     * @param glyphName is name of glyph to check.
     * @return true if this glyph is contained in Adobe Glyph List.
     */
    public static boolean contains(String glyphName) {
        return MAPPING.containsKey(glyphName);
    }

    /**
     * This class represents entity into which Adobe Glyph List maps glyph names,
     * i. a. it contains either Unicode of symbol or Unicode of symbol and
     * Unicode of diacritic symbol.
     */
    public static class AGLUnicode {
        private int symbolCode;
        private int[] diacriticCodes;

        AGLUnicode(int symbolCode, int... diacriticCode) {
            this.symbolCode = symbolCode;
            this.diacriticCodes = diacriticCode;
        }

        AGLUnicode(int symbolCode) {
            this.symbolCode = symbolCode;
            this.diacriticCodes = new int[0];
        }

        /**
         * @return Unicode of symbol that is not diacritic.
         */
        public int getSymbolCode() {
            return symbolCode;
        }

        /**
         * @return array of Unicode for all diacritic glyphs.
         */
        public int[] getDiacriticCodes() {
            return diacriticCodes;
        }

        /**
         * @return true if this character has diacritic symbols.
         */
        public boolean hasDiacritic() {
            return this.diacriticCodes.length != 0;
        }
    }
}
