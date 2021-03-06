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
package org.apache.syncope.client.console.tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.syncope.client.console.SyncopeConsoleSession;
import org.apache.syncope.client.console.commons.Constants;
import org.apache.syncope.client.console.commons.TaskDataProvider;
import org.apache.syncope.client.console.pages.BasePage;
import org.apache.syncope.client.console.panels.ModalPanel;
import org.apache.syncope.client.console.panels.MultilevelPanel;
import org.apache.syncope.client.console.rest.TaskRestClient;
import org.apache.syncope.client.console.wicket.extensions.markup.html.repeater.data.table.ActionColumn;
import org.apache.syncope.client.console.wicket.extensions.markup.html.repeater.data.table.BooleanPropertyColumn;
import org.apache.syncope.client.console.wicket.extensions.markup.html.repeater.data.table.DatePropertyColumn;
import org.apache.syncope.client.console.wicket.extensions.markup.html.repeater.data.table.KeyPropertyColumn;
import org.apache.syncope.client.console.wicket.markup.html.bootstrap.dialog.BaseModal;
import org.apache.syncope.client.console.wicket.markup.html.form.ActionLink;
import org.apache.syncope.client.console.wicket.markup.html.form.ActionLink.ActionType;
import org.apache.syncope.client.console.wicket.markup.html.form.ActionLinksPanel;
import org.apache.syncope.client.console.wizards.AjaxWizard;
import org.apache.syncope.common.lib.types.StandardEntitlement;
import org.apache.syncope.common.lib.types.TaskType;
import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.to.AnyTO;
import org.apache.syncope.common.lib.to.SchedTaskTO;
import org.apache.syncope.common.lib.to.TemplatableTO;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * Tasks page.
 *
 * @param <T> Sched task type.
 */
