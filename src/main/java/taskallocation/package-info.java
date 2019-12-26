/**
 * Contains all the components required for tasks management amongst workers. This package encapsulates the core
 * task allocation logic of the distributed system without dealing with the actual implementation of the task. Each
 * task is represented as {@link taskallocation.Task}.
 * <p>{@link taskallocation.ITaskManager} is the primary interface for this package.
 * <p>{@link taskallocation.TaskAllocationServer} and {@link taskallocation.TaskAllocationClient} hold the task requests
 * handling logic (communication between master and worker for tasks. For heartbeat communication, please check
 * {@link heartbeat}).
 * <p> {@link taskallocation.ITaskExecutor} is the interface that all applications using task management components of
 * this package must implement.
 *
 * @author somilgupta
 */
package taskallocation;