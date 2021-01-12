package fr.mercury.nucleus.application.service;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Deque;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import fr.alchemy.utilities.Validator;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.mercury.nucleus.application.Application;
import fr.mercury.nucleus.application.AbstractApplicationService;
import fr.mercury.nucleus.application.MercuryContext;
import fr.mercury.nucleus.application.MercurySettings;
import fr.mercury.nucleus.utils.OpenGLCall;
import fr.mercury.nucleus.utils.ReadableTimer;

/**
 * <code>TaskExecutorModule</code> is an implementation of {@link AbstractApplicationService} which allows for 
 * asynchronous execution of submitted tasks using an {@link ExecutorService} containing multiple separate {@link Thread}. 
 * It also allows execution of tasks in the <code>OpenGL</code> {@link Thread}, for any graphics related methods.
 * <p>
 * The module will automatically shutdown its execution service when {@link #cleanup()} is called and waits for 
 * any running task to finish. A new service is being created during the initialization of the module, but the tasks 
 * can already be submitted right after instantiation.
 * <p>
 * Note however that the {@link #enable()} or {@link #disable()} methods has no effect on the execution of tasks.
 * 
 * @see #submit(Callable)
 * @see #submitGraphics(Runnable)
 * 
 * @author GnosticOccultist
 */
public class TaskExecutorService extends AbstractApplicationService {

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
     * The executor for tasks to be executed on the rendering thread.
     */
    private Deque<GraphicsAction> graphicsExecutor;

    /**
     * Instantiates a new <code>TaskExecutorModule</code> with the default
     * {@link #NB_THREADS} number of {@link Thread} to generate in the pool.
     */
    public TaskExecutorService() {
        this(NB_THREADS);
    }

    /**
     * Instantiates a new <code>TaskExecutorModule</code> with the provided number
     * of {@link Thread} to generate in the pool.
     * 
     * @param nbThreads The number of threads to generate in the pool.
     */
    public TaskExecutorService(int nbThreads) {
        restartExecutor(nbThreads);
        this.scheduledService = Executors.newSingleThreadScheduledExecutor();
        this.graphicsExecutor = new ConcurrentLinkedDeque<>();
    }

    @Override
    @OpenGLCall
    public void initialize(MercurySettings settings) {
        if (executor == null) {
            restartExecutor(NB_THREADS);
        }
        if (scheduledService == null) {
            this.scheduledService = Executors.newSingleThreadScheduledExecutor();
        }
        if (graphicsExecutor == null) {
            this.graphicsExecutor = new ConcurrentLinkedDeque<>();
        }

        super.initialize(settings);
    }

    @Override
    @OpenGLCall
    public void update(ReadableTimer timer) {
        // Run the graphics tasks in the rendering thread.
        // TODO: See if the timer interface can be helpful to pass as an argument.
        executeGraphics();
    }

    /**
     * Executes all pending {@link GraphicsAction} and remove/release them when
     * finished.
     * <p>
     * Note that before the <code>TaskExecutorModule</code> shutdown, every graphics
     * action are called even if they do block the main {@link Thread}.
     */
    @OpenGLCall
    private void executeGraphics() {
        while (!graphicsExecutor.isEmpty()) {
            GraphicsAction action = graphicsExecutor.peek();
            if (action.action.getAsBoolean()) {
                graphicsExecutor.poll();
                action.semaphore.release();
            }
        }
    }

