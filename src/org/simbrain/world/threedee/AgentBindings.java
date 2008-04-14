package org.simbrain.world.threedee;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.simbrain.workspace.ConsumingAttribute;
import org.simbrain.workspace.ProducingAttribute;
import org.simbrain.workspace.WorkspaceComponent;
import org.simbrain.world.threedee.Moveable.Action;
import org.simbrain.world.threedee.sensors.Smell;

/**
 * Acts as the consumer and producer associated with an Agent.
 * 
 * @author Matt Watson
 */
class AgentBindings extends Bindings {
    /**
     * The priority of the agent.  There's nothing special about the number
     * 10.  It's just sufficiently high to allow others to preempt bindings.
     */
    private static final int PRIORITY = 10;

    /** The consumers for the wrapped agent. */
    private final List<ConsumingBinding> consumers = new ArrayList<ConsumingBinding>();
    /** The agent for these bindings. */
    private final Agent agent;
    
    /**
     * Creates a new bindings object for the given agent
     * and component.
     *
     * @param agent the agent to bind to.
     * @param component the parent component.
     */
    AgentBindings(final Agent agent, final WorkspaceComponent<?> component) {
        super(component, "3D Agent " + agent.getName());

        this.agent = agent;
        
        consumers.add(new ConsumingBinding("left", agent.left()));
        consumers.add(new ConsumingBinding("right", agent.right()));
        consumers.add(new ConsumingBinding("forward", agent.forward()));
        consumers.add(new ConsumingBinding("backward", agent.backward()));
        setDefaultConsumingAttribute(consumers.get(0));
        setDefaultProducingAttribute(new ProducingBinding(
            new Smell(agent.getOdors().get(0).getName(), agent)));
        
        agent.addInput(PRIORITY, new AbstractCollection<Action>() {
            @Override
            public Iterator<Action> iterator() {
                if (doBind()) {
                    /*
                     * set the bind value to the on parameter.  This is not done until
                     * the iterator it to be created to ensure at least one iterator
                     * is returned every time the bindings are turned on.
                     */
                    setBindToOn();
                    
                    final Iterator<ConsumingBinding> internal = consumers.iterator();
    
                    return new Iterator<Action>() {
    
                      public boolean hasNext() {
                          return internal.hasNext();
                      }
    
                      public Action next() {
                          return internal.next().getAction();
                      }
    
                      public void remove() {
                          throw new UnsupportedOperationException();
                      }
                   };
                } else {
                    return Collections.<Action>emptySet().iterator();
                }
            }

            @Override
            public int size() {
                return doBind() ? consumers.size() : 0;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public List<? extends ProducingAttribute<?>> getProducingAttributes() {
        List<ProducingBinding> producing = new ArrayList<ProducingBinding>();
        
        for (String odorType : agent.getEnvironment().getOdors().getOdorTypes()) {
            producing.add(new ProducingBinding(new Smell(odorType, agent)));
        }
        
        return producing;
    }

    /**
     * {@inheritDoc}
     */
    public List<? extends ConsumingAttribute<?>> getConsumingAttributes() {
        return Collections.unmodifiableList(consumers);
    }
}
