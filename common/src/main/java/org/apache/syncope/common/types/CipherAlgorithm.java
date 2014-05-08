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
package org.apache.syncope.common.types;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum CipherAlgorithm {

    SHA1("SHA-1", false),
    SHA256("SHA-256", false),
    AES("AES", true),
    SMD5("S-MD5", false),
    SSHA1("S-SHA-1", false),
    SSHA256("S-SHA-256", false),
    BCRYPT("BCRYPT", false);

    final private String algorithm;

    final private boolean invertible;

    CipherAlgorithm(final String algorithm, final boolean invertible) {
        this.algorithm = algorithm;
        this.invertible = invertible;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public boolean isInvertible() {
        return invertible;
    }

    public static CipherAlgorithm fromString(final String value) {
        return CipherAlgorithm.valueOf(value.toUpperCase());
    }
}