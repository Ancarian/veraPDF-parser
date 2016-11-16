package org.verapdf.pd.font;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides interface for working with CIDToGID mapping in Type 2 CID fonts.
 *
 * @author Sergey Shemyakov
 */
public class CIDToGIDMapping {

    private static final Logger LOGGER = Logger.getLogger(CIDToGIDMapping.class.getCanonicalName());
    private int[] mapping;
    private boolean isIdentity;

    /**
     * Constructor from COSObject, containing CIDToGID.
     *
     * @param obj is COSObject, obtained via key CIDToGIDMap in CIDFontType2 dict.
     */
    public CIDToGIDMapping(COSObject obj) throws IOException {
        if (obj != null && (obj.getType() == COSObjType.COS_STREAM ||
                obj.getType() == COSObjType.COS_NAME)) {
            if (obj.getType() == COSObjType.COS_NAME && obj.getName() == ASAtom.IDENTITY) {
                this.isIdentity = true;
                this.mapping = new int[0];
                return;
            } else {
                this.isIdentity = false;
                ASInputStream stream = obj.getData(COSStream.FilterFlags.DECODE);
                mapping = new
                        int[(obj.getIntegerKey(ASAtom.LENGTH).intValue() + 1) / 2];
                parseCIDToGIDStream(stream);
                return;
            }
        }
        this.isIdentity = true;     // Default value.
        this.mapping = new int[0];
    }

    /**
     * Gets GID for given CID with use of this CIDToGIDMap.
     *
     * @param cid is character ID.
     * @return glyph ID for cid or 0 of no GID is found.
     */
    public int getGID(int cid) {
        if (isIdentity) {
            return cid;
        }
        if (cid < mapping.length) {
            return mapping[cid];
        } else {
            return 0;
        }
    }

    /**
     * Checks if given CID can be mapped into GID with this CIDToGID mapping.
     *
     * @param cid is CID to check.
     * @return true if cid can be mapped into GID.
     */
    public boolean contains(int cid) {
        if (isIdentity) {
            return true;
        }
        return cid < mapping.length && cid >= 0;
    }

    private void parseCIDToGIDStream(ASInputStream stream) throws IOException {
        int b = stream.read();
        int i = 0;
        while (b != -1) {
            int res = b;
            res <<= 8;
            b = stream.read();
            if (b != -1) {
                res += b;
                mapping[i++] = res;
                b = stream.read();
            } else {
                mapping[i++] = res;
            }
            if(i == mapping.length) {
                LOGGER.log(Level.FINE, "CIDToGID mapping has more items than specified in dict");
                List<Integer> endOfMapping = readEndOfMapping(stream, b);
                int[] newMapping = new int[mapping.length + endOfMapping.size()];
                System.arraycopy(mapping, 0, newMapping, 0, mapping.length);
                for(int j = mapping.length; j < newMapping.length; ++j) {
                    newMapping[j] = endOfMapping.get(j - mapping.length);
                }
                this.mapping = newMapping;
                break;
            }
        }
    }

    private List<Integer> readEndOfMapping(ASInputStream stream, int b) throws IOException {
        List<Integer> res = new ArrayList<>();
        while (b != -1) {
            int num = b;
            num <<= 8;
            b = stream.read();
            if (b != -1) {
                num += b;
                res.add(num);
                b = stream.read();
            } else {
                res.add(num);
            }
        }
        return res;
    }
}
