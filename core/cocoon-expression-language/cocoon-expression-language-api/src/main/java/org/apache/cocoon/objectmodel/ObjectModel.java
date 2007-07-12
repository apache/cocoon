package org.apache.cocoon.objectmodel;

import org.apache.commons.collections.MultiMap;

/**
 * ObjectModel is just a {@link MultiMap} with little more constrained contracts.
 * 
 * The only difference is that Collection for each key is compliant with LIFO list constracts. 
 */
public interface ObjectModel extends MultiMap {

}
