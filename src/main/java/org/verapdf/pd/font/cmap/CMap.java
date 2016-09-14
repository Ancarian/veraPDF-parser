package org.verapdf.pd.font.cmap;

import org.apache.log4j.Logger;
import org.verapdf.as.io.ASInputStream;

import java.io.IOException;
import java.util.*;

/**
 * This class represents cmap.
 *
 * @author Sergey Shemyakov
 */
public class CMap {

    private static final Logger LOGGER = Logger.getLogger(CMap.class);

    private int wMode;
    private String registry, ordering;
    private int supplement;
    private String name;
    int shortestCodeSpaceLength;

    private List<CIDMappable> cidMappings;
    private List<CodeSpace> codeSpaces;
    private List<CIDMappable> notDefMappings;
    private Map<Integer, String> toUnicode;

    public CMap() {
        this.cidMappings = new LinkedList<>();
        this.codeSpaces = new ArrayList<>();
        this.notDefMappings = new ArrayList<>();
        this.toUnicode = new HashMap<>();
        wMode = 0; // default
        shortestCodeSpaceLength = Integer.MAX_VALUE;
    }

    /**
     * Gets CID for given character.
     *
     * @param character is code of character, for which CID is calculated.
     * @return CID for given character or 0 if it cannot be obtained.
     */
    public int toCID(int character) {
        for (CIDMappable cidMapping : cidMappings) {
            int res = cidMapping.getCID(character);
            if (res != -1) {
                return res;
            }
        }

        for (CIDMappable notDefMapping : notDefMappings) {
            int res = notDefMapping.getCID(character);
            if (res != -1) {
                return res;
            }
        }
        return 0;
    }

    /**
     * Reads character code from input stream and returnes it's CID. This uses
     * codespace information from CMap. Details are described in PDF32000 in
     * 9.7.6.2 "CMap Mapping".
     *
     * @param stream is stream from which character codes will be read.
     * @return CID of read code.
     */
    public int getCIDFromStream(ASInputStream stream) throws IOException {
        byte[] charCode = new byte[4];
        byte[] temp = new byte[1];
        int previousShortestMatchingCodeSpaceLength = this.shortestCodeSpaceLength;

        for (int i = 0; i <= 4; ++i) {
            stream.read(temp, 1);
            System.arraycopy(temp, 0, charCode, i, 1);
            byte[] currentCode = Arrays.copyOf(charCode, i + 1);
            for (CodeSpace codeSpace : codeSpaces) {     // Looking for complete match
                if (codeSpace.contains(currentCode)) {
                    int res = toCID((int) CMapParser.numberFromBytes(currentCode));
                    if (res != -1) {
                        return res;
                    } else {
                        LOGGER.debug("CMap " + this.name + " has invalid codespace information.");
                    }
                }
            }
            int shortestMatchingCodeSpaceLength = Integer.MAX_VALUE;
            for (CodeSpace codeSpace : codeSpaces) { // Looking for partial matches on bytes 0, ..., i
                boolean partialMatch = true;
                for (int j = 0; j <= i; ++j) {
                    if (!codeSpace.isPartialMatch(charCode[j], j)) {
                        partialMatch = false;
                        break;
                    }
                }
                if (partialMatch && shortestMatchingCodeSpaceLength >
                        codeSpace.getLength()) {
                    shortestMatchingCodeSpaceLength = codeSpace.getLength();    // Remembering length of shortest partially matching codespace
                }
            }
            if (shortestMatchingCodeSpaceLength == Integer.MAX_VALUE) {
                stream.read(charCode, previousShortestMatchingCodeSpaceLength - i - 1);    // No described partial matching, reading necessary amount of bytes and returning 0
                return 0;
            }
            previousShortestMatchingCodeSpaceLength = shortestMatchingCodeSpaceLength;
        }
        return 0;
    }

    /**
     * Method adds all the data needed to this CMap from another CMap.
     *
     * @param another is another CMap.
     */
    public void useCMap(CMap another) {
        this.cidMappings.addAll(another.cidMappings);
        this.codeSpaces.addAll(another.codeSpaces);
        this.notDefMappings.addAll(another.notDefMappings);
        this.toUnicode.putAll(another.toUnicode);
    }

    /**
     * @return writing mode of given CMap.
     */
    public int getwMode() {
        return wMode;
    }

    /**
     * Setter for writing mode of given CMap.
     */
    void setwMode(int wMode) {
        this.wMode = wMode;
    }

    /**
     * @return Registry value from CIDSystemInfo dictionary.
     */
    public String getRegistry() {
        return registry;
    }

    /**
     * Setter for Registry.
     */
    void setRegistry(String registry) {
        this.registry = registry;
    }

    /**
     * @return Ordering value from CIDSystemInfo dictionary.
     */
    public String getOrdering() {
        return ordering;
    }

    /**
     * Setter for Ordering.
     */
    void setOrdering(String ordering) {
        this.ordering = ordering;
    }

    /**
     * @return supplement of CMap.
     */
    public int getSupplement() {
        return supplement;
    }

    public void setSupplement(int supplement) {
        this.supplement = supplement;
    }

    /**
     * Setter for name of CMap.
     */
    void setName(String name) {
        this.name = name;
    }

    /**
     * Setter for codespace ranges.
     */
    void setCodeSpaces(List<CodeSpace> codeSpaces) {
        this.codeSpaces = codeSpaces;
    }

    void addUnicodeMapping(int code, String toUnicode) {
        this.toUnicode.put(code, toUnicode);
    }

    /**
     * Returns Unicode sequence for given character code.
     *
     * @param code is code of character.
     * @return Unicode sequence obtained from this CMap.
     */
    public String getUnicode(int code) {
        return this.toUnicode.get(code);
    }

    /**
     * @return name of this CMap.
     */
    public String getName() {
        return name;
    }

    void addNotDefInterval(NotDefInterval interval) {
        this.notDefMappings.add(interval);
    }

    List<CodeSpace> getCodeSpaces() {
        return codeSpaces;
    }

    void addCidInterval(CIDInterval interval) {
        this.cidMappings.add(0, interval);
    }

    void addSingleCidMapping(SingleCIDMapping mapping) {
        this.cidMappings.add(0, mapping);
    }

    void addSingleNotDefMapping(SingleCIDMapping mapping) {
        this.notDefMappings.add(mapping);
    }
}
