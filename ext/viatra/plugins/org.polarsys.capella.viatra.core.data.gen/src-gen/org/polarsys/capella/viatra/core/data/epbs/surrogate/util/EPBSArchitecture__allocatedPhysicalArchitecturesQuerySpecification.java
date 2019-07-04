/**
 * 
 *   Copyright (c) 2006, 2019 THALES DMS FRANCE.
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *  
 *   Contributors:
 *      Thales - initial API and implementation
 *  
 */
package org.polarsys.capella.viatra.core.data.epbs.surrogate.util;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngine;
import org.eclipse.viatra.query.runtime.api.impl.BaseGeneratedEMFPQuery;
import org.eclipse.viatra.query.runtime.api.impl.BaseGeneratedEMFQuerySpecification;
import org.eclipse.viatra.query.runtime.emf.types.EClassTransitiveInstancesKey;
import org.eclipse.viatra.query.runtime.emf.types.EStructuralFeatureInstancesKey;
import org.eclipse.viatra.query.runtime.exception.ViatraQueryException;
import org.eclipse.viatra.query.runtime.matchers.backend.IQueryBackendFactory;
import org.eclipse.viatra.query.runtime.matchers.backend.QueryEvaluationHint;
import org.eclipse.viatra.query.runtime.matchers.psystem.PBody;
import org.eclipse.viatra.query.runtime.matchers.psystem.PVariable;
import org.eclipse.viatra.query.runtime.matchers.psystem.annotations.PAnnotation;
import org.eclipse.viatra.query.runtime.matchers.psystem.basicdeferred.Equality;
import org.eclipse.viatra.query.runtime.matchers.psystem.basicdeferred.ExportedParameter;
import org.eclipse.viatra.query.runtime.matchers.psystem.basicenumerables.TypeConstraint;
import org.eclipse.viatra.query.runtime.matchers.psystem.queries.PParameter;
import org.eclipse.viatra.query.runtime.matchers.psystem.queries.PParameterDirection;
import org.eclipse.viatra.query.runtime.matchers.psystem.queries.QueryInitializationException;
import org.eclipse.viatra.query.runtime.matchers.tuple.FlatTuple;
import org.polarsys.capella.viatra.core.data.epbs.surrogate.EPBSArchitecture__allocatedPhysicalArchitecturesMatch;
import org.polarsys.capella.viatra.core.data.epbs.surrogate.EPBSArchitecture__allocatedPhysicalArchitecturesMatcher;

/**
 * A pattern-specific query specification that can instantiate EPBSArchitecture__allocatedPhysicalArchitecturesMatcher in a type-safe way.
 * 
 * @see EPBSArchitecture__allocatedPhysicalArchitecturesMatcher
 * @see EPBSArchitecture__allocatedPhysicalArchitecturesMatch
 * 
 */
@SuppressWarnings("all")
public final class EPBSArchitecture__allocatedPhysicalArchitecturesQuerySpecification extends BaseGeneratedEMFQuerySpecification<EPBSArchitecture__allocatedPhysicalArchitecturesMatcher> {
  private EPBSArchitecture__allocatedPhysicalArchitecturesQuerySpecification() {
    super(GeneratedPQuery.INSTANCE);
  }
  
  /**
   * @return the singleton instance of the query specification
   * @throws ViatraQueryException if the pattern definition could not be loaded
   * 
   */
  public static EPBSArchitecture__allocatedPhysicalArchitecturesQuerySpecification instance() throws ViatraQueryException {
    try{
        return LazyHolder.INSTANCE;
    } catch (ExceptionInInitializerError err) {
        throw processInitializerError(err);
    }
  }
  
  @Override
  protected EPBSArchitecture__allocatedPhysicalArchitecturesMatcher instantiate(final ViatraQueryEngine engine) throws ViatraQueryException {
    return EPBSArchitecture__allocatedPhysicalArchitecturesMatcher.on(engine);
  }
  
  @Override
  public EPBSArchitecture__allocatedPhysicalArchitecturesMatcher instantiate() throws ViatraQueryException {
    return EPBSArchitecture__allocatedPhysicalArchitecturesMatcher.create();
  }
  
  @Override
  public EPBSArchitecture__allocatedPhysicalArchitecturesMatch newEmptyMatch() {
    return EPBSArchitecture__allocatedPhysicalArchitecturesMatch.newEmptyMatch();
  }
  
  @Override
  public EPBSArchitecture__allocatedPhysicalArchitecturesMatch newMatch(final Object... parameters) {
    return EPBSArchitecture__allocatedPhysicalArchitecturesMatch.newMatch((org.polarsys.capella.core.data.epbs.EPBSArchitecture) parameters[0], (org.polarsys.capella.core.data.pa.PhysicalArchitecture) parameters[1]);
  }
  
