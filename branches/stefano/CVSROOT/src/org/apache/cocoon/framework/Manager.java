package org.apache.cocoon.framework;

import java.util.*;

/**
 * This class is used to create and control software actors.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:17 $
 */

public class Manager extends Hashtable implements Actor, Factory, Director {

    /**
     * Initialize the actor by indicating their director.
     */
    public void init(Director director) {}

    /**
     * Create the instance of a class given its name.
     */
    public Object create(String name) {
        return create(name, null);
    }

    /**
     * Create the instance of a class and, if configurable, use 
     * the given configurations to configure it.
     */
    public Object create(String name, Configurations conf) throws RuntimeException {
        try {
            Object object = Class.forName(name).newInstance();

            if (object instanceof Actor) {
                ((Actor) object).init((Director) this);
            }
            
            if ((object instanceof Configurable) && (conf != null)) {
                ((Configurable) object).init(conf);
            }
            
            return object;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error creating " + name + ": class is not found");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error creating " + name + ": does not have access");
        } catch (InstantiationException e) {
            throw new RuntimeException("Error creating " + name + ": could not instantiate " + e.getMessage());
        } catch (RuntimeException e) {
            throw e;
        } catch (java.lang.NoClassDefFoundError e) {
            throw new RuntimeException("Error creating " + name + ": make sure the needed classes can be found in the classpath");
        } catch (Throwable e) {
            throw new RuntimeException("Factory error:  unknown exception creating \" " + name + "\" : " + e);
        }
    }

    /**
     * Create a vector of instances.
     */
    public Vector create(Vector names) {
        return create(names, null);
    }
    
    /**
     * Create a vector of instances with given configurations.
     */
     public Vector create(Vector names, Configurations conf) {
         Vector v = new Vector(names.size());
         Enumeration e = names.elements();
         while (e.hasMoreElements()) {
             v.addElement(create((String) e.nextElement(), conf));
         }
         return v;
     }
     
    /**
     * Get the actor currently playing the given role.
     */
    public Actor getActor(String role) {
        return (Actor) this.get(role);
    }

    /**
     * Set the actor for the role.
     */
    public void setRole(String role, Actor actor) {
        this.put(role, actor);
    }
    
    /**
     * Get the roles currently set.
     */
    public Enumeration getRoles() {
        return this.keys();
    }
}