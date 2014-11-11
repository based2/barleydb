package com.smartstream.mi.model;

/*
 * #%L
 * Simple Object Relational Framework
 * %%
 * Copyright (C) 2014 Scott Sinclair <scottysinclair@gmail.com>
 * %%
 * All rights reserved.
 * #L%
 */

import java.util.List;

public interface Template {
    Long getId();

    String getName();

    void setName(String name);

    List<TemplateContent> getContents();

    List<BusinessType> getBusinessTypes();
}
