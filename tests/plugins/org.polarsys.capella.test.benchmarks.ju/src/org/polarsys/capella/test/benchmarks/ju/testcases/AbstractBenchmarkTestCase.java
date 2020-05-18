/*******************************************************************************
 * Copyright (c) 2019 THALES GLOBAL SERVICES.
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
package org.polarsys.capella.test.benchmarks.ju.testcases;

import java.util.Arrays;
import java.util.List;

import org.polarsys.capella.test.benchmarks.ju.TestParameters;
import org.polarsys.capella.test.framework.api.NonDirtyTestCase;

public abstract class AbstractBenchmarkTestCase extends NonDirtyTestCase {

  @Override
  public List<String> getRequiredTestModels() {
    return Arrays.asList(TestParameters.getInstance().getTestModelName());
  }

  @Override
  public void setUp() throws Exception {
    // Do nothing
  }

  @Override
  public void tearDown() throws Exception {
    // Do nothing
  }

}
