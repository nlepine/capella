/*******************************************************************************
 * Copyright (c) 2017 THALES GLOBAL SERVICES.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * 
 * SPDX-License-Identifier: EPL-2.0
 *   
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package org.polarsys.capella.core.projection.interfaces.validation;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.polarsys.capella.core.projection.interfaces.validation.messages"; //$NON-NLS-1$
  public static String DWF_I_23_GenerateInterfacesValidator_missingEI;
  public static String DWF_I_23_GenerateInterfacesValidator_suffix_ComponentExchange;
  public static String DWF_I_23_GenerateInterfacesValidator_suffix_FunctionalExchange;
  public static String DWF_I_23_GenerateInterfacesValidator_unknownEI;
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {
  }
}
