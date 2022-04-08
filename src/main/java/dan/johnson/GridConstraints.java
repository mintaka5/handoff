package dan.johnson;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * GridBag helper.
 *
 * @author dan.johnson
 * @version 1.0
 */
public class GridConstraints extends GridBagConstraints {

    private Insets defaultInsets;

    public GridConstraints() {
        super();
        insets = defaultInsets = new Insets(5, 5, 5, 5);
    }

    public void setDefaultInsets(Insets defaultInsets) {
        this.defaultInsets = (Insets) defaultInsets.clone();
    }

    public GridConstraints fillx() {
        fill = HORIZONTAL;
        weightx = 1.0;
        return this;
    }

    public GridConstraints filly() {
        fill = VERTICAL;
        weighty = 1.0;
        return this;
    }

    public GridConstraints fillboth() {
        fill = BOTH;
        weightx = 1.0;
        weighty = 1.0;
        return this;
    }

    public GridConstraints endrow() {
        gridwidth = REMAINDER;
        return this;
    }

    private void reset() {
        weightx = 0.0;
        weighty = 0.0;
        fill = NONE;
        gridx = RELATIVE;
        gridwidth = RELATIVE;
        gridy = RELATIVE;
        gridheight = RELATIVE;
        insets = defaultInsets;
    }

    @Override
    public Object clone() {
        try {
            GridConstraints gc = (GridConstraints) super.clone();
            gc.insets = insets;
            this.reset();
            return gc;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
