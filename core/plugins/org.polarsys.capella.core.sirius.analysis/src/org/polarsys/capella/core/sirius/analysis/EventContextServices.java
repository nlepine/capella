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
package org.polarsys.capella.core.sirius.analysis;

import static org.polarsys.capella.core.sirius.analysis.refresh.extension.InteractionRefreshExtension.getElementToContainerCache;
import static org.polarsys.capella.core.sirius.analysis.refresh.extension.InteractionRefreshExtension.getEndToEventCache;
import static org.polarsys.capella.core.sirius.analysis.refresh.extension.InteractionRefreshExtension.getInstanceRoleToEventContextCache;
import static org.polarsys.capella.core.sirius.analysis.refresh.extension.InteractionRefreshExtension.getInteractionCache;
import static org.polarsys.capella.core.sirius.analysis.refresh.extension.InteractionRefreshExtension.putElementToContainerCache;
import static org.polarsys.capella.core.sirius.analysis.refresh.extension.InteractionRefreshExtension.putEndToEventCache;
import static org.polarsys.capella.core.sirius.analysis.refresh.extension.InteractionRefreshExtension.putInstanceRoleToEventContextCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.interaction.AbstractEnd;
import org.polarsys.capella.core.data.interaction.AbstractFragment;
import org.polarsys.capella.core.data.interaction.Execution;
import org.polarsys.capella.core.data.interaction.ExecutionEnd;
import org.polarsys.capella.core.data.interaction.InstanceRole;
import org.polarsys.capella.core.data.interaction.InteractionFragment;
import org.polarsys.capella.core.data.interaction.InteractionState;
import org.polarsys.capella.core.data.interaction.MessageEnd;
import org.polarsys.capella.core.data.interaction.Scenario;
import org.polarsys.capella.core.data.interaction.StateFragment;
import org.polarsys.capella.core.data.interaction.TimeLapse;

/**
 * Compute EventContext structure for sequence diagram.
 * 
 * @author nlepine
 *
 */
public class EventContextServices {

  /**
   * Return abstract end covered TimeLapse/InstanceRole.
   * 
   * @param end
   *          AbstractEnd
   * @param instanceRole
   *          InstanceRole
   * @return TimeLapse
   */
  public static Optional<CapellaElement> getEvent(AbstractEnd end, InstanceRole instanceRole) {
    Optional<CapellaElement> timeLapse = getEndToEventCache(end);
    if (!timeLapse.isPresent()) {
      // compute result from EventContext structure
      List<EventContext> eventContexts = getEventContext(instanceRole);

      // get TimeLapse from cache
      // if not present, compute it from EventContext structure
      Optional<EventContext> endEventContext = eventContexts.stream()
          .filter(eventContext -> end.equals(eventContext.getElement())).findFirst();
      if (endEventContext.isPresent() && endEventContext.get().getParent() instanceof CapellaElement) {
        putEndToEventCache(end, (CapellaElement) endEventContext.get().getParent());
      }
      timeLapse = getEndToEventCache(end);
    }
    return timeLapse;
  }

  /**
   * Compute EventContext structure from InstanceRole if needed and put it in cache.
   * 
   * @param instanceRole
   *          InstanceRole
   * @return List<EventContext>
   */
  private static List<EventContext> getEventContext(InstanceRole instanceRole) {
    List<EventContext> eventContexts = getInstanceRoleToEventContextCache(instanceRole);
    if (eventContexts == null) {
      eventContexts = computeInstanceRoleEventContextStructure(instanceRole);
      putInstanceRoleToEventContextCache(instanceRole, eventContexts);
    }
    return eventContexts;
  }

  /**
   * Get direct event on element. (InstanceRole -> Executions + StateFragment, Execution -> Execution children +
   * StateFragment)
   * 
   * @param element
   *          EObject
   * @param instanceRole
   *          InstanceRole
   * @return Collection<EObject>
   */
  public static Collection<EObject> getDirectEvents(EObject element, InstanceRole instanceRole) {
    Collection<EObject> directEvents = getElementToContainerCache(element);
    if (directEvents == null) {
      // compute result from EventContext structure
      List<EventContext> eventContexts = getEventContext(instanceRole);

      directEvents = eventContexts.stream().filter(eventContext -> element.equals(eventContext.getParent()))
          .map(eventContext -> eventContext.getElement()).filter(event -> event != element).distinct()
          .collect(Collectors.toList());
      putElementToContainerCache(element, directEvents);
    }
    return directEvents;
  }

