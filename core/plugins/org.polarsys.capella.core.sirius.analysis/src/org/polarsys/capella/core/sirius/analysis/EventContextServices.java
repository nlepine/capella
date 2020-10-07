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

import static org.polarsys.capella.core.sirius.analysis.refresh.extension.InteractionRefreshExtension.getDirectEventsFromCache;
import static org.polarsys.capella.core.sirius.analysis.refresh.extension.InteractionRefreshExtension.getInstanceRoleToEventContextCache;
import static org.polarsys.capella.core.sirius.analysis.refresh.extension.InteractionRefreshExtension.getInteractionCache;
import static org.polarsys.capella.core.sirius.analysis.refresh.extension.InteractionRefreshExtension.getInteractionFragmentToTimeLapseCache;
import static org.polarsys.capella.core.sirius.analysis.refresh.extension.InteractionRefreshExtension.getTimeLapseFromCache;
import static org.polarsys.capella.core.sirius.analysis.refresh.extension.InteractionRefreshExtension.isRefreshCacheEnabled;
import static org.polarsys.capella.core.sirius.analysis.refresh.extension.InteractionRefreshExtension.putDirectEventsInCache;
import static org.polarsys.capella.core.sirius.analysis.refresh.extension.InteractionRefreshExtension.putInstanceRoleToEventContextsCache;
import static org.polarsys.capella.core.sirius.analysis.refresh.extension.InteractionRefreshExtension.putTimeLapseInCache;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
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
import org.polarsys.capella.core.data.interaction.SequenceMessage;
import org.polarsys.capella.core.data.interaction.StateFragment;
import org.polarsys.capella.core.data.interaction.TimeLapse;

/**
 * Compute EventContext structure for sequence diagram.
 * 
 * @author nlepine
 *
 */
public class EventContextServices {

  private EventContextServices() {
    super();
  }

