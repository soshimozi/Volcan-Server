package com.mvc.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JPanel;

/**
 * This class provides the base level abstraction for views in this example. All
 * views that extend this class also extend JPanel (which is useful for performing
 * GUI manipulations on the view in NetBeans Matisse), as well as providing the 
 * modelPropertyChange() method that controllers can use to propogate model 
 * property changes.
 *
 * @author Robert Eckstein
 */

public abstract class AbstractViewPanel extends JPanel {

    private final List<ActionListener> listeners = new CopyOnWriteArrayList<ActionListener>();
    
    public void addActionListener(ActionListener l) {
        listeners.add(l);
    }
    
    public void removeActionListener(ActionListener l) {
        listeners.remove(l);
    }
    
    protected void fireAction(ActionEvent evt) {
    
        for (ActionListener l : listeners) {
            l.actionPerformed(evt);        
        }
    }
    
    /**
     * Called by the controller when it needs to pass along a property change 
     * from a model.
     *
     * @param evt The property change event from the model
     */
    public abstract void modelPropertyChange(PropertyChangeEvent evt);
	
}
