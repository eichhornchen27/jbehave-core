package org.jbehave.core.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Valery_Yatsynovich
 */
public class LifecycleBehaviour {

    @Test
    public void shouldCreateEmptyLifecycleWithDefaultConstructor() {
        Lifecycle lifecycle = new Lifecycle();
        assertTrue(lifecycle.isEmpty());
    }
}
