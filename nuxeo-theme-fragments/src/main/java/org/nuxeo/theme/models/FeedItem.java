/*
 * (C) Copyright 2006-2007 Nuxeo SAS <http://nuxeo.com> and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jean-Marc Orliaguet, Chalmers
 *
 * $Id$
 */

package org.nuxeo.theme.models;

import java.util.Calendar;

import org.nuxeo.theme.models.AbstractModel;
import org.nuxeo.theme.nodes.NodeTypeFamily;

public class FeedItem extends AbstractModel {

    private String title;

    private String description;

    private Calendar date;
    
    private String creator;

    private String url;

    public FeedItem() {
    }

    @Override
    public String getModelTypeName() {
        return "feed item";
    }

    @Override
    public NodeTypeFamily getNodeTypeFamily() {
        return NodeTypeFamily.LEAF;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }
    
    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

}
