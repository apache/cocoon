/*
 * $Id: Sex.java,v 1.1 2003/11/06 22:58:37 ugo Exp $
 */
package org.apache.cocoon.woody.samples;

import java.util.Locale;

import org.apache.cocoon.woody.datatype.Enum;

/**
 * Description of Sex.
 * @version CVS $Id: Sex.java,v 1.1 2003/11/06 22:58:37 ugo Exp $
 */
public class Sex implements Enum {

    public static final Sex MALE = new Sex("M");
    public static final Sex FEMALE = new Sex("F");
    private String code;

    private Sex(String code) { this.code = code; }

    public String toString() {
      // Will probably have some i18n support here
      switch(code.charAt(0)) {
          case 'M' : return "male";
          case 'F' : return "female";
          default : return "unknown"; // Should never happen
      }
    }

    public static Sex fromString(String value, Locale locale) {
        if (value.equals("male")) return Sex.MALE;
        if (value.equals("female")) return Sex.FEMALE;
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.woody.datatype.Enum#convertToString(java.lang.Object, java.util.Locale)
     */
    public String convertToString(Locale locale) {
        return toString();
    }

}
