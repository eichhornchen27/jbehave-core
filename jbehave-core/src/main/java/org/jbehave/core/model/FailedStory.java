package org.jbehave.core.model;

import java.util.Collections;

import org.jbehave.core.failures.UUIDExceptionWrapper;

public class FailedStory extends Story {

    private Throwable cause;

    public FailedStory(String path, Meta meta, String stage, String subStage, Throwable cause) {
        this(path, meta, new Scenario(stage, Collections.singletonList(subStage)), cause);
    }

    private FailedStory(String path, Meta meta, Scenario stage, Throwable cause) {
        super(path, Description.EMPTY, meta, Narrative.EMPTY, Collections.singletonList(stage));
        this.cause = new UUIDExceptionWrapper(cause);
    }

    public Throwable getCause() {
        return cause;
    }

    /**
     * @deprecated use {@link #getScenarioStage()}
     * @return Scenario title
     */
    @Deprecated
    public String getStage() {
        return getScenarioStage().getTitle();
    }

    public String getSubStage() {
        return getScenarioStage().getSteps().get(0);
    }

    public Scenario getScenarioStage() {
        return getScenarios().get(0);
    }
}
