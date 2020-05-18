/*******************************************************************************
 * Copyright (c) 2006, 2019 THALES GLOBAL SERVICES.
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

package org.polarsys.capella.core.business.queries.ctx;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.polarsys.capella.common.queries.interpretor.QueryInterpretor;
import org.polarsys.capella.common.queries.queryContext.QueryContext;
import org.polarsys.capella.core.business.queries.IBusinessQuery;
import org.polarsys.capella.core.business.queries.QueryConstants;
import org.polarsys.capella.core.data.cs.CsPackage;
import org.polarsys.capella.core.data.ctx.CtxPackage;

/**
 */
public class SystemComponent_UsedInterfaces implements IBusinessQuery {

	@Override
	public EClass getEClass() {
		return CtxPackage.Literals.SYSTEM_COMPONENT;
	}

	@Override
	public List<EReference> getEStructuralFeatures() {
    List<EReference> list = new ArrayList<EReference>(1);
    list.add(CsPackage.Literals.INTERFACE_USE__USED_INTERFACE);
    list.add(CsPackage.Literals.COMPONENT__OWNED_INTERFACE_USES);
    return list;
	}

	@Override
	public List<EObject> getAvailableElements(EObject element) {
	  QueryContext context = new QueryContext();
		context.putValue(QueryConstants.ECLASS_PARAMETER, getEClass());
		return QueryInterpretor.executeQuery(QueryConstants.GET_AVAILABLE__SYSTEM_COMPONENT__USED_INTERFACES___LIB, element, context);
	}

	@Override
	public List<EObject> getCurrentElements(EObject element, boolean onlyGenerated) {
	  QueryContext context = new QueryContext();
		context.putValue(QueryConstants.ECLASS_PARAMETER, getEClass());
		return QueryInterpretor.executeQuery(QueryConstants.GET_CURRENT__SYSTEM_COMPONENT__USED_INTERFACES, element, context);
	}
}
