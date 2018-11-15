package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.evaluation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.util.UtilityClassNotInstantiableException;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.jvlc2015.JvlcEvaluationExecutor;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run.TaskExecutor;

public class TaskExecutorUtils {

	private TaskExecutorUtils() {
		throw new UtilityClassNotInstantiableException();
	}

	/**
	 * This method waits until "quit" is entered on the command-line or until
	 * all subprocesses of the given {@link TaskExecutor} have terminated.
	 */
	public static void waitForQuitOrTerminationOfSubprocesses(TaskExecutor taskExecutor) {

		// For some reason this code crashes the TaskExecutor sometimes.
		final NonblockingBufferedReader reader = new NonblockingBufferedReader(
				new BufferedReader(new InputStreamReader(System.in)), 500L);
		try {
			while (true) {

				try {
					final String line = reader.readLine();

					if ("quit".equals(line))
						break;

					synchronized (taskExecutor) {
						if (taskExecutor.areAllTasksFinished()) {
							break;
						}
					}

					Thread.sleep(1000);

				} catch (InterruptedException | IOException e) {
					Monitor.log(JvlcEvaluationExecutor.class, Level.ERROR,
							"Problem during stdin reading: " + e.getMessage());

				}
			}

			synchronized (taskExecutor) {
				if (taskExecutor.getStartedProcesses().isEmpty()) {
					Monitor.log(TaskExecutorUtils.class, Level.INFO,
							"Terminating executor since all sub-processes have terminated.");
				}
			}
		} finally {
			reader.close();
		}

	}

	public static void showStandardPromptForTermination(TaskExecutor taskExecutor) {
		Monitor.log(JvlcEvaluationExecutor.class, Level.INFO, "Enter 'quit' to abort simulation.");
		waitForQuitOrTerminationOfSubprocesses(taskExecutor);
		Monitor.log(JvlcEvaluationExecutor.class, Level.INFO, "Simulation aborted - closing all threads");
		taskExecutor.stop();
	}

	/**
	 * A reader that allows to read from a {@link BufferedReader}
	 * asynchronously.
	 *
	 * This reader must be closed after usage via {@link #close()}.
	 *
	 * @author Roland Kluge - Initial Implementation
	 *
	 */
	private static class NonblockingBufferedReader {
		private final BlockingQueue<String> lines = new LinkedBlockingQueue<String>();
		private final long waitingTimeMillis;
		private Thread backgroundReaderThread = null;
		private volatile boolean closed = false;

		/**
		 * Configures the source reader and the waiting time
		 *
		 * @param bufferedReader
		 *            the reader to read from
		 * @param waitingTimeMillis
		 *            waiting time in milliseconds
		 */
		public NonblockingBufferedReader(final BufferedReader bufferedReader, final long waitingTimeMillis) {
			backgroundReaderThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						while (!Thread.interrupted()) {
							String line = bufferedReader.readLine();
							if (line == null) {
								break;
							}
							lines.add(line);
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					} finally {
						closed = true;
					}
				}
			});
			backgroundReaderThread.setDaemon(true);
			backgroundReaderThread.start();
			this.waitingTimeMillis = waitingTimeMillis;
		}

		/**
		 * Returns a line from the configured {@link BufferedReader} or null if
		 * there is no line available within the configured waiting time
		 *
		 * @return
		 * @throws IOException
		 */
		public String readLine() throws IOException {
			try {
				return closed && lines.isEmpty() ? null : lines.poll(waitingTimeMillis, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				throw new IOException("The BackgroundReaderThread was interrupted!", e);
			}
		}

		/**
		 * Closes this reader
		 */
		public void close() {
			if (backgroundReaderThread != null) {
				backgroundReaderThread.interrupt();
				backgroundReaderThread = null;
			}
		}
	}
}
