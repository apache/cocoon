/*
 * Created on 2005-02-25
 */
package org.apache.cocoon.template.jxtg.instruction;

import org.apache.cocoon.template.jxtg.script.event.Event;

/**
 * @author lgawron
 */
public class MacroContext {
    private final String macroQName;
    private final Event bodyStart;
    private final Event bodyEnd;

    public MacroContext(String macroQName, Event bodyStart, Event bodyEnd) {
        this.macroQName = macroQName;
        this.bodyStart = bodyStart;
        this.bodyEnd = bodyEnd;
    }

    public Event getBodyEnd() {
        return bodyEnd;
    }

    public Event getBodyStart() {
        return bodyStart;
    }

    public String getMacroQName() {
        return macroQName;
    }
}
