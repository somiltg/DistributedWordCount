/**
 * This project builds a distributed Word Count application. The application defines a single node, multi-process
 * master-worker architecture to find the joint ordered word count frequency of a collection of files.
 * <p> {@link heartbeat} defines mechanism for ensuring node health check using system of ping pongs called
 * heartbeat.
 * <p> {@link resource} creates and maintains the infrastructure by maintaining multiple process respresenting the
 * various nodes in the single node, multi-process system. Does not manage task allocation.
 * <p> {@link taskallocation} handles the core task management and orchestration without holding the business
 * specific execution logic e.g. wordcount here. The components also handles task associated communication between
 * components and updates the task as per infrastructure or task status changes.
 * <p> {@link util} holds common business-agnostic utility functions for the application.
 * <p> All the above components are generic and can be reused across different distributed applications that work in
 * a master slave architecture.
 * <p> {@link wordcount} containing the word count execution logic and worker application that needs to be run over
 * the architecture.
 * <p> Master defines the generic interface for Master while WordCount is its word-count application
 * specific implementation.
 *
 * @author somilgupta, sahilsharma
 */
package java;