  /**
   * Get Execution direct events on element.
   * 
   * @param element
   *          EObject
   * @param instanceRole
   *          InstanceRole
   * @return Collection<EObject>
   */
  public static Collection<Execution> getExecutionDirectEvents(EObject element, InstanceRole instanceRole) {
    // get executions from cache
    Collection<EObject> directEvents = getElementToContainerCache(element);
    if (directEvents != null) {
      return directEvents.stream().filter(Execution.class::isInstance).map(Execution.class::cast)
          .collect(Collectors.toList());
    }
    // if not present, compute it and put it in cache
    return getDirectEvents(element, instanceRole).stream().filter(Execution.class::isInstance)
        .map(Execution.class::cast).collect(Collectors.toList());
  }

  /**
   * Get StateFragment direct events on element.
   * 
   * @param element
   *          EObject
   * @param instanceRole
   *          InstanceRole
   * @return Collection<EObject>
   */
  public static List<StateFragment> getStateFragmentDirectEvents(EObject element, InstanceRole instanceRole) {
    // get states from cache
    Collection<EObject> directEvents = getElementToContainerCache(element);
    if (directEvents != null) {
      return directEvents.stream().filter(StateFragment.class::isInstance).map(StateFragment.class::cast)
          .collect(Collectors.toList());
    }
    // if not present, compute it and put it in cache
    return getDirectEvents(element, instanceRole).stream().filter(StateFragment.class::isInstance)
        .map(StateFragment.class::cast).collect(Collectors.toList());
  }

  /**
   * Compute EventContext structure for InstanceRole
   * 
   * @param instanceRole
   *          InstanceRole
   * @return List<EventContext>
   */
  public static List<EventContext> computeInstanceRoleEventContextStructure(InstanceRole instanceRole) {
    if (instanceRole == null || !(instanceRole.eContainer() instanceof Scenario)) {
      return Collections.emptyList();
    }
    Scenario scenario = SequenceDiagramServices.getScenario(instanceRole);

    // initialize ancestors
    Stack<EObject> ancestors = new Stack<EObject>();
    ancestors.push(instanceRole);
    List<EventContext> result = new ArrayList<EventContext>();

    // cache Execution.start -> Execution and Execution.finish -> Execution
    // cache StateFragment.start -> Execution and StateFragment.finish -> Execution
    computeTimeLapseStartAndEndCache(scenario);

    // compute Execution/StateFragment and InteractionFragment structure
    Stream<InteractionFragment> ends = scenario.getOwnedInteractionFragments().stream()
        .filter(frag -> frag instanceof AbstractEnd || frag instanceof InteractionState);
    ends.forEachOrdered(end -> {
      InstanceRole covered = getCoveredInstanceRole(end);
      if (covered != null && covered.equals(instanceRole)) {

        // Execution End case
        if (end instanceof ExecutionEnd) {
          visit(ancestors, result, (ExecutionEnd) end);
        }

        // Interaction State case
        if (end instanceof InteractionState) {
          visit(ancestors, result, (InteractionState) end);
        }

        // Message End case
        if (end instanceof MessageEnd) {
          visit(ancestors, result, (MessageEnd) end);
        }

      }
    });
    return result;
  }

  /**
   * Compute EventStructure for MessageEnd.
   * 
   * @param ancestors
   *          Stack<EObject>
   * @param result
   *          List<EventContext>
   * @param end
   *          MessageEnd
   */
  private static void visit(Stack<EObject> ancestors, List<EventContext> result, MessageEnd end) {
    Optional<CapellaElement> container = getEndToEventCache(end);
    if (container.isPresent() && container.get() instanceof Execution
        && ((Execution) container.get()).getStart() == end) {
      result.add(new EventContext(ancestors.peek(), container.get(), true, ancestors.size() + 1));
      ancestors.push(container.get());
    }

    if (container.isPresent() && container.get() instanceof Execution
        && ((Execution) container.get()).getFinish() == end) {
      ancestors.pop();
      result.add(new EventContext(ancestors.peek(), container.get(), false, ancestors.size() + 1));
    }
    if (!container.isPresent()) {
      putEndToEventCache(end, (CapellaElement) ancestors.peek());
    }
  }

