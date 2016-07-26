package org.verapdf.pd.colors;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;

/**
 * @author Maksim Bezrukov
 */
public class PDSeparation extends PDColorSpace {

    private final ASAtom colorantName;
    private final PDColorSpace alternate;
    private final COSObject tintTransform;

    public PDSeparation(ASAtom colorantName, PDColorSpace alternate, COSObject tintTransform) {
        this.colorantName = colorantName;
        this.alternate = alternate;
        this.tintTransform = tintTransform;
    }

    public ASAtom getColorantName() {
        return this.colorantName;
    }

    public PDColorSpace getAlternate() {
        return this.alternate;
    }

    public COSObject getTintTransform() {
        return this.tintTransform;
    }

    @Override
    public int getNumberOfComponents() {
        return 1;
    }

    @Override
    public ASAtom getType() {
        return ASAtom.SEPARATION;
    }
}
