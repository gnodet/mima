package eu.maveniverse.maven.mima.context;

import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.util.List;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

public class Context implements Closeable {
    private final RuntimeSupport runtime;

    private final RepositorySystem repositorySystem;

    private final RepositorySystemSession repositorySystemSession;

    private final List<RemoteRepository> remoteRepositories;

    public Context(
            RuntimeSupport runtime,
            RepositorySystem repositorySystem,
            RepositorySystemSession repositorySystemSession,
            List<RemoteRepository> remoteRepositories) {
        this.runtime = runtime;
        this.repositorySystemSession = requireNonNull(repositorySystemSession);
        this.repositorySystem = requireNonNull(repositorySystem);
        this.remoteRepositories = requireNonNull(remoteRepositories);
    }

    public boolean isManagedRepositorySystem() {
        return runtime.managedRepositorySystem();
    }

    public RepositorySystemSession repositorySystemSession() {
        return repositorySystemSession;
    }

    public RepositorySystem repositorySystem() {
        return repositorySystem;
    }

    public List<RemoteRepository> remoteRepositories() {
        return remoteRepositories;
    }

    public Context customize(ContextOverrides overrides) {
        return runtime.customizeContext(overrides, this, false);
    }

    @Override
    public void close() {
        // in the future session may become closeable as well
        // repositorySystemSession.close();
        if (isManagedRepositorySystem()) {
            repositorySystem.shutdown();
        }
    }
}