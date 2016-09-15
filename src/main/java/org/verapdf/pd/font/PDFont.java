package org.verapdf.pd.font;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;
import org.verapdf.pd.PDResource;
import org.verapdf.pd.font.cmap.PDCMap;
import org.verapdf.pd.font.stdmetrics.StandardFontMetrics;
import org.verapdf.pd.font.stdmetrics.StandardFontMetricsFactory;
import org.verapdf.pd.font.type1.PDType1Font;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This is PD representation of font.
 *
 * @author Sergey Shemyakov
 */
public abstract class PDFont extends PDResource {

    private static final Logger LOGGER = Logger.getLogger(PDFont.class);

    protected COSDictionary dictionary;
    protected COSDictionary fontDescriptor;
    protected PDCMap toUnicodeCMap;
    protected boolean isFontParsed = false;
    protected FontProgram fontProgram;
    protected Encoding encoding = null;

    /**
     * Constructor from COSDictionary.
     *
     * @param dictionary is font dictionary.
     */
    public PDFont(COSDictionary dictionary) {
        if (dictionary == null) {
            dictionary = (COSDictionary) COSDictionary.construct().get();
        }
        this.dictionary = dictionary;
        COSObject fd = dictionary.getKey(ASAtom.FONT_DESC);
        if (fd != null && fd.getType() == COSObjType.COS_DICT) {
            fontDescriptor = (COSDictionary) fd.getDirectBase();
        } else {
            fontDescriptor = (COSDictionary) COSDictionary.construct().get();
        }
    }

    /**
     * @return font COSDictionary.
     */
    public COSDictionary getDictionary() {
        return dictionary;
    }

    /**
     * @return font descriptor COSDictionary.
     */
    public COSDictionary getFontDescriptor() {
        return fontDescriptor;
    }

    /**
     * @return font type (Type entry).
     */
    public String getType() {
        String type = this.dictionary.getStringKey(ASAtom.TYPE);
        return type == null ? "" : type;
    }

    /**
     * @return font subtype (Subtype entry).
     */
    public ASAtom getSubtype() {
        return this.dictionary.getNameKey(ASAtom.SUBTYPE);
    }

    /**
     * @return font name defined by BaseFont entry in the font dictionary and
     * FontName key in the font descriptor.
     * @throws IllegalStateException if font names specified in font dictionary
     *                               and font descriptor are different.
     */
    public ASAtom getFontName() {
        ASAtom type = this.dictionary.getNameKey(ASAtom.BASE_FONT);
        if (this.fontDescriptor != null && type != null) {
            ASAtom typeFromDescriptor =
                    this.fontDescriptor.getNameKey(ASAtom.FONT_NAME);
            if (type != typeFromDescriptor) {
                LOGGER.warn("Font names in font descriptor dictionary and in font dictionary are different for "
                        + type.getValue());
            }
        }
        return type;
    }

    /**
     * @return true if the font flags in the font descriptor dictionary mark
     * indicate that the font is symbolic (the entry /Flags has bit 3 set to 1
     * and bit 6 set to 0).
     * descriptor is null.
     */
    public boolean isSymbolic() {
        if (this.fontDescriptor == null) {
            LOGGER.warn("Font descriptor is null");
            return false;
        }
        Long flagsLong = this.fontDescriptor.getIntegerKey(ASAtom.FLAGS);
        if (flagsLong == null) {
            LOGGER.warn("Font descriptor doesn't contain /Flags entry");
            return false;
        }
        int flags = flagsLong.intValue();
        return (flags & 0b00100100) == 4;
    }

    public Encoding getEncodingMapping() {
        if (this.encoding == null) {
            COSBase cosEncoding = this.getEncoding().getDirectBase();
            if (cosEncoding.getType() == COSObjType.COS_NAME) {
                this.encoding = new Encoding(cosEncoding.getName());
                return this.encoding;
            } else if (cosEncoding.getType() == COSObjType.COS_DICT) {
                this.encoding = new Encoding(cosEncoding.getNameKey(ASAtom.BASE_ENCODING),
                        this.getDifferences());
                return this.encoding;
            } else {
                return null;
            }
        } else {
            return this.encoding;
        }
    }