    @Override
    @OpenGLCall
    public void cleanup() {
        // Avoid any security exception with a privileged access.
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            // Shutdown the executor and scheduled service first.
            executor.shutdown();
            scheduledService.shutdown();

            return null;
        });

        executeGraphics();

        executor = null;
        scheduledService = null;
        graphicsExecutor = null;

        logger.info("TaskExecutorModule successfully shutdown.");

        super.cleanup();
    }

    /**
     * Submits the provided {@link Callable} to be executed with the
     * <code>TaskExecutorModule</code>. Invoking the function will return a
     * {@link Future} as the pending result of the task.
     * <p>
     * If you would like to immediately block waiting for a task, you can call
     * {@link Future#get()}.
     * 
     * @param      <T> The type of the task's result.
     * 
     * @param task The task to be executed (not null).
     * @return The pending result of the task.
     */
    public <T> Future<T> submit(Callable<T> task) {
        Validator.nonNull(task, "The task to be executed can't be null!");
        return executor.submit(task);
    }

    /**
     * Submits the provided {@link BooleanSupplier} to be executed with the
     * <code>TaskExecutorModule</code>, in the <code>OpenGL</code> {@link Thread}.
     * <p>
     * The task will be executed instantly if in the OpenGL thread or the next time
     * this module gets updated by the {@link Application} managing it and until the
     * returned value return true.
     * 
     * @param action The action to be executed in the graphics thread (not null).
     */
    public void submitGraphics(BooleanSupplier action) {
        Validator.nonNull(action, "The task to be executed can't be null!");
        if (MercuryContext.isMainThread()) {
            action.getAsBoolean();
        } else {
            graphicsExecutor.addLast(new GraphicsAction(action));
        }
    }

    /**
     * Submits the provided {@link BooleanSupplier} to be executed with the
     * <code>TaskExecutorModule</code>, in the <code>OpenGL</code> {@link Thread},
     * blocking the thread until the action has been successfully executed.
     * <p>
     * The task will be executed instantly if in the OpenGL thread or the next time
     * this module gets updated by the {@link Application} managing it and until the
     * returned value return true.
     * 
     * @param action The action to be executed in the graphics thread (not null).
     */
    public void submitGraphicsBlocking(BooleanSupplier action) {
        Validator.nonNull(action, "The task to be executed can't be null!");
        if (MercuryContext.isMainThread()) {
            if (!action.getAsBoolean()) {
                GraphicsAction graphicsAction = new GraphicsAction(action);
                graphicsExecutor.addLast(graphicsAction);
                graphicsAction.semaphore.acquireUninterruptibly();
            }
        } else {
            GraphicsAction graphicsAction = new GraphicsAction(action);
            graphicsExecutor.addLast(graphicsAction);
            graphicsAction.semaphore.acquireUninterruptibly();
        }
    }

    /**
     * Schedule the provided {@link Runnable} to be executed with the
     * <code>TaskExecutorModule</code> at a fixed rate with the provided delay in
     * milliseconds.
     * 
     * @param task  The task to be scheduled (not null).
     * @param delay The delay between each execution in milliseconds (&ge;0).
     */
    public void scheduleAtFixedRate(Runnable task, long delay) {
        Validator.nonNull(task, "The task to be scheduled can't be null!");
        Validator.nonNegative(delay, "The delay can't be negative!");

        scheduledService.scheduleAtFixedRate(task, delay, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Restart the {@link ExecutorService} by recreating a new instance with the
     * provided number of {@link Thread} in the pool.
     * <p>
     * This when is called during the initialization and instantiation of the
     * <code>TaskExecutorModule</code>.
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

    /**
     * <code>GraphicsAction</code> is a wrapper class containing a
     * {@link BooleanSupplier} which can be executed on the main {@link Thread} to
     * deal with graphics and a {@link Semaphore} which allows for blocking the
     * thread until successful execution.
     * 
     * @see TaskExecutorService#submitGraphics(BooleanSupplier)
     * @see TaskExecutorService#submitGraphicsBlocking(BooleanSupplier)
     * 
     * @author GnosticOccultist
     */
    public class GraphicsAction {

        /**
         * The action which will be executed until it has returned false.
         */
        private final BooleanSupplier action;
        /**
         * The semaphore used for blocking and release the thread.
         */
        private final Semaphore semaphore;

        /**
         * Private constructor to inhibit instantiation of <code>GraphicsAction</code>.
         * Use {@link TaskExecutorService#submitGraphics(BooleanSupplier)} or
         * {@link TaskExecutorService#submitGraphicsBlocking(BooleanSupplier)} to create
         * and submit a new instance of this class.
         * 
         * @param action The action to execute (not null).
         */
        private GraphicsAction(BooleanSupplier action) {
            this.action = action;
            this.semaphore = new Semaphore(0);
        }
    }
}
