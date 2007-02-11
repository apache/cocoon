/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.environment;

/**
 * Describes a {@link Source} object whose data content can change.
 *
 * @deprecated Use the {@link org.apache.excalibur.source.Source} interface instead
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @version CVS $Id: ModifiableSource.java,v 1.3 2004/03/05 13:02:41 bdelacretaz Exp $
 */
public interface ModifiableSource extends Source
{
  /**
   * Refresh the content of this object after the underlying data
   * content has changed.
   */
  void refresh();
}