    public String getName() {
        return this.dictionary.getStringKey(ASAtom.BASE_FONT);
    }

    public COSObject getEncoding() {
        return this.dictionary.getKey(ASAtom.ENCODING);
    }

    public COSStream getFontFile2() {
        return (COSStream)
                this.fontDescriptor.getKey(ASAtom.FONT_FILE2).getDirectBase();
    }

    public Map<Integer, String> getDifferences() {
        COSObject encoding = this.getEncoding();
        COSArray differences = (COSArray)
                encoding.getKey(ASAtom.DIFFERENCES).getDirectBase();
        if (differences == null) {
            return null;
        }
        Map<Integer, String> res = new HashMap<>();
        int diffIndex = 0;
        for (COSObject obj : differences) {
            if (obj.getType() == COSObjType.COS_INTEGER) {
                diffIndex = obj.getInteger().intValue();
            } else if (obj.getType() == COSObjType.COS_NAME && diffIndex != -1) {
                res.put(diffIndex++, obj.getString());
            }
        }
        return res;
    }

    public COSObject getWidths() {
        return this.dictionary.getKey(ASAtom.WIDTHS);
    }

    public Long getFirstChar() {
        return this.dictionary.getIntegerKey(ASAtom.FIRST_CHAR);
    }

    public Long getLastChar() {
        return this.dictionary.getIntegerKey(ASAtom.LAST_CHAR);
    }

    protected static COSStream getStreamFromObject(COSObject obj) throws IOException {
        if (obj == null || obj.getDirectBase().getType() != COSObjType.COS_STREAM) {
            throw new IOException("Can't get COSStream from COSObject");
        } else {
            return (COSStream) obj.getDirectBase();
        }
    }

    /**
     * Method reads next character code from stream according to font data. It
     * can contain from 1 to 4 bytes.
     *
     * @param stream is stream with raw data.
     * @return next character code read.
     * @throws IOException if reading fails.
     */
    public int readCode(InputStream stream) throws IOException {
        return stream.read();
    }

    public abstract FontProgram getFontProgram();

    /**
     * Gets Unicode string for given character code. This method returns null in
     * case when no toUnicode mapping for this character was found, so some
     * inherited classes need to call this method, check return value on null
     * and then implement their special logic.
     *
     * @param code is code for character.
     * @return Unicode string
     */
    public String toUnicode(int code) {

        if (toUnicodeCMap == null) {
            this.toUnicodeCMap = new PDCMap(this.dictionary.getKey(ASAtom.TO_UNICODE));
        }

        if (toUnicodeCMap.getCMapName() != null &&
                toUnicodeCMap.getCMapName().startsWith("Identity-")) {
            return new String(new char[]{(char) code});
        }
        return this.toUnicodeCMap.toUnicode(code);
    }

    public double getWidth(int code) {
        if (dictionary.knownKey(ASAtom.WIDTHS) ||
                dictionary.knownKey(ASAtom.MISSING_WIDTH)) {
            int firstChar = dictionary.getIntegerKey(ASAtom.FIRST_CHAR).intValue();
            int lastChar = dictionary.getIntegerKey(ASAtom.LAST_CHAR).intValue();
            if (getWidths().size() > 0 && code >= firstChar && code <= lastChar) {
                return getWidths().at(code - firstChar).getReal();
            }

            if (this.fontDescriptor != null) {
                return fontDescriptor.getRealKey(ASAtom.MISSING_WIDTH);
            }
        }

        if (this instanceof PDType1Font && ((PDType1Font) this).isStandard()) {
            StandardFontMetrics metrics =
                    StandardFontMetricsFactory.getFontMetrics(this.getName());
            Encoding enc = this.getEncodingMapping();
            if (metrics != null) {
                return metrics.getWidth(enc.getName(code));
            } else {
                // should not get here
                LOGGER.error("Can't get standard metrics");
                return 0;
            }
        }

        try {
            this.getFontProgram().parseFont();
            return this.getFontProgram().getWidth(code);
        } catch (IOException e) {
            LOGGER.warn("Can't parse font program of font " + this.getName());
            return 0;
        }
    }
}
