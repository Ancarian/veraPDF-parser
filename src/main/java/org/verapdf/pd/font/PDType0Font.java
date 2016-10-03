package org.verapdf.pd.font;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.font.cmap.CMap;
import org.verapdf.pd.font.cmap.PDCMap;

/**
 * Represents Type0 font on pd level.
 *
 * @author Sergey Shemyakov
 */
public class PDType0Font extends PDCIDFont {

    private static final Logger LOGGER = Logger.getLogger(PDType0Font.class);
    private static final String UCS2 = "UCS2";

    private PDCMap pdcMap;
    private PDCMap ucsCMap;
    private COSDictionary cidSystemInfo;
    private COSDictionary type0FontDict;

    public PDType0Font(COSDictionary dictionary) {
        super(getDedcendantCOSDictionary(dictionary));
        type0FontDict = dictionary == null ?
                (COSDictionary) COSDictionary.construct().get() : dictionary;

        this.cMap = getCMap().getCMapFile();
    }

    public COSDictionary getCIDSystemInfo() {
        if (this.cidSystemInfo == null) {
            COSObject cidFontDictObj =
                    this.type0FontDict.getKey(ASAtom.DESCENDANT_FONTS).at(0);
            if (!cidFontDictObj.empty()) {
                COSDictionary cidFontDict = (COSDictionary) cidFontDictObj.getDirectBase();
                if (cidFontDict != null) {
                    COSDictionary cidSystemInfo = (COSDictionary)
                            cidFontDict.getKey(ASAtom.CID_SYSTEM_INFO).getDirectBase();
                    this.cidSystemInfo = cidSystemInfo;
                    return cidSystemInfo;
                }
            }
            return null;
        } else {
            return this.cidSystemInfo;
        }
    }

    public org.verapdf.pd.font.cmap.PDCMap getCMap() {
        if (this.pdcMap == null) {
            COSObject cMap = this.type0FontDict.getKey(ASAtom.ENCODING);
            if (!cMap.empty()) {
                org.verapdf.pd.font.cmap.PDCMap pdcMap = new org.verapdf.pd.font.cmap.PDCMap(cMap);
                this.pdcMap = pdcMap;
                return pdcMap;
            } else {
                return null;
            }
        } else {
            return this.pdcMap;
        }
    }

    private static COSDictionary getDedcendantCOSDictionary(COSDictionary dict) {
        if (dict != null) {
            COSArray array =
                    (COSArray) dict.getKey(ASAtom.DESCENDANT_FONTS).getDirectBase();
            if (array != null) {
                return (COSDictionary) array.at(0).getDirectBase();
            }
        }
        return null;
    }

    public COSDictionary getDescendantFont() {
        return getDedcendantCOSDictionary(this.type0FontDict);
    }

    /**
     * This method maps character code to a Unicode value. Firstly it checks
     * toUnicode CMap, then it behaves like described in PDF32000_2008 9.10.2
     * "Mapping Character Codes to Unicode Values" for Type0 font.
     *
     * @param code is code for character.
     * @return unicode value.
     */
    @Override
    public String toUnicode(int code) {
        if(this.toUnicodeCMap == null) {
            this.toUnicodeCMap = new PDCMap(
                    this.type0FontDict.getKey(ASAtom.TO_UNICODE));
        }

        String unicode = super.toUnicode(code);
        if (unicode != null) {
            return unicode;
        }

        if(ucsCMap != null) {
            return ucsCMap.toUnicode(code);
        }

        PDCMap pdcMap = this.getCMap();
        if (pdcMap != null && pdcMap.getCMapFile() != null) {
            int cid = pdcMap.getCMapFile().toCID(code);
            String registry = pdcMap.getRegistry();
            String ordering = pdcMap.getOrdering();
            String ucsName = registry + "-" + ordering + "-" + UCS2;
            PDCMap pdUCSCMap = new PDCMap(COSName.construct(ucsName));
            CMap ucsCMap = pdUCSCMap.getCMapFile();
            if (ucsCMap != null) {
                this.ucsCMap = pdUCSCMap;
                return ucsCMap.getUnicode(cid);
            }
            LOGGER.debug("Can't load CMap " + ucsName);
            return null;
        } else {
            LOGGER.debug("Can't get CMap for font " + this.getName());
            return null;
        }
    }

    public COSDictionary getType0FontDict() {
        return type0FontDict;
    }

    public int toCID(int code) {
        return this.pdcMap.getCMapFile().toCID(code);
    }
}
