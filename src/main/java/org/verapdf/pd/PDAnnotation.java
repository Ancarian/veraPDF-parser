package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.tools.TypeConverter;

/**
 * @author Maksim Bezrukov
 */
public class PDAnnotation extends PDObject {

	public PDAnnotation(COSObject obj) {
		super(obj);
	}

	public ASAtom getSubtype() {
		return getObject().getNameKey(ASAtom.SUBTYPE);
	}

	public Long getF() {
		return getObject().getIntegerKey(ASAtom.F);
	}

	public Double getCA() {
		return getObject().getRealKey(ASAtom.CA);
	}

	public ASAtom getFT() {
		return getObject().getNameKey(ASAtom.FT);
	}

	public double[] getRect() {
		return TypeConverter.getRealArray(getKey(ASAtom.RECT), 4, "Rect");
	}

	public COSObject getCOSC(){
		COSObject res = getKey(ASAtom.C);
		if (res != null && res.getType() == COSObjType.COS_ARRAY) {
			return res;
		}
		return null;
	}

	public COSObject getCOSIC(){
		COSObject res = getKey(ASAtom.IC);
		if (res != null && res.getType() == COSObjType.COS_ARRAY) {
			return res;
		}
		return null;
	}
}
