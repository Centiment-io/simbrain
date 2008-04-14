package org.simbrain.world.threedee.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.simbrain.world.threedee.Agent;
import org.simbrain.world.threedee.AgentElement;
import org.simbrain.world.threedee.Collision;
import org.simbrain.world.threedee.Element;
import org.simbrain.world.threedee.SpatialData;
import org.simbrain.world.threedee.Viewable;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;

/**
 * Represents the viewable world and is the container for adding elements and
 * views to that world.
 * 
 * @author Matt Watson
 */
public class Environment {
    /** The number of milliseconds between refresh events. */
    public static final int REFRESH_WAIT = 10;

    /** The static logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(Environment.class);

    /** Timer that fires the update operation. */
    private Timer timer;

    /** The elements in this environment. */
    private final Map<Renderer, Node> parents = new HashMap<Renderer, Node>();
    
    /** The elements in this environment. */
    private final List<Element> elements = new ArrayList<Element>();

    /** All the views on this environment. */
    private final List<Viewable> views = new ArrayList<Viewable>();

    /** The odors in this environment. */
    private final Odors odors = new Odors();
    
    /** The size of the environment. */
    private final int size = 256;
    
    /** The terrain for the environment. */
    private final Terrain terrain = new Terrain(size);

    /** The sky for the environment. */
//    private Sky sky = new Sky();
    
    /**
     * Creates a new environment.
     */
    public Environment() {
        elements.add(terrain);
//        elements.add(sky);
    }

    /**
     * Adds an agent to this environment.
     * 
     * @param agent the agent to add
     */
    public void add(final Agent agent) {
        AgentElement element = new AgentElement(agent);
        
        elements.add(element);
        
        for (Map.Entry<Renderer, Node> parent : parents.entrySet()) {
            element.init(parent.getKey(), parent.getValue());
        }
        
        Random random = new Random();
        views.add(agent);
        agent.setEnvironment(this);
        
        int limit = (size * 2) - 1;
        
        agent.setLimit(limit);
        
        agent.commit();
        
        boolean collided;
        
        do {
            collided = false;
//            element.update();
            agent.setHeight();
            
            for (Element other : elements) {
                if (other == element) continue;
//                other.update();
                
//                SpatialData elementT = element.getTentative();
//                SpatialData otherT = other.getTentative();
                
//                System.out.println("element: " + elementT.centerPoint());
//                System.out.println("other: " + (otherT == null ? null : otherT.centerPoint()));
                
                if (element.getTentative().intersects(other.getTentative())) {
//                    System.out.println("collision");
                    agent.getLocation().setX(random.nextFloat() % limit);
                    agent.getLocation().setZ(random.nextFloat() % limit);
                    
                    collided = true;
                    break;
                }
            }
        } while (collided);
        
        element.commit();
        odors.addOdors(agent);
    }

    /**
     * Adds a new view.
     * 
     * @param view the view to add
     */
    public void addViewable(final Viewable view) {
        views.add(view);
    }

    /**
     * Returns the floor height at the x and z coordinates of the given point.
     * 
     * @param location The location to get the floor height at.
     * @return The floor height at the x and z coordinates of the given point.
     */
    public float getFloorHeight(final Vector3f location) {
        return terrain.getHeight(location);
    }

    /**
     * Initializes the environment with the given renderer and parent.
     * 
     * @param renderer the renderer
     * @param parent the parent node
     */
    public void init(final Renderer renderer, final Node parent) {
        LOGGER.debug("init: " + renderer);

        parents.put(renderer, parent);
        
        for (final Element element : elements) {
            LOGGER.debug("element: " + element);
            element.init(renderer, parent);
        }

        parent.setModelBound(new BoundingBox());
        parent.updateModelBound();

        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, REFRESH_WAIT, REFRESH_WAIT);
    }

    /**
     * Calls updates on all the elements, looks for collisions, fires any
     * collision events, commits the elements, and updates the views.
     */
    public void update() {
        LOGGER.trace("update");

        for (final Element element : elements) {
            element.update();
        }

        for (int i = 0; i < elements.size(); i++) {
            final Element a = elements.get(i);
            final SpatialData aData = a.getTentative();

            if (aData == null) { continue; }

            for (int j = i + 1; j < elements.size(); j++) {
                final Element b = elements.get(j);
                final SpatialData bData = b.getTentative();

                if (bData == null) { continue; }

                if (aData.intersects(bData)) {
                    final CollisionData data = new CollisionData(a, b);

                    a.collision(data.collisionA);
                    b.collision(data.collisionB);
                }
            }
        }

        for (final Element element : elements) {
            element.commit();
        }

        for (final Viewable view : views) {
            view.updateView();
        }
    }
    
    /**
     * Returns the odors in this environment.
     * 
     * @return The odors in this environment.
     */
    public Odors getOdors() {
        return odors;
    }
    
    /**
     * Helper class for managing collision data temporarily creates the
     * collision data objects for both elements involved in the collision.
     * 
     * @author Matt Watson
     */
    private static class CollisionData {
        /** The collision data for the first element. */
        private final Collision collisionA;
        /** The collision data for the second element. */
        private final Collision collisionB;

        /**
         * Creates a new CollisionData instance.
         * 
         * @param a The first element.
         * @param b The second element.
         */
        CollisionData(final Element a, final Element b) {
            /**
             * Inner class for collision calculations.
             * 
             * @author Matt Watson
             */
            class CollisionLocal implements Collision {
                /** The other element. */
                private final Element other;
                /** The point of impact. */
                private final Vector3f point;

                /**
                 * Creates a new CollisionLocal instance.
                 * 
                 * @param other The other element.
                 * @param center The center of the element this collision is respective to.
                 * @param fraction The the fraction representing the how far from the center
                 *        point to the bounding sphere surface the collision took place.
                 */
                CollisionLocal(final Element other, final Vector3f center, final float fraction) {
                    this.other = other;

                    Vector3f point = (Vector3f) center.clone();
                    point.interpolate(other.getTentative().centerPoint(), fraction);

                    this.point = point;
                }

                /**
                 * Returns the other element in the collision.
                 * 
                 * @return The other element in the collision.
                 */
                public Element other() {
                    return other;
                }

                /**
                 * Returns the point of impact.
                 * 
                 * @return The point of impact.
                 */
                public Vector3f point() {
                    return point;
                }

            }
            
            float aRadius = a.getTentative().radius();
            float bRadius = b.getTentative().radius();
            
            final float total = aRadius + bRadius;

            collisionA = new CollisionLocal(b, a.getTentative().centerPoint(), total / aRadius);
            collisionB = new CollisionLocal(a, b.getTentative().centerPoint(), total / bRadius);
        }
    }
}
