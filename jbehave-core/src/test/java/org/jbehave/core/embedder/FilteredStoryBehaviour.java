package org.jbehave.core.embedder;

import static org.junit.Assert.assertFalse;

import org.jbehave.core.model.FailedStory;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Story;
import org.junit.Test;

public class FilteredStoryBehaviour {

    @Test
    public void shouldNotAllowFailedStory() {
        Story failedStory = new FailedStory("", Meta.EMPTY, "", "", new Exception());
        FilteredStory filteredStory = new FilteredStory(new MetaFilter(), failedStory, new StoryControls());
        assertFalse(filteredStory.allowed());
    }
}
