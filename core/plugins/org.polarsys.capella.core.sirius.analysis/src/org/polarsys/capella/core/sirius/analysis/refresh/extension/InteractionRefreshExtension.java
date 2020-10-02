/*******************************************************************************
 * Copyright (c) 2020 THALES GLOBAL SERVICES.
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
package org.polarsys.capella.core.sirius.analysis.refresh.extension;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.diagram.DDiagram;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.helpers.cache.Cache;
import org.polarsys.capella.core.data.interaction.InstanceRole;
import org.polarsys.capella.core.data.interaction.InteractionFragment;
import org.polarsys.capella.core.sirius.analysis.EventContextServices.EventContext;

public class InteractionRefreshExtension extends AbstractCacheAwareRefreshExtension {

  /**
   * Generic cache (basically for getCovered).
   */
  private static Cache interactionCache = new Cache();

  /**
   * Cache for MessageEnd -> Execution/InstanceRole and InteractionState -> StateFragment. (Cross referencer might be a
   * way to get this information.)
   */
  private static Map<InteractionFragment, CapellaElement> endToEventCache = new ConcurrentHashMap<>();

  /**
   * Cache for ChildExecution -> ParentExecution and StateFragment -> ParentExecution.
   */
  private static Map<EObject, Collection<EObject>> elementToContainerCache = new ConcurrentHashMap<>();

  /**
   * Cache for InstanceRole -> EventContext structure.
   */
  private static Map<InstanceRole, List<EventContext>> instanceRoleToEventContextCache = new ConcurrentHashMap<>();

  /**
   * Is InteractionRefreshExtension caches enabled ?
   */
  private static boolean isCacheEnabled;

  /**
   * @see org.eclipse.sirius.business.api.refresh.IRefreshExtension#beforeRefresh(org.eclipse.sirius.DDiagram)
   */
  @Override
  public void beforeRefresh(DDiagram diagram) {
    isCacheEnabled = true;
  }

  /**
   * @see org.eclipse.sirius.business.api.refresh.IRefreshExtension#postRefresh(org.eclipse.sirius.DDiagram)
   */
  @Override
  public void postRefresh(DDiagram diagram) {
    clearCaches();
    isCacheEnabled = false;
  }

  private void clearCaches() {
    interactionCache.clearCache();
    elementToContainerCache.clear();
    instanceRoleToEventContextCache.clear();
    endToEventCache.clear();
  }

  /**
   * 
   * @param function
   * @param parameter
   * @return If enabled, return the cached result if any or apply the function to the given parameter and cache the
   *         result before returning it. //
   */
  public static <P, R> R getInteractionCache(Function<P, R> function, P parameter) {
    return interactionCache.get(function, parameter);
  }

  /**
   * Get EventContext structure for instanceRole
   * 
   * @param instanceRole
   *          InstanceRole
   * @return List<EventContext>
   */
  public static List<EventContext> getInstanceRoleToEventContextCache(InstanceRole instanceRole) {
    return instanceRoleToEventContextCache.get(instanceRole);
  }

  /**
   * Put EventContext structure for instanceRole in cache.
   * 
   * @param instanceRole
   *          InstanceRole
   * @param structure
   *          List<EventContext>
   */
  public static void putInstanceRoleToEventContextCache(InstanceRole instanceRole, List<EventContext> structure) {
    instanceRoleToEventContextCache.put(instanceRole, structure);
  }

  /**
   * Get element container.
   * 
   * @param capellaElement
   *          EObject
   * @return Collection<EObject>
   */
  public static Collection<EObject> getElementToContainerCache(EObject capellaElement) {
    return elementToContainerCache.get(capellaElement);
  }

  /**
   * Put element -> container in cache.
   * 
   * @param element
   *          EObject
   * @param container
   *          Collection<EObject>
   */
  public static void putElementToContainerCache(EObject element, Collection<EObject> container) {
    elementToContainerCache.put(element, container);
  }

  /**
   * @param key
   *          InteractionFragment
   * @return Event corresponding to key.
   */
  public static Optional<CapellaElement> getEndToEventCache(InteractionFragment key) {
    return Optional.ofNullable(endToEventCache.get(key));
  }

  /**
   * Put end -> Event in cache.
   * 
   * @param key
   *          InteractionFragment
   * @param value
   *          Event
   */
  public static void putEndToEventCache(InteractionFragment key, CapellaElement value) {
    endToEventCache.put(key, value);
  }

  /**
   * @return endToEventCache
   */
  public static Map<InteractionFragment, CapellaElement> getEndToEventCache() {
    return endToEventCache;
  }

  /**
   * @return if refresh cache is enabled
   */
  public static boolean isRefreshCacheEnabled() {
    return isCacheEnabled;
  }

}
