/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.ml.rest;

import static org.junit.Assert.assertEquals;

public class TestSecrets {

    public void testSecrets() {
        assertEquals(System.getenv("TEST_SECRET"), "Testing1234");
    }

}
