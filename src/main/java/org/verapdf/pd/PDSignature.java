package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSString;

/**
 * Represents digital signature on pd level.
 *
 * @author Sergey Shemyakov
 */
public class PDSignature extends PDObject {

    public PDSignature(COSDictionary dict) {
        super(new COSObject(dict));
    }

    /**
     * @return int[] representation of ByteRange entry.
     */
    public int[] getByteRange() {
        COSObject cosByteRange = this.getKey(ASAtom.BYTERANGE);
        if (cosByteRange != null) {
            COSArray array = (COSArray) cosByteRange.get();
            if (array.size() >= 4) {
                int[] res = new int[4];
                for (int i = 0; i < 4; ++i) {
                    res[i] = array.at(i).getInteger().intValue();
                }
                return res;
            }
        }
        return null;
    }

    /**
     * @return array of signature reference dictionaries.
     */
    public COSArray getReference() {
        return (COSArray) this.getKey(ASAtom.REFERENCE).get();
    }

    /**
     * @return COSString that contains Contents of PDSignature.
     */
    public COSString getContents() {
        COSString contents = (COSString) this.getKey(ASAtom.CONTENTS).get();
        return contents;
    }

    /**
     * @return the name of the preferred signature handler to use when
     * validating this signature.
     */
    public ASAtom getFilter() {
        return this.getNameKey(ASAtom.FILTER);
    }

    /**
     * @return a name that describes the encoding of the signature value and key
     * information in the signature dictionary.
     */
    public ASAtom getSubfilter() {
        return this.getNameKey(ASAtom.SUB_FILTER);
    }

    /**
     * @return the name of the person or authority signing the document.
     */
    public String getName() {
        return this.getStringKey(ASAtom.NAME);
    }

    /**
     * @return the time of signing.
     */
    public String getSignDate() {
        return this.getStringKey(ASAtom.M);
    }

    /**
     * @return the CPU host name or physical location of the signing.
     */
    public String getLocation() {
        return this.getStringKey(ASAtom.LOCATION);
    }

    /**
     * @return the reason for the signing, such as ( I agree ).
     */
    public String getReason() {
        return this.getStringKey(ASAtom.REASON);
    }

    /**
     * @return information provided by the signer to enable a recipient to
     * contact the signer to verify the signature.
     */
    public String getContactInfo() {
        return this.getStringKey(ASAtom.CONTACT_INFO);
    }
}