  /**
   * Compute EventStructure for InteractionState.
   * 
   * @param ancestors
   *          Stack<EObject>
   * @param result
   *          List<EventContext>
   * @param end
   *          InteractionState
   */
  private static void visit(Stack<EObject> ancestors, List<EventContext> result, InteractionState end) {
    Optional<CapellaElement> container = getEndToEventCache(end);
    if (container.isPresent() && container.get() instanceof StateFragment
        && ((StateFragment) container.get()).getStart() == end) {
      result.add(new EventContext(ancestors.peek(), container.get(), true, ancestors.size() + 1));
    }

    if (container.isPresent() && container.get() instanceof StateFragment
        && ((StateFragment) container.get()).getFinish() == end) {
      result.add(new EventContext(ancestors.peek(), container.get(), false, ancestors.size() + 1));
    }
    if (!container.isPresent()) {
      putEndToEventCache(end, (CapellaElement) ancestors.peek());
    }
  }

  /**
   * Compute EventStructure for ExecutionEnd.
   * 
   * @param ancestors
   *          Stack<EObject>
   * @param result
   *          List<EventContext>
   * @param end
   *          ExecutionEnd
   */
  private static void visit(Stack<EObject> ancestors, List<EventContext> result, ExecutionEnd end) {
    Execution execution = end.getExecution();
    if (execution != null && end.getExecution().getStart() == end) {
      result.add(new EventContext(ancestors.peek(), execution, true, ancestors.size() + 1));
      ancestors.push(execution);
    }
    if (execution != null && end.getExecution().getFinish() == end) {
      ancestors.pop();
      result.add(new EventContext(ancestors.peek(), execution, false, ancestors.size() + 1));
    }
  }

  /**
   * Return covered instance role.
   * 
   * @param end
   *          InteractionFragment
   * @return covered instance role.
   */
  private static InstanceRole getCoveredInstanceRole(InteractionFragment end) {
    InstanceRole covered = null;
    if (end instanceof AbstractEnd) {
      covered = getInteractionCache(AbstractEnd::getCovered, (AbstractEnd) end);
    } else if (end instanceof InteractionState) {
      covered = getInteractionCache(InteractionState::getCovered, (InteractionState) end);
    }
    return covered;
  }

  /**
   * Compute cache for TimeLapse start and finish ends.
   * 
   * @param scenario
   *          Scenario
   */
  private static void computeTimeLapseStartAndEndCache(Scenario scenario) {
    // cache Execution.start -> Execution and Execution.finish -> Execution
    Stream<TimeLapse> timeLapses = scenario.getOwnedTimeLapses().stream()
        .filter(tl -> !(tl instanceof AbstractFragment));
    // scan timelapse only one time
    if (getEndToEventCache().isEmpty()) {
      timeLapses.forEach(e -> {
        if (e.getStart() instanceof MessageEnd) {
          putEndToEventCache(e.getStart(), e);
        }
        if (e.getFinish() instanceof MessageEnd) {
          putEndToEventCache(e.getFinish(), e);
        }
        if (e.getStart() instanceof InteractionState) {
          putEndToEventCache(e.getStart(), e);
        }
        if (e.getFinish() instanceof InteractionState) {
          putEndToEventCache(e.getFinish(), e);
        }
      });
    }
  }

  /**
   * Helper class to keep track of who "contains" who depending on the interleaving of the start/finish ends.
   * 
   * @author pcdavid
   */
  public static final class EventContext {
    private final EObject parent;

    private final boolean start;

    private final EObject element;

    private final int level;

    public EventContext(EObject parent, EObject element, boolean start, int level) {
      this.parent = parent;
      this.element = element;
      this.level = level;
      this.start = start;
    }

    public boolean isStart() {
      return start;
    }

    public EObject getParent() {
      return parent;
    }

    public EObject getElement() {
      return element;
    }

    public int getLevel() {
      return level;
    }

    @Override
    public String toString() {
      return String.format("%02d\t%s\t%s", getLevel(), element, parent);
    }
  }

}
