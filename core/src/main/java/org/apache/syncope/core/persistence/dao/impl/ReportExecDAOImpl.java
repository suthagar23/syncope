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
package org.apache.syncope.core.persistence.dao.impl;

import java.util.Date;
import java.util.List;
import javax.persistence.TypedQuery;
import org.apache.syncope.core.persistence.beans.Report;
import org.apache.syncope.core.persistence.beans.ReportExec;
import org.apache.syncope.core.persistence.dao.ReportExecDAO;
import org.apache.syncope.core.persistence.validation.entity.InvalidEntityException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ReportExecDAOImpl extends AbstractDAOImpl implements ReportExecDAO {

    @Override
    public ReportExec find(final Long id) {
        return entityManager.find(ReportExec.class, id);
    }

    private ReportExec findLatest(final Report report, final String field) {
        TypedQuery<ReportExec> query = entityManager.createQuery(
                "SELECT e FROM " + ReportExec.class.getSimpleName() + " e "
                + "WHERE e.report=:report ORDER BY e." + field + " DESC", ReportExec.class);
        query.setParameter("report", report);
        query.setMaxResults(1);

        List<ReportExec> result = query.getResultList();
        return result == null || result.isEmpty()
                ? null
                : result.iterator().next();
    }

    @Override
    public ReportExec findLatestStarted(final Report report) {
        return findLatest(report, "startDate");
    }

    @Override
    public ReportExec findLatestEnded(final Report report) {
        return findLatest(report, "endDate");
    }

    @Override
    public List<ReportExec> findAll(
            final Report report,
            final Date startedBefore, final Date startedAfter, final Date endedBefore, final Date endedAfter) {

        StringBuilder queryString = new StringBuilder("SELECT e FROM ").append(ReportExec.class.getSimpleName()).
                append(" e WHERE e.report=:report ");

        if (startedBefore != null) {
            queryString.append(" AND e.startDate < :startedBefore");
        }
        if (startedAfter != null) {
            queryString.append(" AND e.startDate > :startedAfter");
        }
        if (endedBefore != null) {
            queryString.append(" AND e.endDate < :endedBefore");
        }
        if (endedAfter != null) {
            queryString.append(" AND e.endDate > :endedAfter");
        }

        TypedQuery<ReportExec> query = entityManager.createQuery(queryString.toString(), ReportExec.class);
        query.setParameter("report", report);
        if (startedBefore != null) {
            query.setParameter("startedBefore", startedBefore);
        }
        if (startedAfter != null) {
            query.setParameter("startedAfter", startedAfter);
        }
        if (endedBefore != null) {
            query.setParameter("endedBefore", endedBefore);
        }
        if (endedAfter != null) {
            query.setParameter("endedAfter", endedAfter);
        }

        return query.getResultList();
    }

    /**
     * This method is annotated as transactional because called from ReportJob.
     *
     * @see org.apache.syncope.core.report.ReportJob
     * @param execution to be merged
     * @return merged execution
     * @throws InvalidEntityException if any validation error occurs
     */
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ReportExec save(final ReportExec execution) throws InvalidEntityException {
        return entityManager.merge(execution);
    }

    @Override
    public void delete(final Long id) {
        ReportExec execution = find(id);
        if (execution == null) {
            return;
        }

        delete(execution);
    }

    @Override
    public void delete(final ReportExec execution) {
        if (execution.getReport() != null) {
            execution.getReport().removeExec(execution);
        }

        entityManager.remove(execution);
    }
}