  /**
   * Inner class allowing the singleton instance of {@link EPBSArchitecture__allocatedPhysicalArchitecturesQuerySpecification} to be created 
   *     <b>not</b> at the class load time of the outer class, 
   *     but rather at the first call to {@link EPBSArchitecture__allocatedPhysicalArchitecturesQuerySpecification#instance()}.
   * 
   * <p> This workaround is required e.g. to support recursion.
   * 
   */
  private static class LazyHolder {
    private final static EPBSArchitecture__allocatedPhysicalArchitecturesQuerySpecification INSTANCE = new EPBSArchitecture__allocatedPhysicalArchitecturesQuerySpecification();
    
    /**
     * Statically initializes the query specification <b>after</b> the field {@link #INSTANCE} is assigned.
     * This initialization order is required to support indirect recursion.
     * 
     * <p> The static initializer is defined using a helper field to work around limitations of the code generator.
     * 
     */
    private final static Object STATIC_INITIALIZER = ensureInitialized();
    
    public static Object ensureInitialized() {
      INSTANCE.ensureInitializedInternalSneaky();
      return null;
    }
  }
  
  private static class GeneratedPQuery extends BaseGeneratedEMFPQuery {
    private final static EPBSArchitecture__allocatedPhysicalArchitecturesQuerySpecification.GeneratedPQuery INSTANCE = new GeneratedPQuery();
    
    private final PParameter parameter_pSelf = new PParameter("self", "org.polarsys.capella.core.data.epbs.EPBSArchitecture", new EClassTransitiveInstancesKey((EClass)getClassifierLiteralSafe("http://www.polarsys.org/capella/core/epbs/1.3.0", "EPBSArchitecture")), PParameterDirection.INOUT);
    
    private final PParameter parameter_pTarget = new PParameter("target", "org.polarsys.capella.core.data.pa.PhysicalArchitecture", new EClassTransitiveInstancesKey((EClass)getClassifierLiteralSafe("http://www.polarsys.org/capella/core/pa/1.3.0", "PhysicalArchitecture")), PParameterDirection.INOUT);
    
    private final List<PParameter> parameters = Arrays.asList(parameter_pSelf, parameter_pTarget);
    
    @Override
    public String getFullyQualifiedName() {
      return "org.polarsys.capella.viatra.core.data.epbs.surrogate.EPBSArchitecture__allocatedPhysicalArchitectures";
    }
    
    @Override
    public List<String> getParameterNames() {
      return Arrays.asList("self","target");
    }
    
    @Override
    public List<PParameter> getParameters() {
      return parameters;
    }
    
    @Override
    public Set<PBody> doGetContainedBodies() throws QueryInitializationException {
      setEvaluationHints(new QueryEvaluationHint(null, (IQueryBackendFactory)null));
      Set<PBody> bodies = Sets.newLinkedHashSet();
      try {
          {
              PBody body = new PBody(this);
              PVariable var_self = body.getOrCreateVariableByName("self");
              PVariable var_target = body.getOrCreateVariableByName("target");
              new TypeConstraint(body, new FlatTuple(var_self), new EClassTransitiveInstancesKey((EClass)getClassifierLiteral("http://www.polarsys.org/capella/core/epbs/1.3.0", "EPBSArchitecture")));
              new TypeConstraint(body, new FlatTuple(var_target), new EClassTransitiveInstancesKey((EClass)getClassifierLiteral("http://www.polarsys.org/capella/core/pa/1.3.0", "PhysicalArchitecture")));
              body.setSymbolicParameters(Arrays.<ExportedParameter>asList(
                 new ExportedParameter(body, var_self, parameter_pSelf),
                 new ExportedParameter(body, var_target, parameter_pTarget)
              ));
              // 	EPBSArchitecture.allocatedArchitectures(self, target)
              new TypeConstraint(body, new FlatTuple(var_self), new EClassTransitiveInstancesKey((EClass)getClassifierLiteral("http://www.polarsys.org/capella/core/epbs/1.3.0", "EPBSArchitecture")));
              PVariable var__virtual_0_ = body.getOrCreateVariableByName(".virtual{0}");
              new TypeConstraint(body, new FlatTuple(var_self, var__virtual_0_), new EStructuralFeatureInstancesKey(getFeatureLiteral("http://www.polarsys.org/capella/core/cs/1.3.0", "BlockArchitecture", "allocatedArchitectures")));
              new TypeConstraint(body, new FlatTuple(var__virtual_0_), new EClassTransitiveInstancesKey((EClass)getClassifierLiteral("http://www.polarsys.org/capella/core/cs/1.3.0", "BlockArchitecture")));
              new Equality(body, var__virtual_0_, var_target);
              bodies.add(body);
          }
          {
              PAnnotation annotation = new PAnnotation("Surrogate");
              annotation.addAttribute("feature", "allocatedPhysicalArchitectures");
              addAnnotation(annotation);
          }
          // to silence compiler error
          if (false) throw new ViatraQueryException("Never", "happens");
      } catch (ViatraQueryException ex) {
          throw processDependencyException(ex);
      }
      return bodies;
    }
  }
}