package com.bolyuba.nexus.plugin.npm;

import org.sonatype.nexus.plugin.PluginIdentity;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * This plugin adds Npm support to Sonatype Nexus. Implementation is based on
 * http://wiki.commonjs.org/wiki/Packages/Registry spec and behaviour of https://registry.npmjs.org
 *
 * @author Georgy Bolyuba (georgy@bolyuba.com)
 */
@Named
@Singleton
public class NpmPlugin extends PluginIdentity {

    public static final String ARTIFACT_ID = "nexus-npm-repository-plugin";

    public static final String GROUP_ID = "com.bolyuba.nexus.plugin";

    public NpmPlugin() throws Exception {
        super(GROUP_ID, ARTIFACT_ID);
    }
}
