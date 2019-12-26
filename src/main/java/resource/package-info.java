/**
 * Contains all the components required to manage worker processes (resource). Each worker is a separate process spawned from the
 * master process. The components of this package must deal with all the infrastructure management aspects of the
 * application which involve spawning, killing and maintaining workers, generating heartbeats for them
 * {@link heartbeat} and managing changes on account of failure to nodes.
 * {@link resource.IResourceManager} is the primary interface for this package.
 *
 * <p>This component does not manage tasks allocated to workers, nor does it allocates tasks. For task allocation,
 * check {@link taskallocation}.
 *
 * @author somilgupta
 */
package resource;