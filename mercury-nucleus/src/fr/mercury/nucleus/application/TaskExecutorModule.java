package fr.mercury.nucleus.application;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.application.module.AbstractApplicationModule;

public class TaskExecutorModule extends AbstractApplicationModule {
	
	private static final Logger logger = FactoryLogger.getLogger("mercury.tasks");
	
	/**
	 * The default number of threads in the pool (default &rarr; 4).
	 */
	private static final int NB_THREADS = 4;
	
	/**
	 * The executor for tasks.
	 */
	private ExecutorService executor;

	/**
	 * Instantiates a new <code>TaskExecutorModule</code> with the default {@link #NB_THREADS}
	 * number of {@link Thread} to generate in the pool.
	 */
	public TaskExecutorModule() {
		restartExecutor(NB_THREADS);
	}
	
	/**
	 * Instantiates a new <code>TaskExecutorModule</code> with the provided number of {@link Thread}
	 * to generate in the pool.
	 * 
	 * @param nbThreads The number of threads to generate in the pool.
	 */
	public TaskExecutorModule(int nbThreads) {
		restartExecutor(nbThreads);
	}
	
	@Override
	public void initialize(Application application) {
		if(executor == null) {
			restartExecutor(10);
		}
		
		super.initialize(application);
	}

	@Override
	public void update(float tpf) {
		// No need for updating this module.
	}
	
	@Override
	public void cleanup() {
		// Avoid any security exception with a privileged access.
		AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
			// Shutdown the executor service first.
			executor.shutdown();
			
			return null;
		});
		
		executor = null;
		logger.info("TaskModule successfully shutdown.");
		
		super.cleanup();
	}
	
	/**
	 * Submits the provided {@link Callable} to be executed with the <code>TaskExecutorModule</code>.
	 * Invoking the function will return a {@link Future} as the pending result of the task.
	 * <p>
	 * If you would like to immediately block waiting for a task, you can call {@link Future#get()}.
	 * 
	 * @param <T> The type of the task's result.
	 * 
	 * @param task The task to be executed (not null).
	 * @return	   The pending result of the task.
	 */
	public <T> Future<T> submit(Callable<T> task) {
		Validator.nonNull(task, "The task to be executed can't be null!");
		return executor.submit(task);
	}
	
	/**
	 * Restart the {@link ExecutorService} by recreating a new instance with the provided
	 * number of {@link Thread} in the pool.
	 * <p>
	 * This when is called during the initialization and instantiation of the <code>TaskExecutorModule</code>.
	 * 
	 * @param nbThreads The number of threads to generate in the pool.
	 */
	private void restartExecutor(int nbThreads) {
		this.executor = Executors.newFixedThreadPool(nbThreads, new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r, "Executor");
				thread.setDaemon(true);
				return thread;
			}
		});
	}
}
