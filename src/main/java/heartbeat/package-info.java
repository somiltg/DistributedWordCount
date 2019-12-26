/**
 * Contains all the components required to manage heartbeat. A heartbeat is a continuous ping-pong between the master and the worker.
 * Every worker is a heartbeat server that must respond to the request within a duration. Components in this package
 * must maintain heartbeat between the processes (liveliness) and notify interested parties in case of changes in
 * heartbeat.
 * {@link heartbeat.IHeartBeatManager} is the primary interface for this package.
 *
 * @author somilgupta
 */
package heartbeat;