/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package snakked.base;

import java.net.URL;

/**
 * basic interface for action items (menu items, buttons etc). It has built-in capability for i18n
 * 
 * the icon properties are supposed to be resolved via the resources.RazRes class into a classpath
 * file
 * 
 * for more details, find the project razactionables and read the details there...
 * 
 * TODO 2-2 implement fully, with property bundle for the labels and actions, including tooltips etc
 * 
 * TODO the arguments are patched on...try to nice-fy via constructors maybe?
 * 
 * TODO how do we generically assign permissions to these ?
 * 
 * @author razvanc99
 * @version $Id$
 */
public class ActionItem implements Cloneable {

    public static final ActionItem[] NOACTIONS     = {};

    /** most actions do not have arguments...for those that do, this contains their defs (name:type=default) */
    public AttrAccess args = null;

    /** A=act(ACT), C=create(PUT), R=read(GET), U=update(POST), D=delete(REMOVE) */
    public enum ActionType { A, C, R, U, D };
    public ActionType actionType = ActionType.R;
  
    /** @return this */
    public ActionItem setType (ActionType t) {
       this.actionType = t;
       return this;
    }
    /**
     * overwrite the label of another action item
     * 
     * @param name action name
     * @param newLabel
     */
    public ActionItem(ActionItem other, String newLabel) {
        this.name = other.name;
        this.iconProp = other.iconProp;
        this.label = newLabel;
    }

    /**
     * @param name action name
     * @param icon an iceon - a pic file
     * @param label the label for this action
     * @param tooltip
     */
    public ActionItem(String name, String icon, String label, String tooltip) {
        this.name = name;
        this.label = label;
        this.tooltip = tooltip;
        this.iconProp = icon;
    }

    /**
     * @param name action name
     * @param icon an icon property - icons are supposed to be mapped via the RazIconRes class
     */
    public ActionItem(String name, String icon) {
        this.name = this.label = name;
        this.iconProp = icon;
    }

    /** this assumes the icon code is the same as the action name */
    public ActionItem(String name) {
        this.name = this.label = name;
        this.iconProp = name;
    }

    /**
     * the icon properties are supposed to be resolved via the resources.RazRes class into a
     * classpath file
     */
    public String getIconProp() {
        return this.iconProp;
    }

    public String getLabel() {
        return this.label;
    }

    public String getTooltip() {
        return this.tooltip;
    }

    public URL getHelpUrl() {
        return this.helpUrl;
    }

    @Override
    public ActionItem clone() {
        ActionItem i = new ActionItem(this.name, this.iconProp);
        i.label = this.label;
        i.tooltip = this.tooltip;
        i.helpUrl = this.helpUrl;
        return i;
    }

    /** only looking at the actual action code */
    public boolean equals(Object another) {
        if (another instanceof ActionItem && ((ActionItem) another).name.equals(this.name))
            return true;
        else if (another instanceof String && name.equals(another))
            return true;
        return false;
    }

    /** only looking at the actual action code */
    public int hashCode() {
        return name.hashCode();
    }

    /** only looking at the actual action code */
    public String toString() {
        return name;
    }

    public String name;
    public String iconProp = "UNKNOWN";
    public String label;
    public String tooltip;
    public URL    helpUrl;
}
