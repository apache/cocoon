/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package org.apache.cocoon.components.flow.javascript;
import org.mozilla.javascript.*;
import java.sql.*;

/**
 * Wraps a JDBC connection and provides an API similar to JSTL
 * A ScriptableConnection provides two methods:
 *
 * <UL>
 * <LI>query([String] stmt, [Array] parameters, [Number] startRow, [Number] maxRows)</LI>
 * <LI>update([String] stmt, [Array] parameters)</LI>
 * </UL>
 * The object returned by <code>query</code> contains the following
 * properties:
 * <UL>
 * <LI>[Array] rows - an array of row objects</LI>
 * <LI>[Array] rowsByIndex - An array with an array per row of column values</LI>
 * <LI>[Array] columnNames - An array of column names</LI>
 * <LI>[Number] rowCount - Number of rows returned</LI>
 * <LI>[Boolean] limitedByMaxRows - true if not all rows are included due to matching a maximum value </LI>
 * </UL>
 *
 * A ScriptableConnection is also a wrapper around a real JDBC Connection and thus 
 * provides all of methods of Connection as well
 *
 * @version CVS $Id: ScriptableConnection.java,v 1.5 2003/03/17 19:19:25 coliver Exp $
 */
public class ScriptableConnection extends ScriptableObject {

    Connection connection;
    Scriptable wrapper;

    static Object wrap(final Scriptable wrapper, 
                       final Scriptable wrapped,
                       Object obj) {
        if (obj instanceof Function) {
            return wrap(wrapper, wrapped, (Function)obj);
        }
        return obj;
    }


    static Function wrap(final Scriptable wrapper, 
                         final Scriptable wrapped, 
                         final Function fun) {
        return new Function() {
                public Object call(Context cx, Scriptable scope, Scriptable thisObj,
                                   Object[] args)  throws JavaScriptException {
                    if (thisObj == wrapper) {
                        thisObj = wrapped;
                    }
                    return fun.call(cx, scope, thisObj, args);
                }

                public Scriptable construct(Context cx, Scriptable scope, 
                                            Object[] args)
                    throws JavaScriptException {
                    return fun.construct(cx, scope, args);
                }

                public String getClassName() {
                    return fun.getClassName();
                }
                
                public Object get(String name, Scriptable start) {
                    return fun.get(name, fun);
                }

                public Object get(int index, Scriptable start) {
                    return fun.get(index, fun);
                }

                public boolean has(String name, Scriptable start) {
                    return fun.has(name, start);
                }

                public boolean has(int index, Scriptable start) {
                    return fun.has(index, start);
                }

                public void put(String name, Scriptable start, Object value) {
                    fun.put(name, start, value);
                }

                public void put(int index, Scriptable start, Object value) {
                    fun.put(index, start, value);
                }

                public void delete(String name) {
                    fun.delete(name);
                }

                public void delete(int index) {
                    fun.delete(index);
                }

                public Scriptable getPrototype() {
                    return fun.getPrototype();
                }

                public void setPrototype(Scriptable prototype) {
                }

                public Scriptable getParentScope() {
                    return fun.getParentScope();
                }

                public void setParentScope(Scriptable parent) {
                }

                public Object[] getIds() {
                    return fun.getIds();
                }

                public Object getDefaultValue(Class hint) {
                    return fun.getDefaultValue(hint);
                }

                public boolean hasInstance(Scriptable instance) {
                    return fun.hasInstance(instance);
                }

            };
    }

    public String getClassName() {
        return "Database";
    }

    public ScriptableConnection() {
    }

    public static void finishInit(Scriptable proto) {
    }

    public static Scriptable jsConstructor(Context cx, Object[] args,
                                           Function ctorObj, 
                                           boolean inNewExpr)
        throws Exception {
        Connection conn = null;
        if (args.length > 0) {
            Object arg = args[0];
            if (arg instanceof Wrapper) {
                arg = ((Wrapper)arg).unwrap();
            }
            if (arg instanceof Connection) {
                conn = (Connection)arg;
            }
        }
        if (conn == null) {
            throw new JavaScriptException("expected an instance of java.sql.Connection");
        }
        ScriptableConnection result = new ScriptableConnection(ctorObj, conn);
        return result;
    }


    public ScriptableConnection(Scriptable parent, Connection conn) {
        this.connection = conn;
        this.wrapper = Context.toObject(connection, parent);
    }

    public Object jsFunction_query(String sql, Object params,
                                   int startRow, int maxRows) 
        throws JavaScriptException {
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            Scriptable array = (Scriptable)params;
            if (array != Undefined.instance) {
                int len = (int)
                    Context.toNumber(ScriptableObject.getProperty(array, "length"));
                for (int i = 0; i < len; i++) {
                    Object val = ScriptableObject.getProperty(array, i);
                    if (val instanceof Wrapper) {
                        val = ((Wrapper)val).unwrap();
                    }
                    if (val == Scriptable.NOT_FOUND) {
                        val = null;
                    }
                    stmt.setObject(i + 1, val);
                }
            }
            ResultSet rs = stmt.executeQuery();
            if (maxRows == 0) {
                maxRows = -1;
            }
            ScriptableResult s = new ScriptableResult(this, rs, 
                                                      startRow, maxRows);
            s.setParentScope(getTopLevelScope(this));
            s.setPrototype(getClassPrototype(this, s.getClassName()));
            return s;
        } catch (Exception e) {
            throw new JavaScriptException(e);
        }
    }

    public int jsFunction_update(String sql, Object params) 
        throws JavaScriptException {
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            Scriptable array = (Scriptable)params;
            if (array != Undefined.instance) {
                int len = (int)
                    Context.toNumber(ScriptableObject.getProperty(array, "length"));
                for (int i = 0; i < len; i++) {
                    Object val = ScriptableObject.getProperty(array, i);
                    if (val instanceof Wrapper) {
                        val = ((Wrapper)val).unwrap();
                    }
                    if (val == Scriptable.NOT_FOUND) {
                        val = null;
                    }
                    stmt.setObject(i + 1, val);
                }
            }
            stmt.execute();
            return stmt.getUpdateCount();
        } catch (Exception e) {
            throw new JavaScriptException(e);
        }
    }

    public Object get(String name, Scriptable start) {
        if (wrapper != null) {
            Object result = wrapper.get(name, wrapper);
            if (result != NOT_FOUND) {
                return wrap(this, wrapper, result);
            }
        }
        return super.get(name, start);
    }

    public boolean has(String name, Scriptable start) {
        if (wrapper != null) {
            if (wrapper.has(name, wrapper)) {
                return true;
            }
        }
        return super.has(name, start);
    }

    public boolean has(int index, Scriptable start) {
        if (wrapper != null) {
            if (wrapper.has(index, start)) {
                return true;
            }
        }
        return super.has(index, start);
    }

    public Object get(int index, Scriptable start) {
        if (wrapper != null) {
            Object result = wrapper.get(index, start);
            if (result != NOT_FOUND) {
                return wrap(this, wrapper, result);
            }
        }
        return super.get(index, start);
    }

    public void put(String name, Scriptable start, Object value) {
        if (wrapper != null) {
            wrapper.put(name, wrapper, value);
            return;
        }
        super.put(name, start, value);
    }

}



