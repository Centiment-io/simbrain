
package org.simbrain.network.nodes;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.BasicStroke;

import java.awt.event.ActionEvent;

import java.awt.geom.Point2D;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Iterator;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;

import edu.umd.cs.piccolo.PNode;

import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

import edu.umd.cs.piccolo.util.PBounds;

import org.simbrain.network.NetworkPanel;
import org.simbrain.network.dialog.network.CompetitivePropertiesDialog;
import org.simbrain.network.dialog.network.DiscreteHopfieldPropertiesDialog;
import org.simbrain.network.dialog.network.WTAPropertiesDialog;
import org.simnet.interfaces.Network;
import org.simnet.networks.Competitive;
import org.simnet.networks.DiscreteHopfield;
import org.simnet.networks.WinnerTakeAll;

/**
 * Subnetwork node.
 *
 * <p>
 * Node composition:
 * <pre>
 * SubnetworkNode extends PNode
 *   |
 *   + -- TabNode extends ScreenElement
 *   |      |
 *   |      + -- PText, for label
 *   |      |
 *   |      + -- PPath, for outline and background
 *   |
 *   + -- OutlineNode extends PPath
 *          |
 *          + -- ... (child nodes)
 * </pre>
 * </p>
 */
public abstract class SubnetworkNode
    extends PNode {

    /** Tab height. */
    private static final double TAB_HEIGHT = 22.0d;

    /** Default tab width. */
    private static final double DEFAULT_TAB_WIDTH = 100.0d;

    /** Tab inset or border height. */
    private static final double TAB_INSET_HEIGHT = 5.0d;

    /** Tab inset or border width. */
    private static final double TAB_INSET_WIDTH = 6.0d;

    /** Outline inset or border height. */
    public static final double OUTLINE_INSET_HEIGHT = 12.0d;

    /** Outline inset or border width. */
    public static final double OUTLINE_INSET_WIDTH = 12.0d;

    /** Default outline height. */
    private static final double DEFAULT_OUTLINE_HEIGHT = 150.0d;

    /** Default outline width. */
    private static final double DEFAULT_OUTLINE_WIDTH = 150.0d;

    /** Default tab paint. */
    private static final Paint DEFAULT_TAB_PAINT = Color.LIGHT_GRAY;

    /** Default tab stroke paint. */
    private static final Paint DEFAULT_TAB_STROKE_PAINT = Color.DARK_GRAY;

    /** Default outline stroke. */
    private static final Stroke DEFAULT_OUTLINE_STROKE = new BasicStroke(1.0f);

    /** Default outline stroke paint. */
    private static final Paint DEFAULT_OUTLINE_STROKE_PAINT = Color.LIGHT_GRAY;

    /** Reference to model subnetwork. */
    private Network subnetwork;

    /** Tab node. */
    private TabNode tab;

    /** Outline node. */
    private OutlineNode outline;

    /** The tab paint for this subnetwork node. */
    private Paint tabPaint;

    /** The tab stroke paint for this subnetwork node. */
    private Paint tabStrokePaint;

    /** The last outline stroke, if any. */
    private Stroke lastOutlineStroke;

    /** The outline stroke paint for this subnetwork node. */
    private Paint outlineStrokePaint;

    /** The label for this subnetwork node. */
    private String label;

    /** True if this subnetwork node is to show its outline. */
    private boolean showOutline;

    /** Intial child layout complete. */
    private boolean initialChildLayoutComplete;


    /**
     * Create a new subnetwork node.
     *
     * @param networkPanel network panel
     * @param subnetwork subnetwork
     * @param x x
     * @param y y
     */
    protected SubnetworkNode(final NetworkPanel networkPanel,
                             final Network subnetwork,
                             final double x, final double y) {
        super();

        initialChildLayoutComplete = false;

        offset(x, y);
        setPickable(false);
        setChildrenPickable(true);

        this.subnetwork = subnetwork;
        tabPaint = DEFAULT_TAB_PAINT;
        tabStrokePaint = DEFAULT_TAB_STROKE_PAINT;
        outlineStrokePaint = DEFAULT_OUTLINE_STROKE_PAINT;
        label = subnetwork.getType();
        showOutline = true;

        tab = new TabNode(networkPanel, x, y);
        outline = new OutlineNode();

        super.addChild(outline);
        super.addChild(tab);
    }


    /**
     * Return <code>true</code> if this subnetwork node has a context menu.
     * If this subnetwork node does not have a context menu, a context menu
     * event handler will not be registered.
     *
     * @see #getContextMenu
     * @return true if this subnetwork node has a context menu.
     */
    protected abstract boolean hasContextMenu();

    /**
     * Return a context menu specific to this subnetwork node.  Return
     * <code>null</code> if this subnetwork node does not have a context
     * menu.
     *
     * @see #hasContextMenu
     * @return a context menu specific to this subnetwork node
     */
    protected abstract JPopupMenu getContextMenu();

    /**
     * Return <code>true</code> if this subnetwork node has a property dialog.
     * If this subnetwork node does not have a property dialog, a property
     * dialog event handler will not be registered.
     *
     * @see #getPropertyDialog
     * @return true if this subnetwork node has a property dialog
     */
    protected abstract boolean hasPropertyDialog();

    /**
     * Return a property dialog for this subnetwork node.  Return
     * <code>null</code> if this subnetwork node does not have a
     * property dialog.
     *
     * @see #hasPropertyDialog
     * @return a property dialog for this subnetwork node
     */
    protected abstract JDialog getPropertyDialog();

    /**
     * Update the synapse node positions of any child neuron nodes.
     */
    public final void updateSynapseNodePositions() {
        for (Iterator i = outline.getChildrenIterator(); i.hasNext(); ) {
            PNode node = (PNode) i.next();
            if (node instanceof NeuronNode) {
                NeuronNode neuronNode = (NeuronNode) node;
                neuronNode.updateSynapseNodePositions();
            }
        }
    }

    /** @see PNode */
    protected final void layoutChildren() {
        if (!initialChildLayoutComplete) {
            outline.updateOutlineBoundsAndPath();
            initialChildLayoutComplete = true;
        }

        // attach the outline to the lower left corner of the tab
        Point2D lowerLeft = new Point2D.Double(0.0d, TAB_HEIGHT);
        lowerLeft = tab.localToParent(lowerLeft);
        outline.setOffset(lowerLeft.getX(), lowerLeft.getY());

        updateSynapseNodePositions();
    }

    /** @see PNode */
    public final void addChild(final PNode child) {
        // add all child nodes to outline instead of this
        outline.addChild(child);
        child.addPropertyChangeListener("fullBounds", outline);
    }

    /** @see PNode */
    public final PNode removeChild(final PNode child) {
        PNode ret = outline.removeChild(child);
        outline.updateOutlineBoundsAndPath();
        return ret;
    }

    /**
     * Return the logical subnetwork this node represents.
     *
     * @return the model subnetwork
     */
    public final Network getSubnetwork() {
        return subnetwork;
    }

    //
    // bound properties

    /**
     * Return the tab paint for this subnetwork node.
     * The tab paint will not be null.
     *
     * @return the tab paint for this subnetwork node
     */
    public final Paint getTabPaint() {
        return tabPaint;
    }

    /**
     * Set the tab paint for this subnetwork node to <code>tabPaint</code>.
     *
     * <p>This is a bound property.</p>
     *
     * @param tabPaint tab paint for this subnetwork node, must not be null
     */
    public final void setTabPaint(final Paint tabPaint) {
        if (tabPaint == null) {
            throw new IllegalArgumentException("tabPaint must not be null");
        }

        Paint oldTabPaint = this.tabPaint;
        this.tabPaint = tabPaint;
        tab.setTabPaint(this.tabPaint);
        firePropertyChange("tabPaint", oldTabPaint, this.tabPaint);
    }

    /**
     * Return the tab stroke paint for this subnetwork node.
     * The tab stroke paint will not be null.
     *
     * @return the tab stroke paint for this subnetwork node
     */
    public final Paint getTabStrokePaint() {
        return tabStrokePaint;
    }

    /**
     * Set the tab stroke paint for this subnetwork node to <code>tabStrokePaint</code>.
     *
     * <p>This is a bound property.</p>
     *
     * @param tabStrokePaint tab stroke paint for this subnetwork node, must not be null
     */
    public final void setTabStrokePaint(final Paint tabStrokePaint) {
        if (tabStrokePaint == null) {
            throw new IllegalArgumentException("tabStrokePaint must not be null");
        }

        Paint oldTabStrokePaint = this.tabStrokePaint;
        this.tabStrokePaint = tabStrokePaint;
        tab.setTabStrokePaint(tabStrokePaint);
        firePropertyChange("tabStrokePaint", oldTabStrokePaint, this.tabStrokePaint);
    }

    /**
     * Return the outline stroke paint for this subnetwork node.
     * The outline stroke paint will not be null.
     *
     * @return the outline stroke paint for this subnetwork node
     */
    public final Paint getOutlineStrokePaint() {
        return outlineStrokePaint;
    }

    /**
     * Set the outline stroke paint for this subnetwork node to <code>outlineStrokePaint</code>.
     *
     * <p>This is a bound property.</p>
     *
     * @param outlineStrokePaint outline stroke paint for this subnetwork node, must not be null
     */
    public final void setOutlineStrokePaint(final Paint outlineStrokePaint) {
        if (outlineStrokePaint == null) {
            throw new IllegalArgumentException("outlineStrokePaint must not be null");
        }

        Paint oldOutlineStrokePaint = this.outlineStrokePaint;
        this.outlineStrokePaint = outlineStrokePaint;
        outline.setStrokePaint(this.outlineStrokePaint);
        firePropertyChange("outlineStrokePaint", oldOutlineStrokePaint, this.outlineStrokePaint);
    }

    /**
     * Return the label for this subnetwork node.
     * The label may be null.
     *
     * @return the label for this subnetwork node
     */
    public final String getLabel() {
        return label;
    }

    /**
     * Set the label for this subnetwork node to <code>label</code>.
     *
     * <p>This is a bound property.</p>
     *
     * @param label label for this subnetwork node
     */
    public final void setLabel(final String label) {
        String oldLabel = this.label;
        this.label = label;
        tab.setLabel(this.label);
        firePropertyChange("label", oldLabel, this.label);
    }

    /**
     * Return true if this subnetwork node is to show its outline.
     *
     * @return true if this subnetwork node is to show its outline
     */
    public final boolean getShowOutline() {
        return showOutline;
    }

    /**
     * Set to true if this subnetwork node is to show its outline.
     *
     * <p>This is a bound property.</p>
     *
     * @param showOutline true if this subnetwork node is to show its outline
     */
    public final void setShowOutline(final boolean showOutline) {
        boolean oldShowOutline = this.showOutline;
        this.showOutline = showOutline;

        if (oldShowOutline != this.showOutline) {
            if (this.showOutline) {
                if (lastOutlineStroke == null) {
                    outline.setStroke(lastOutlineStroke);
                } else {
                    outline.setStroke(DEFAULT_OUTLINE_STROKE);
                }
            } else {
                lastOutlineStroke = outline.getStroke();
                outline.setStroke(null);
            }
        }

        firePropertyChange("showOutline", Boolean.valueOf(oldShowOutline), Boolean.valueOf(showOutline));
    }


    /**
     * Tab node.
     */
    private final class TabNode
        extends ScreenElement {

        /** Label. */
        private PText label;

        /** Background. */
        private PPath background;


        /**
         * Create a new tab node.
         *
         * @param networkPanel network panel
         * @param x x
         * @param y y
         */
        public TabNode(final NetworkPanel networkPanel, final double x, final double y) {
            super(networkPanel);

            setPickable(true);
            setChildrenPickable(false);
            setOffset(0.0d, -1 * TAB_HEIGHT);

            label = new PText(getLabel());
            label.offset(TAB_INSET_HEIGHT, TAB_INSET_WIDTH);

            double backgroundWidth = Math.max(label.getWidth() + (2 * TAB_INSET_WIDTH), DEFAULT_TAB_WIDTH);

            background = PPath.createRectangle(0.0f, 0.0f, (float) backgroundWidth, (float) TAB_HEIGHT);
            background.setPaint(getTabPaint());
            background.setStrokePaint(getTabStrokePaint());

            setBounds(0.0d, 0.0d, backgroundWidth, TAB_HEIGHT);
            addChild(background);
            addChild(label);
        }


        /** @see ScreenElement */
        public boolean isSelectable() {
            return true;
        }

        /** @see ScreenElement */
        public boolean showSelectionHandle() {
            return false;
        }

        /** @see ScreenElement */
        public boolean isDraggable() {
            return true;
        }

        /** @see ScreenElement */
        protected boolean hasToolTipText() {
            return true;
        }

        /** @see ScreenElement */
        protected String getToolTipText() {
            return getLabel();
        }

        /** @see ScreenElement */
        protected boolean hasContextMenu() {
            return SubnetworkNode.this.hasContextMenu();
        }

        /** @see ScreenElement */
        protected JPopupMenu getContextMenu() {
            return SubnetworkNode.this.getContextMenu();
        }

        /** @see ScreenElement */
        protected boolean hasPropertyDialog() {
            return SubnetworkNode.this.hasPropertyDialog();
        }

        /** @see ScreenElement */
        protected JDialog getPropertyDialog() {
            return SubnetworkNode.this.getPropertyDialog();
        }

        /** @see ScreenElement */
        public void resetColors() {
            // empty
        }

        /**
         * Set the tab paint to <code>tabPaint</code>.
         *
         * @param tabPaint tab paint
         */
        private void setTabPaint(final Paint tabPaint) {
            background.setPaint(tabPaint);
        }

        /**
         * Set the tab stroke paint to <code>tabStrokePaint</code>.
         *
         * @param tabStrokePaint tab stroke paint
         */
        private void setTabStrokePaint(final Paint tabStrokePaint) {
            background.setStrokePaint(tabStrokePaint);
        }

        /**
         * Set the label text to <code>labelText</code>.
         *
         * @param labelText label text
         */
        private void setLabel(final String labelText) {
            label.setText(labelText);
            double backgroundWidth = Math.max(label.getWidth() + (2 * TAB_INSET_WIDTH), DEFAULT_TAB_WIDTH);
            background.setPathToRectangle(0.0f, 0.0f, (float) backgroundWidth, (float) TAB_HEIGHT);
            setBounds(0.0d, 0.0d, backgroundWidth, TAB_HEIGHT);
        }
    }

    /**
     * Outline node.
     */
    private final class OutlineNode
        extends PPath
        implements PropertyChangeListener {

        /**
         * Create a new outline node.
         */
        public OutlineNode() {
            super();

            setPickable(false);
            setChildrenPickable(true);
            setBounds(0.0d, 0.0d, DEFAULT_OUTLINE_WIDTH, DEFAULT_OUTLINE_HEIGHT);
            setPathToRectangle(0.0f, 0.0f, (float) DEFAULT_OUTLINE_WIDTH, (float) DEFAULT_OUTLINE_HEIGHT);

            setStrokePaint(DEFAULT_OUTLINE_STROKE_PAINT);
        }


        /**
         * Update outline bounds and path.
         */
        public void updateOutlineBoundsAndPath() {

            // one of the child nodes' full bounds changed
            PBounds bounds = new PBounds();
            for (Iterator i = getChildrenIterator(); i.hasNext(); ) {
                PNode child = (PNode) i.next();
                PBounds childBounds = child.getBounds();
                child.localToParent(childBounds);
                bounds.add(childBounds);
            }

            // add (0.0d, 0.0d)
            bounds.add(OUTLINE_INSET_WIDTH, OUTLINE_INSET_HEIGHT);
            // add border
            bounds.setRect(bounds.getX() - OUTLINE_INSET_WIDTH,
                           bounds.getY() - OUTLINE_INSET_HEIGHT,
                           bounds.getWidth() + (2 * OUTLINE_INSET_WIDTH),
                           bounds.getHeight() + (2 * OUTLINE_INSET_HEIGHT));

            // set outline to new bounds
            // TODO:  only update rect if it needs updating
            setBounds(bounds);
            setPathToRectangle((float) bounds.getX(), (float) bounds.getY(),
                               (float) bounds.getWidth(), (float) bounds.getHeight());
        }


        /** @see PropertyChangeListener */
        public void propertyChange(final PropertyChangeEvent event) {
            updateOutlineBoundsAndPath();
        }
    }
}
