package org.jbehave.core.embedder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.StepCollector;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Aliaksandr_Tsymbal.
 */
public class PerformableTreeConversionBehaviour {

    @Test
    public void shouldConvertParameters(){
        SharpParameterConverters sharpParameterConverters = new SharpParameterConverters();
        PerformableTree performableTree = new PerformableTree();
        PerformableTree.RunContext context = mock(PerformableTree.RunContext.class);
        Configuration configuration = mock(Configuration.class);
        Story story = mock(Story.class);
        when(context.configuration()).thenReturn(configuration);
        Narrative narrative = mock(Narrative.class);
        when(story.getNarrative()).thenReturn(narrative);
        FilteredStory filteredStory = mock(FilteredStory.class);
        when(context.filter(story)).thenReturn(filteredStory);
        when(filteredStory.allowed()).thenReturn(true);

        Lifecycle lifecycle = mock(Lifecycle.class);
        when(story.getLifecycle()).thenReturn(lifecycle);
        ExamplesTable storyExamplesTable = mock(ExamplesTable.class);
        when(lifecycle.getExamplesTable()).thenReturn(storyExamplesTable);

        HashMap<String,String> storyExampleFirstRow = new HashMap<String, String>();
        storyExampleFirstRow.put("var1","#A");
        storyExampleFirstRow.put("var2","#B");
        HashMap<String,String> storyExampleSecondRow = new HashMap<String, String>();
        storyExampleSecondRow.put("var1","#C");
        storyExampleSecondRow.put("var2","#D");

        when(storyExamplesTable.getRows())
                .thenReturn(Arrays.<Map<String, String>> asList(storyExampleFirstRow, storyExampleSecondRow));
        StoryControls storyControls = mock(StoryControls.class);
        when(configuration.storyControls()).thenReturn(storyControls);
        when(storyControls.skipBeforeAndAfterScenarioStepsIfGivenStory()).thenReturn(false);
        when(configuration.parameterConverters()).thenReturn(sharpParameterConverters);

        Scenario scenario = mock(Scenario.class);
        when(story.getScenarios()).thenReturn(Collections.singletonList(scenario));
        when(context.failureOccurred()).thenReturn(false);
        when(filteredStory.allowed(scenario)).thenReturn(true);
        Meta meta = mock(Meta.class);
        when(scenario.getMeta()).thenReturn(meta);
        when(story.getMeta()).thenReturn(meta);
        when(meta.inheritFrom(meta)).thenReturn(meta);

        HashMap<String,String> scenarioExample = new HashMap<String, String>();
        scenarioExample.put("var1","#E");
        scenarioExample.put("var3","#F");
        HashMap<String,String> scenarioExampleSecond = new HashMap<String, String>();
        scenarioExampleSecond.put("var1","#G");
        scenarioExampleSecond.put("var3","#H");

        ExamplesTable scenarioExamplesTable = mock(ExamplesTable.class);
        when(scenario.getExamplesTable()).thenReturn(scenarioExamplesTable);
        when(scenarioExamplesTable.isEmpty()).thenReturn(false);
        when(scenarioExamplesTable.getRows()).
                thenReturn(Arrays.<Map<String, String>> asList(scenarioExample, scenarioExampleSecond));
        GivenStories givenStories = mock(GivenStories.class);
        when(scenario.getGivenStories()).thenReturn(givenStories);
        when(givenStories.requireParameters()).thenReturn(false);
        Keywords keywords = mock(Keywords.class);
        when(configuration.keywords()).thenReturn(keywords);
        MetaFilter metaFilter = mock(MetaFilter.class);
        when(context.filter()).thenReturn(metaFilter);
        when(metaFilter.allow(Mockito.<Meta>anyObject())).thenReturn(true);

        when(context.beforeOrAfterScenarioSteps(meta, StepCollector.Stage.BEFORE, ScenarioType.EXAMPLE))
                .thenReturn(new PerformableTree.PerformableSteps());
        when(context.beforeOrAfterScenarioSteps(meta, StepCollector.Stage.AFTER, ScenarioType.EXAMPLE))
                .thenReturn(new PerformableTree.PerformableSteps());
        when(context.beforeOrAfterScenarioSteps(meta, StepCollector.Stage.BEFORE, ScenarioType.ANY))
                .thenReturn(new PerformableTree.PerformableSteps());
        when(context.beforeOrAfterScenarioSteps(meta, StepCollector.Stage.AFTER, ScenarioType.ANY))
                .thenReturn(new PerformableTree.PerformableSteps());
        when(context.lifecycleSteps(lifecycle, meta, StepCollector.Stage.BEFORE))
                .thenReturn(new PerformableTree.PerformableSteps());
        when(context.lifecycleSteps(lifecycle, meta, StepCollector.Stage.AFTER))
                .thenReturn(new PerformableTree.PerformableSteps());

        when(context.scenarioSteps(Mockito.eq(scenario), Mockito.anyMap()))
                .thenReturn(new PerformableTree.PerformableSteps());
        when(scenario.getGivenStories()).thenReturn(givenStories);
        when(givenStories.getPaths()).thenReturn(Collections.EMPTY_LIST);
        when(story.getGivenStories()).thenReturn(givenStories);
        performableTree.addStories(context, Collections.singletonList(story));
        List<PerformableTree.PerformableScenario> performableScenarios = performableTree.getRoot().getStories().get(0)
                .getScenarios();

        assertEquals(scenarioExample.size(), performableScenarios.size());
        List<PerformableTree.ExamplePerformableScenario> examplePerformableScenarios = performableScenarios.get(0)
                .getExamples();
        assertEquals(scenarioExample.size(), examplePerformableScenarios.size());
        assertEquals("eE", examplePerformableScenarios.get(0).getParameters().get("var1"));
        assertEquals("bB", examplePerformableScenarios.get(0).getParameters().get("var2"));
        assertEquals("fF", examplePerformableScenarios.get(0).getParameters().get("var3"));

        assertEquals("gG", examplePerformableScenarios.get(1).getParameters().get("var1"));
        assertEquals("bB", examplePerformableScenarios.get(1).getParameters().get("var2"));
        assertEquals("hH", examplePerformableScenarios.get(1).getParameters().get("var3"));

        examplePerformableScenarios = performableScenarios.get(1).getExamples();
        assertEquals(scenarioExample.size(), examplePerformableScenarios.size());
        assertEquals("eE", examplePerformableScenarios.get(0).getParameters().get("var1"));
        assertEquals("dD", examplePerformableScenarios.get(0).getParameters().get("var2"));
        assertEquals("fF", examplePerformableScenarios.get(0).getParameters().get("var3"));

        assertEquals("gG", examplePerformableScenarios.get(1).getParameters().get("var1"));
        assertEquals("dD", examplePerformableScenarios.get(1).getParameters().get("var2"));
        assertEquals("hH", examplePerformableScenarios.get(1).getParameters().get("var3"));
    }

    private class SharpParameterConverters extends ParameterConverters{

        public Object convert(String value, Type type, Story story) {
            if(type == String.class){

                return value.replace("#", value.substring(1).toLowerCase());
            }
            return null;
        }
    }
}
