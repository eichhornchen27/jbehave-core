package org.jbehave.core.embedder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jbehave.core.embedder.PerformableTree.RunContext;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.codehaus.plexus.util.FileUtils;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.StoryManager.RunningStory;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.model.FailedStory;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.junit.Test;

public class StoryManagerBehaviour {

	private static final String STORY_PATH = "storyPath";

	private PerformableTree performableTree = new PerformableTree();
	private EmbedderMonitor embedderMonitor = new NullEmbedderMonitor(); 
	private EmbedderControls embedderControls = new EmbedderControls();
	private ExecutorService executorService = mock(ExecutorService.class);
	private InjectableStepsFactory stepsFactory = mock(InjectableStepsFactory.class);

	@Test
	public void shouldEnsureStoryReportOutputDirectoryExistsWhenWritingStoryDurations() throws IOException{
		Configuration configuration = new MostUsefulConfiguration();
		configuration.storyReporterBuilder().withRelativeDirectory("inexistent");
		File outputDirectory = configuration.storyReporterBuilder().outputDirectory();
		FileUtils.deleteDirectory(outputDirectory); 
		assertThat(outputDirectory.exists(), is(false));
		StoryManager manager = new StoryManager(configuration, stepsFactory, embedderControls, embedderMonitor, executorService, performableTree);
		Collection<RunningStory> runningStories = new ArrayList<>();
		manager.writeStoryDurations(runningStories);
		assertThat(outputDirectory.exists(), is(true));
	}

	@Test
	public void shouldReportFailedStory() {
		Configuration mockedConfiguration = mock(Configuration.class);
		PerformableTree mockedPerformableTree = mock(PerformableTree.class);
		StoryManager manager = new StoryManager(mockedConfiguration, stepsFactory, embedderControls, embedderMonitor,
				executorService, mockedPerformableTree);
		FailedStory mockedStory = mock(FailedStory.class);
		String stage = "stage";
		when(mockedStory.getStage()).thenReturn(stage);
		String subStage = "subStage";
		when(mockedStory.getSubStage()).thenReturn(subStage);
		Throwable cause = new Exception();
		when(mockedStory.getCause()).thenReturn(cause);
		when(mockedStory.getPath()).thenReturn(STORY_PATH);
		List<CandidateSteps> candidateSteps = new ArrayList<>();
		when(stepsFactory.createCandidateSteps()).thenReturn(candidateSteps);
		MetaFilter metaFilter = new MetaFilter();
		BatchFailures batchFailures = new BatchFailures();
		RunContext mockedContext = mock(RunContext.class);
		when(mockedContext.getFailures()).thenReturn(batchFailures);
		when(mockedPerformableTree.newRunContext(mockedConfiguration, candidateSteps, embedderMonitor, metaFilter,
				batchFailures)).thenReturn(mockedContext);
		FilteredStory mockedFilteredStory = mock(FilteredStory.class);
		when(mockedContext.filter(mockedStory)).thenReturn(mockedFilteredStory);
		when(mockedFilteredStory.allowed()).thenReturn(false);
		StoryReporter mockedReporter = mock(StoryReporter.class);
		when(mockedContext.reporter()).thenReturn(mockedReporter);
		manager.runStories(Collections.<Story>singletonList(mockedStory), metaFilter, batchFailures);
		verify(mockedReporter).beforeStory(mockedStory, false);
		verify(mockedReporter).beforeScenario(stage);
		verify(mockedReporter).beforeStep(subStage);
		verify(mockedReporter).failed(subStage, cause);
		verify(mockedReporter).afterScenario();
		verify(mockedReporter).afterStory(false);
		verify(mockedContext).addFailure(STORY_PATH, cause);
	}
}
