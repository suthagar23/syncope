/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.common.lib.to;

import java.util.ArrayList;
import java.util.List;
import org.apache.syncope.common.lib.AbstractBaseBean;
import org.apache.syncope.common.lib.types.AuditElements;

public class EventCategoryTO extends AbstractBaseBean {

    private static final long serialVersionUID = -4340060002701633401L;

    private AuditElements.EventCategoryType type;

    private String category;

    private String subcategory;

    private final List<String> events = new ArrayList<>();

    /**
     * Constructor for Type.REST event category.
     */
    public EventCategoryTO() {
        this(AuditElements.EventCategoryType.LOGIC);
    }

    /**
     * Constructor for the given Type event category.
     *
     * @param type event category type
     */
    public EventCategoryTO(final AuditElements.EventCategoryType type) {
        super();
        this.type = type;
    }

    public AuditElements.EventCategoryType getType() {
        return type;
    }

    public void setType(final AuditElements.EventCategoryType type) {
        this.type = type == null ? AuditElements.EventCategoryType.CUSTOM : type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(final String subcategory) {
        this.subcategory = subcategory;
    }

    public List<String> getEvents() {
        return events;
    }
}
