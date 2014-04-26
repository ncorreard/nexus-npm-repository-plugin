package com.bolyuba.nexus.plugin.npm.hosted;

import com.bolyuba.nexus.plugin.npm.NpmContentClass;
import com.bolyuba.nexus.plugin.npm.NpmPlugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.inject.Description;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.RepositoryKind;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Georgy Bolyuba (georgy@bolyuba.com)
 */
@Named(NpmPlugin.ROLE_HINT)
@Description("Npm registry hosted repo")
public class DefaultNpmHostedRepository
        extends AbstractProxyRepository
                implements NpmHostedRepository {

    private final ContentClass contentClass;

    private final NpmHostedRepositoryConfigurator configurator;

    private final RepositoryKind repositoryKind;

    @Inject
    public DefaultNpmHostedRepository(final @Named(NpmContentClass.ID) ContentClass contentClass,
                                      NpmHostedRepositoryConfigurator configurator) {
        this.contentClass = contentClass;
        this.configurator = configurator;
        this.repositoryKind = new DefaultRepositoryKind(NpmHostedRepository.class, null);
    }

    @Override
    protected Configurator getConfigurator() {
        return this.configurator;
    }

    @Override
    public RepositoryKind getRepositoryKind() {
        return this.repositoryKind;
    }

    @Override
    public ContentClass getRepositoryContentClass() {
        return this.contentClass;
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory() {
        return new CRepositoryExternalConfigurationHolderFactory<NpmHostedRepositoryConfiguration>() {
            @Override
            public NpmHostedRepositoryConfiguration createExternalConfigurationHolder(final CRepository config) {
                return new NpmHostedRepositoryConfiguration((Xpp3Dom) config.getExternalConfiguration());
            }
        };
    }
}