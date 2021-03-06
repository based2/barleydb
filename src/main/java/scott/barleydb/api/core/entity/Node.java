package scott.barleydb.api.core.entity;

/*
 * #%L
 * BarleyDB
 * %%
 * Copyright (C) 2014 Scott Sinclair <scottysinclair@gmail.com>
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import scott.barleydb.api.config.NodeType;
import scott.barleydb.api.core.entity.Entity;
import scott.barleydb.api.core.entity.EntityContext;
import scott.barleydb.api.core.entity.NodeEvent;

public abstract class Node implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Entity parent;

    private final String name;

    public Node(final Entity parent, final String name) {
        this.parent = parent;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public EntityContext getEntityContext() {
        return parent.getEntityContext();
    }

    public Entity getParent() {
        return parent;
    }

    public void handleEvent(NodeEvent event) {
        if (parent != null) {
            parent.handleEvent(event);
        }
    }

    public NodeType getNodeType() {
        return parent.getEntityType().getNodeType(name, true);
    }

    public abstract Element toXml(Document doc);

}
