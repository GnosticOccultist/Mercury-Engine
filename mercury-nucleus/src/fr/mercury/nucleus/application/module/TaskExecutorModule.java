package fr.mercury.nucleus.application.module;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.application.Application;

/**
 * <code>TaskExecutorModule</code> is an implementation of {@link AbstractApplicationModule} which allows
 * for asynchronous execution of submitted tasks using an {@link ExecutorService} containing multiple 
 * separate {@link Thread}.
 * <p>
 * The module will automatically shutdown its execution service when {@link #cleanup()} is called and 
 * waits for any running task to finish. A new service is being created during the initialization of the module, 
 * but the tasks can already be submitted right after instantiation.
 * <p>
 * Note however that the {@link #enable()} or {@link #disable()} methods has no effect on the execution of tasks.
 * 
 * @author GnosticOccultist
 */
public class TaskExecutorModule extends AbstractApplicationModule {
	
	/**
	 * The logger for the tasking module.
	 */
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
	 * The service to executed scheduled tasks. 
	 */
	private ScheduledExecutorService scheduledService;

	/**
	 * Instantiates a new <code>TaskExecutorModule</code> with the default {@link #NB_THREADS}
	 * number of {@link Thread} to generate in the pool.
	 */
	public TaskExecutorModule() {
		this(NB_THREADS);
	}
	
	/**
	 * Instantiates a new <code>TaskExecutorModule</code> with the provided number of {@link Thread}
	 * to generate in the pool.
	 * 
	 * @param nbThreads The number of threads to generate in the pool.
	 */
	public TaskExecutorModule(int nbThreads) {
		restartExecutor(nbThreads);
		this.scheduledService = Executors.newSingleThreadScheduledExecutor();
	}
	
	@Override
	public void initialize(Application application) {
		if(executor == null) {
			restartExecutor(NB_THREADS);
		}
		if(scheduledService == null) {
			this.scheduledService = Executors.newSingleThreadScheduledExecutor();
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
			// Shutdown the executor and scheduled service first.
			executor.shutdown();
			scheduledService.shutdown();
			
			return null;
		});
		
		executor = null;
		scheduledService = null;
		
		logger.info("TaskExecutorModule successfully shutdown.");
		
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
     * Schedule the provided {@link Runnable} to be executed with the <code>TaskExecutorModule</code> 
     * at a fixed rate with the provided delay in milliseconds.
     * 
     * @param runnable The task to be scheduled (not null).
     * @param delay    The delay between each execution in milliseconds (&ge;0).
     */
	public void scheduleAtFixedRate(Runnable task, long delay) {
		Validator.nonNull(task, "The task to be scheduled can't be null!");
		Validator.nonNegative(delay, "The delay can't be negative!");
		
		scheduledService.scheduleAtFixedRate(task, delay, delay, TimeUnit.MILLISECONDS);
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