  /**
   * Compute EventContext structure from InstanceRole if needed and put it in cache.
   * 
   * @param instanceRole
   *          InstanceRole
   * @return List<EventContext>
   */
  public static List<EventContext> getEventContexts(InstanceRole instanceRole) {
    List<EventContext> eventContexts = getInstanceRoleToEventContextCache(instanceRole);
    if (eventContexts == null) {
      eventContexts = computeInstanceRoleEventContextStructure(instanceRole);
      if (isRefreshCacheEnabled()) {
        putInstanceRoleToEventContextsCache(instanceRole, eventContexts);
      }
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
    Collection<EObject> directEvents = getDirectEventsFromCache(element);
    if (directEvents == null) {
      // compute result from EventContext structure
      List<EventContext> eventContexts = getEventContexts(instanceRole);

      directEvents = eventContexts.stream().filter(eventContext -> element.equals(eventContext.getParent()))
          .map(EventContext::getElement).filter(event -> event != element).distinct().collect(Collectors.toList());
      if (isRefreshCacheEnabled()) {
        putDirectEventsInCache(element, directEvents);
      }
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
    Collection<EObject> directEvents = getDirectEventsFromCache(element);
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
    Collection<EObject> directEvents = getDirectEventsFromCache(element);
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
    Deque<CapellaElement> ancestors = new ArrayDeque<>();
    ancestors.push(instanceRole);
    List<EventContext> result = new ArrayList<>();

    // Cache missing info from M2 :
    // - cache Execution.start -> Execution and Execution.finish -> Execution when the end is a MessageEnd
    // - cache StateFragment.start -> InteractionState and StateFragment.finish -> InteractionState
    // (ExecutionEnd has a getExecution() method)(ExecutionEnd has a getExecution() method)
    computeTimeLapseStartAndEndCache(scenario);

    // compute Execution/StateFragment and InteractionFragment structure
    Stream<InteractionFragment> ends = scenario.getOwnedInteractionFragments().stream()
        .filter(frag -> frag instanceof AbstractEnd || frag instanceof InteractionState);
    ends.forEachOrdered(end -> {
      InstanceRole covered = getCoveredInstanceRole(end);
      if (covered != null && covered.equals(instanceRole)) {
        Optional<TimeLapse> timeLapse = getTimeLapseFromCache(end);

        // Execution End case
        if (end instanceof ExecutionEnd) {
          visit(ancestors, result, (ExecutionEnd) end, timeLapse);
        }

        // Interaction State case
        if (end instanceof InteractionState) {
          visit(ancestors, result, (InteractionState) end, timeLapse);
        }

        // Message End case
        if (end instanceof MessageEnd) {
          visit(ancestors, result, (MessageEnd) end, timeLapse);
        }

      }
    });
    return result;
  }

  /**
   * Compute EventStructure for MessageEnd.
   * 
   * @param ancestors
   *          Deque<EObject>
   * @param result
   *          List<EventContext>
   * @param end
   *          MessageEnd
   */
  private static void visit(Deque<CapellaElement> ancestors, List<EventContext> result, MessageEnd end,
      Optional<TimeLapse> timeLapse) {

    // Execution start / SyncCall and ASyncCall
    if (timeLapse.isPresent() && timeLapse.get() instanceof Execution
        && ((Execution) timeLapse.get()).getStart() == end) {
      result.add(new EventContext(ancestors.peek(), timeLapse.get(), true, ancestors.size() + 1));
      ancestors.push(timeLapse.get());
    }

    // Message - EventContext to directly know which execution/instanceRole DNode must be source/target of the
    // message's DEdge
    // Handle all cases :
    // - sending end of the main branch (no execution found in endToEventCache)
    // - receiving end of the main branch (start of execution found in endToEvent cache and pushed on the ancestor
    // stack)
    // - sending end of the return branch (end of execution found in endToEvent cache, not yet removed from the
    // ancestor cache)
    // - receiving end of the return branch (no execution found in endToEvent cache).
    SequenceMessage message = end.getMessage();
    if (message != null) {
      result.add(new EventContext(ancestors.peek(), message, end.equals(message.getSendingEnd()), ancestors.size()));
    }

    // Execution with return branch end / SyncCall
    if (timeLapse.isPresent() && timeLapse.get() instanceof Execution
        && ((Execution) timeLapse.get()).getFinish() == end) {
      ancestors.pop();
      result.add(new EventContext(ancestors.peek(), timeLapse.get(), false, ancestors.size() + 1));
    }

    // @formatter:off
        //  The Following diagram would result in
        //
        //     | IR1 |   | IR2 ]   | IR3 ] 
        //        |         |         |
        //        |         |         |
        //        |-------> -         |   e1  m1
        //        |        | |------> -   e2  m2
        //        |        | |       | |
        //        |        | - <-----| |  e3  m3
        //        |        || |      | |
        //        |        || |      | |
        //        |        | - ----->| |      return_m3
        //        |        | |       | |
        //        |<------- -        | |      return_m1
        //        |         |         - 
        //        |         |         | 
        
        // would result the following structures:
        
        //  For IR1: 
        //   EventContext(IR1, m1, true, 1)
        //   EventContext(IR1, return_m1, false, 1)
        //
        //  For IR1: 
        //   EventContext(IR2, e1, true, 2)
        //   EventContext(e1, m1, false, 2)
        //   EventContext(e1, m2, true, 2)
        //   EventContext(e1, e3, true, 3)
        //   EventContext(e3, m3, false, 3)
        //   EventContext(e3, return_m3, true, 3)
        //   EventContext(e1, e3, false, 3)
        //   EventContext(e1, return_m1 true, 2)
        //   EventContext(IR2, e1, false, 2)
        //
        //  For IR3: 
        //   EventContext(IR3, e3, true, 2)
        //   EventContext(e3, m2, false, 2)
        //   EventContext(IR3, e3, false, 2)
        // @formatter:on
  }

  /**
   * Compute EventStructure for InteractionState.
   * 
   * @param ancestors
   *          Deque<EObject>
   * @param result
   *          List<EventContext>
   * @param end
   *          InteractionState
   */
  private static void visit(Deque<CapellaElement> ancestors, List<EventContext> result, InteractionState end,
      Optional<TimeLapse> timeLapse) {
    if (timeLapse.isPresent() && timeLapse.get() instanceof StateFragment
        && ((StateFragment) timeLapse.get()).getStart() == end) {
      result.add(new EventContext(ancestors.peek(), timeLapse.get(), true, ancestors.size() + 1));
    }

    if (timeLapse.isPresent() && timeLapse.get() instanceof StateFragment
        && ((StateFragment) timeLapse.get()).getFinish() == end) {
      result.add(new EventContext(ancestors.peek(), timeLapse.get(), false, ancestors.size() + 1));
    }
  }

  /**
   * Compute EventStructure for ExecutionEnd.
   * 
   * @param ancestors
   *          Deque<EObject>
   * @param result
   *          List<EventContext>
   * @param end
   *          ExecutionEnd
   */
  private static void visit(Deque<CapellaElement> ancestors, List<EventContext> result, ExecutionEnd end,
      Optional<TimeLapse> timeLapse) {
    // Should not happen in current implementation of Capella Sequence diagrams
    if (timeLapse.isPresent() && timeLapse.get().getStart() == end) {
      result.add(new EventContext(ancestors.peek(), timeLapse.get(), true, ancestors.size() + 1));
      ancestors.push(timeLapse.get());
    }

    // Execution without return branch end / ASyncCall
    if (timeLapse.isPresent() && timeLapse.get().getFinish() == end) {
      ancestors.pop();
      result.add(new EventContext(ancestors.peek(), timeLapse.get(), false, ancestors.size() + 1));
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
    // scan timelapse only one time
    if (getInteractionFragmentToTimeLapseCache().isEmpty()) {
      Stream<TimeLapse> timeLapses = scenario.getOwnedTimeLapses().stream()
          .filter(tl -> !(tl instanceof AbstractFragment));
      timeLapses.forEach(e -> {
        // Keep Interaction to TimeLapse info in cache
        // M2 allows to retrieve an Execution from an ExecutionEnd (execution.getFinish() when there is no
        // return branch : ASyncCall)
        // but not an Execution from a MessagEnd (SyncCall and ASyncCall) or a StateFragment from an
        // InteractionState.
        putTimeLapseInCache(e.getStart(), e);
        putTimeLapseInCache(e.getFinish(), e);
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
