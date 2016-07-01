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
package org.apache.syncope.common.lib.policy;

import java.util.ArrayList;
import java.util.List;

public class AccountPolicyTO extends AbstractPolicyTO implements ComposablePolicy<AbstractAccountRuleConf> {

    private static final long serialVersionUID = -1557150042828800134L;

    private boolean propagateSuspension;

    private int maxAuthenticationAttempts;

    private final List<AbstractAccountRuleConf> ruleConfs = new ArrayList<>();

    private final List<String> passthroughResources = new ArrayList<>();

    public boolean isPropagateSuspension() {
        return propagateSuspension;
    }

    public void setPropagateSuspension(final boolean propagateSuspension) {
        this.propagateSuspension = propagateSuspension;
    }

    public int getMaxAuthenticationAttempts() {
        return maxAuthenticationAttempts;
    }

    public void setMaxAuthenticationAttempts(final int maxAuthenticationAttempts) {
        this.maxAuthenticationAttempts = maxAuthenticationAttempts;
    }

    @Override
    public List<AbstractAccountRuleConf> getRuleConfs() {
        return ruleConfs;
    }

    public List<String> getPassthroughResources() {
        return passthroughResources;
    }
}
