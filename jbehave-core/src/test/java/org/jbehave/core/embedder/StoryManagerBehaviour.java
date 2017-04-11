package org.jbehave.core.embedder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyListOf;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

import org.codehaus.plexus.util.FileUtils;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.StoryManager.RunningStory;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.ExamplesCutException;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.junit.Test;
import org.mockito.Mockito;

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
	public void shouldHandleExceptionAtStoryParsing() {
		Configuration mockedConfiguration = mock(Configuration.class);
		EmbedderMonitor mockedEmbedderMonitor = mock(EmbedderMonitor.class);
		PerformableTree mockedPerformableTree = mock(PerformableTree.class);
		StoryManager spy = Mockito.spy(new StoryManager(mockedConfiguration, stepsFactory, embedderControls, mockedEmbedderMonitor, executorService, mockedPerformableTree));
		Exception expected = new ExamplesCutException();
		when(mockedPerformableTree.storyOfPath(mockedConfiguration, STORY_PATH)).thenThrow(expected);
		doNothing().when(spy).runStories(anyListOf(Story.class), any(MetaFilter.class), any(BatchFailures.class));
		MetaFilter metaFilter = new MetaFilter();
		BatchFailures batchFailures = new BatchFailures();
		spy.runStoriesAsPaths(Collections.singletonList(STORY_PATH), metaFilter, batchFailures);
		verify(mockedEmbedderMonitor).storyFailed(STORY_PATH, expected);
		verify(spy).runStories(Collections.<Story>emptyList(), metaFilter, batchFailures);
	}
}