public abstract class SchedTaskDirectoryPanel<T extends SchedTaskTO>
        extends TaskDirectoryPanel<T> implements ModalPanel {

    private static final long serialVersionUID = 4984337552918213290L;

    private static final String GROUP_MEMBER_PROVISION_TASKJOB =
            "org.apache.syncope.core.provisioning.java.job.GroupMemberProvisionTaskJobDelegate";

    protected final Class<T> reference;

    protected T schedTaskTO;

    private final TaskStartAtTogglePanel startAt;

    protected final TemplatesTogglePanel templates;

    protected SchedTaskDirectoryPanel(
            final BaseModal<?> baseModal,
            final MultilevelPanel multiLevelPanelRef,
            final Class<T> reference,
            final PageReference pageRef) {
        super(baseModal, multiLevelPanelRef, pageRef);
        this.reference = reference;

        try {
            schedTaskTO = reference.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            LOG.error("Falure instantiating task", e);
        }

        this.addNewItemPanelBuilder(new SchedTaskWizardBuilder<>(schedTaskTO, pageRef), true);

        MetaDataRoleAuthorizationStrategy.authorize(addAjaxLink, RENDER, StandardEntitlement.TASK_CREATE);

        enableExitButton();
        setFooterVisibility(false);

        initResultTable();

        startAt = new TaskStartAtTogglePanel(container, pageRef);
        addInnerObject(startAt);

        templates = new TemplatesTogglePanel(getActualId(), this, pageRef) {

            private static final long serialVersionUID = -8765794727538618705L;

            @Override
            protected Serializable onApplyInternal(
                    final TemplatableTO targetObject, final String type, final AnyTO anyTO) {
                targetObject.getTemplates().put(type, anyTO);
                new TaskRestClient().update(SchedTaskTO.class.cast(targetObject));
                return targetObject;
            }
        };

        addInnerObject(templates);
    }

    protected List<IColumn<T, String>> getFieldColumns() {
        final List<IColumn<T, String>> columns = new ArrayList<>();

        columns.add(new KeyPropertyColumn<T>(
                new StringResourceModel("key", this), "key", "key"));

        columns.add(new PropertyColumn<T, String>(
                new StringResourceModel("name", this), "name", "name"));

        columns.add(new PropertyColumn<T, String>(new StringResourceModel(
                "jobDelegateClassName", this), "jobDelegateClassName", "jobDelegateClassName") {

            private static final long serialVersionUID = -3223917055078733093L;

            @Override
            public void populateItem(
                    final Item<ICellPopulator<T>> item, final String componentId, final IModel<T> rowModel) {
                final IModel<?> model = getDataModel(rowModel);
                if (model != null && model.getObject() instanceof String) {
                    String value = String.class.cast(model.getObject());
                    if (value.length() > 20) {
                        item.add(new Label(componentId, new Model<>("..." + value.substring(value.length() - 17))));
                    } else {
                        item.add(new Label(componentId, getDataModel(rowModel)));
                    }
                } else {
                    super.populateItem(item, componentId, rowModel);
                }
            }

        });

        columns.add(new DatePropertyColumn<T>(
                new StringResourceModel("lastExec", this), "lastExec", "lastExec"));

        columns.add(new DatePropertyColumn<T>(
                new StringResourceModel("nextExec", this), "nextExec", "nextExec"));

        columns.add(new PropertyColumn<T, String>(
                new StringResourceModel("latestExecStatus", this), "latestExecStatus", "latestExecStatus"));

        columns.add(new BooleanPropertyColumn<T>(
                new StringResourceModel("active", this), "active", "active"));

        return columns;
    }

    @Override
    protected final List<IColumn<T, String>> getColumns() {
        final List<IColumn<T, String>> columns = new ArrayList<>();

        columns.addAll(getFieldColumns());

        columns.add(new ActionColumn<T, String>(new ResourceModel("actions")) {

            private static final long serialVersionUID = 2054811145491901166L;

            @Override
            public ActionLinksPanel<T> getActions(final String componentId, final IModel<T> model) {
                final T taskTO = model.getObject();

                ActionLinksPanel.Builder<T> panel = ActionLinksPanel.<T>builder().add(new ActionLink<T>() {

                    private static final long serialVersionUID = -3722207913631435501L;

                    @Override
                    public void onClick(final AjaxRequestTarget target, final T ignore) {
                        viewTask(taskTO, target);
                    }
                }, ActionLink.ActionType.VIEW, StandardEntitlement.TASK_READ).add(new ActionLink<T>() {

                    private static final long serialVersionUID = -3722207913631435501L;

                    @Override
                    public void onClick(final AjaxRequestTarget target, final T ignore) {
                        send(SchedTaskDirectoryPanel.this, Broadcast.EXACT,
                                new AjaxWizard.EditItemActionEvent<>(
                                        restClient.readSchedTask(reference, model.getObject().getKey()),
                                        target).setResourceModel(
                                        new StringResourceModel("inner.task.edit",
                                                SchedTaskDirectoryPanel.this,
                                                Model.of(Pair.of(
                                                        ActionLink.ActionType.EDIT, model.getObject())))));
                    }
                }, ActionLink.ActionType.EDIT, StandardEntitlement.TASK_UPDATE,
                        !GROUP_MEMBER_PROVISION_TASKJOB.equals(taskTO.getJobDelegateClassName())).
                        add(new ActionLink<T>() {

                            private static final long serialVersionUID = -3722207913631435501L;

                            @Override
                            public void onClick(final AjaxRequestTarget target, final T ignore) {
                                final T clone = SerializationUtils.clone(model.getObject());
                                clone.setKey(null);
                                send(SchedTaskDirectoryPanel.this, Broadcast.EXACT,
                                        new AjaxWizard.EditItemActionEvent<>(clone, target).setResourceModel(
                                                new StringResourceModel("inner.task.clone",
                                                        SchedTaskDirectoryPanel.this,
                                                        Model.of(Pair.of(
                                                                ActionLink.ActionType.CLONE, model.getObject())))));
                            }
                        }, ActionLink.ActionType.CLONE, StandardEntitlement.TASK_CREATE).add(new ActionLink<T>() {

                    private static final long serialVersionUID = -3722207913631435501L;

                    @Override
                    public void onClick(final AjaxRequestTarget target, final T ignore) {
                        startAt.setExecutionDetail(
                                model.getObject().getKey(), model.getObject().getName(), target);
                        startAt.toggle(target, true);
                    }
                }, ActionLink.ActionType.EXECUTE, StandardEntitlement.TASK_EXECUTE,
                        !GROUP_MEMBER_PROVISION_TASKJOB.equals(taskTO.getJobDelegateClassName())).
                        add(new ActionLink<T>() {

                            private static final long serialVersionUID = -3722207913631435501L;

                            @Override
                            public void onClick(final AjaxRequestTarget target, final T ignore) {
                                try {
                                    restClient.delete(taskTO.getKey(), reference);
                                    SyncopeConsoleSession.get().info(getString(Constants.OPERATION_SUCCEEDED));
                                    target.add(container);
                                } catch (SyncopeClientException e) {
                                    SyncopeConsoleSession.get().error(StringUtils.isBlank(e.getMessage())
                                            ? e.getClass().getName() : e.getMessage());
                                    LOG.error("While deleting propagation task {}", taskTO.getKey(), e);
                                }
                                ((BasePage) pageRef.getPage()).getNotificationPanel().refresh(target);
                            }
                        }, ActionLink.ActionType.DELETE, StandardEntitlement.TASK_DELETE);

                addFurtherActions(panel, model);

                return panel.build(componentId, model.getObject());
            }

            @Override
            public ActionLinksPanel<T> getHeader(final String componentId) {
                final ActionLinksPanel.Builder<T> panel = ActionLinksPanel.builder();

                return panel.add(new ActionLink<T>() {

                    private static final long serialVersionUID = -7978723352517770644L;

                    @Override
                    public void onClick(final AjaxRequestTarget target, final T ignore) {
                        if (target != null) {
                            target.add(container);
                        }
                    }
                }, ActionLink.ActionType.RELOAD, StandardEntitlement.TASK_LIST).build(componentId);
            }
        });

        return columns;
    }

    protected void addFurtherActions(final ActionLinksPanel.Builder<T> panel, final IModel<T> model) {
    }

    @Override
    protected String paginatorRowsKey() {
        return Constants.PREF_SCHED_TASKS_PAGINATOR_ROWS;
    }

    @Override
    protected Collection<ActionType> getBulkActions() {
        final List<ActionType> bulkActions = new ArrayList<>();
        bulkActions.add(ActionType.DELETE);
        bulkActions.add(ActionType.EXECUTE);
        bulkActions.add(ActionType.DRYRUN);
        return bulkActions;
    }

    @Override
    protected SchedTasksProvider<T> dataProvider() {
        return new SchedTasksProvider<>(reference, TaskType.SCHEDULED, rows);
    }

    protected class SchedTasksProvider<T extends SchedTaskTO> extends TaskDataProvider<T> {

        private static final long serialVersionUID = 4725679400450513556L;

        private final Class<T> reference;

        public SchedTasksProvider(final Class<T> reference, final TaskType id, final int paginatorRows) {
            super(paginatorRows, id, restClient);
            setSort("name", SortOrder.ASCENDING);
            this.reference = reference;
        }

        @Override
        public Iterator<T> iterator(final long first, final long count) {
            int page = ((int) first / paginatorRows);

            List<T> tasks = restClient.list(reference, (page < 0 ? 0 : page) + 1, paginatorRows, getSort());
            Collections.sort(tasks, getComparator());
            return tasks.iterator();
        }
    }
}
