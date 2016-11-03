package org.verapdf.pd.font.cff;

import org.verapdf.io.SeekableStream;
import org.verapdf.pd.font.CFFNumber;
import org.verapdf.pd.font.Encoding;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.pd.font.cmap.CMap;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Instance of this class represent a Type1 font from FontSet of
 * CFF file.
 *
 * @author Sergey Shemyakov
 */
public class CFFType1FontProgram extends CFFFontBaseParser implements FontProgram {

    private static final String NOTDEF_STRING = ".notdef";

    private Encoding pdEncoding;    // mapping code -> glyphName
    private CMap externalCMap;  // in case if font is located in
    private long encodingOffset;
    private int[] encoding;     // array with mapping code -> gid
    private boolean isStandardEncoding = false;
    private boolean isExpertEncoding = false;
    private Map<String, Integer> charSet;   // mappings glyphName -> gid
    private Map<Integer, String> inverseCharSet;    // mappings gid -> glyph name
    private String[] encodingStrings;

    CFFType1FontProgram(SeekableStream stream, CFFIndex definedNames, CFFIndex globalSubrs,
                        long topDictBeginOffset, long topDictEndOffset,
                        Encoding pdEncoding, CMap externalCMap) {
        super(stream);
        encodingOffset = 0;
        encoding = new int[256];
        this.definedNames = definedNames;
        this.globalSubrs = globalSubrs;
        this.topDictBeginOffset = topDictBeginOffset;
        this.topDictEndOffset = topDictEndOffset;
        this.pdEncoding = pdEncoding;
        this.externalCMap = externalCMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parseFont() throws IOException {
        this.source.seek(topDictBeginOffset);
        while (this.source.getOffset() < topDictEndOffset) {
            readTopDictUnit();
        }
        this.stack.clear();

        this.source.seek(this.privateDictOffset);
        while (this.source.getOffset() < this.privateDictOffset + this.privateDictSize) {
            this.readPrivateDictUnit();
        }
        this.readLocalSubrsAndBias();

        this.source.seek(charStringsOffset);
        this.readCharStrings();

        this.source.seek(encodingOffset);
        this.readEncoding();

        this.source.seek(charSetOffset);
        this.readCharSet();

        this.readWidths();
    }

    @Override
    protected void readTopDictOneByteOps(int lastRead) {
        switch (lastRead) {
            case 16:    // encoding
                this.encodingOffset = stack.get(stack.size() - 1).getInteger();
                this.stack.clear();
                break;
            default:
                this.stack.clear();
        }
    }

    private void readEncoding() throws IOException {
        if (encodingOffset == 0) {
            this.isStandardEncoding = true;
        } else if (encodingOffset == 1) {
            this.isExpertEncoding = true;
        } else {
            int format = this.readCard8() & 0xFF;
            int amount;
            switch (format) {
                case 0:
                case 128:
                    amount = this.readCard8() & 0xFF;
                    for (int i = 0; i < amount; ++i) {
                        this.encoding[this.readCard8()] = i;
                    }
                    if (format == 0) {
                        break;
                    }
                    this.readSupplements();
                    break;
                case 1:
                case 129:
                    amount = this.readCard8() & 0xFF;
                    int encodingPointer = 0;
                    for (int i = 0; i < amount; ++i) {
                        int first = this.readCard8() & 0xFF;
                        int nLeft = this.readCard8() & 0xFF;
                        for (int j = 0; j <= nLeft; ++j) {
                            encoding[(first + j)] = encodingPointer++;
                        }
                    }
                    if (format == 1) {
                        break;
                    }
                    this.readSupplements();
                    break;
                default:
                    break;
            }
        }
    }

    private void readSupplements() throws IOException {
        int nSups = this.readCard8() & 0xFF;
        for (int i = 0; i < nSups; ++i) {
            int code = this.readCard8() & 0xFF;
            int glyph = this.readCard16();
            encoding[code] = glyph;
        }
    }

    private void readCharSet() throws IOException {
        this.charSet = new HashMap<>();
        this.inverseCharSet = new HashMap<>();
        this.charSet.put(this.getStringBySID(0), 0);
        this.inverseCharSet.put(0, this.getStringBySID(0));
        if (this.charSetOffset == 0) {
            initializeCharSet(CFFPredefined.ISO_ADOBE_CHARSET);
        } else if (this.charSetOffset == 1) {
            initializeCharSet(CFFPredefined.EXPERT_CHARSET);
        } else if (this.charSetOffset == 2) {
            initializeCharSet(CFFPredefined.EXPERT_SUBSET_CHARSET);
        } else {
            int format = this.readCard8();
            switch (format) {
                case 0:
                    for (int i = 1; i < nGlyphs; ++i) {
                        int sid = this.readCard16();
                        this.charSet.put(this.getStringBySID(sid), i);
                        this.inverseCharSet.put(i, this.getStringBySID(sid));
                    }
                    break;
                case 1:
                case 2:
                    try {
                        int charSetPointer = 1;
                        while (charSetPointer < nGlyphs) {
                            int first = this.readCard16();
                            int nLeft;
                            if (format == 1) {
                                nLeft = this.readCard8() & 0xFF;
                            } else {
                                nLeft = this.readCard16();
                            }
                            for (int i = 0; i <= nLeft; ++i) {
                                this.charSet.put(this.getStringBySID(first + i),
                                        charSetPointer);
                                this.inverseCharSet.put(charSetPointer++,
                                        this.getStringBySID(first + i));
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new IOException("Error in parsing ranges of CharString in CFF file", e);
                    }
                    break;
                default:
                    throw new IOException("Can't process format of CharSet in CFF file");
            }
        }
    }

    private void readWidths() throws IOException {
        for (int i = 0; i < nGlyphs; ++i) {
            CFFNumber width = getWidthFromCharString(this.charStrings.get(i));
            float res = width.isInteger() ? width.getInteger() :
                    width.getReal();
            if (res == -1.) {
                res = this.defaultWidthX;
            } else {
                res += this.nominalWidthX;
            }
            this.widths[i] = res;
        }
        if (!Arrays.equals(this.fontMatrix, CFFType1FontProgram.DEFAULT_FONT_MATRIX)) {
            for (int i = 0; i < widths.length; ++i) {
                widths[i] = widths[i] * (fontMatrix[0] * 1000);
            }
        }
    }

    private String getGlyphName(int code) {
        if(pdEncoding != null) {
            return pdEncoding.getName(code);
        }
        if(isStandardEncoding) {
            return CFFPredefined.STANDARD_STRINGS[CFFPredefined.STANDARD_ENCODING[code]];
        } else if (isExpertEncoding) {
            int sid = CFFPredefined.EXPERT_ENCODING[code];
            if(sid == CFFPredefined.SPACE_SID_EXPERT) {
                return CFFPredefined.EXPERT_CHARSET[CFFPredefined.SPACE_SID_EXPERT];
            } else if (sid < CFFPredefined.ISO_ADOBE_CHARSET.length) {
                return CFFPredefined.STANDARD_STRINGS[sid];
            } else if (sid <= CFFPredefined.EXPERT_ENCODING_LAST_SID) {
                return CFFPredefined.EXPERT_CHARSET[sid -
                        CFFPredefined.ISO_ADOBE_CHARSET.length - 2];
            }
            return NOTDEF_STRING;
        } else {
            return inverseCharSet.get(encoding[code]);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(int charCode) {
        if(externalCMap != null) {
            int gid = this.externalCMap.toCID(charCode);
            if(gid < widths.length) {
                return widths[gid];
            } else {
                return widths[0];
            }
        }
        try {
            return this.getWidth(getGlyphName(charCode));
        } catch (ArrayIndexOutOfBoundsException e) {
            return this.widths[0];
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(String charName) {
        Integer index = this.charSet.get(charName);
        if (index == null || index >= this.widths.length || index < 0) {
            return this.widths[0];
        }
        return this.widths[index];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsCode(int code) {
        return this.charSet.keySet().contains(this.getGlyphName(code));
    }

    private void initializeCharSet(String[] charSetArray) {
        for (int i = 0; i < charSetArray.length; ++i) {
            charSet.put(charSetArray[i], i);
            inverseCharSet.put(i, charSetArray[i]);
        }
    }

    public String[] getEncoding() {
        if (this.encodingStrings == null) {
            this.encodingStrings = new String[256];
            for(int i = 0; i < 256; ++i) {
                String glyphName = inverseCharSet.get(encoding[i]);
                this.encodingStrings[i] =
                        glyphName == null ? NOTDEF_STRING : glyphName;
            }
            return this.encodingStrings;
        } else {
            return this.encodingStrings;
        }
    }

    /**
     * @return list of names for all glyphs in this font.
     */
    public String[] getCharSet() {
        Set<String> set = this.charSet.keySet();
        return set.toArray(new String[set.size()]);
    }
}
