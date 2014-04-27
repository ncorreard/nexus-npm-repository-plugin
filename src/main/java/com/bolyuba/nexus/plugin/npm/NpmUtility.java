package com.bolyuba.nexus.plugin.npm;

import com.bolyuba.nexus.plugin.npm.proxy.content.NpmFilteringContentLocator;
import org.sonatype.nexus.proxy.RequestContext;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.ProxyRepository;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Utility class that implements most of the commonjs/npm related plumbing.
 *
 * @author Georgy Bolyuba (georgy@bolyuba.com)
 */
@Named
@Singleton
public class NpmUtility {

    private static final String NPM_DECORATED_FLAG = "npm.decorated";

    private static final String JSON_CONTENT_FILE_NAME = "content.json";

    private static final String JSON_MIME_TYPE = "application/json";

    /**
     * Trying to decide if request is coming form npm utility.
     * <p/>
     * Following http://wiki.commonjs.org/wiki/Packages/Registry#HTTP_Request_Method_and_Headers
     * checking Accept for "application/json" would be a good idea. Right now it is not possible as
     * {@link org.sonatype.nexus.web.content.NexusContentServlet#getResourceStoreRequest(javax.servlet.http.HttpServletRequest)}
     * does not map Accept header into anything.
     *
     * @param request request we are about to process
     * @return {@code true} if we think request is coming form npm utility, {@code false} otherwise (for example,
     * if someone is browsing content of the repo in Nexus UI).
     */
    public final boolean isNmpRequest(ResourceStoreRequest request) {
        RequestContext context = request.getRequestContext();
        if (context == null) {
            return false;
        }

        Object o = context.get("request.agent");
        if (o == null) {
            return false;
        }

        // not strictly an npm, but node. Best we can do atm
        return o.toString().toLowerCase().startsWith("node");
    }

    public final String suggestMimeType(@Nonnull String path) {
        // this should take into account if request in from npm or not
        // right now we only know that content.json is json
        if (path.toLowerCase().endsWith(JSON_CONTENT_FILE_NAME)) {
            return JSON_MIME_TYPE;
        }
        return null;
    }

    public final boolean isJson(DefaultStorageFileItem item) {
        return JSON_MIME_TYPE.equals(item.getMimeType());
    }

    public final DefaultStorageFileItem wrapJsonItem(ProxyRepository repository, ResourceStoreRequest request, DefaultStorageFileItem item) {
        NpmFilteringContentLocator decoratedContentLocator = decorateContentLocator(item, request, repository.getRemoteUrl());
        ResourceStoreRequest decoratedRequest = decorateRequest(request);

        DefaultStorageFileItem storageFileItem = new DefaultStorageFileItem(
                repository,
                decoratedRequest,
                item.isReadable(),
                item.isWritable(),
                decoratedContentLocator);

        storageFileItem.getItemContext().put(NPM_DECORATED_FLAG, true);
        return storageFileItem;
    }

    private NpmFilteringContentLocator decorateContentLocator(DefaultStorageFileItem item, ResourceStoreRequest request, @Nonnull String remoteUrl) {
        return new NpmFilteringContentLocator(item.getContentLocator(), request, remoteUrl);
    }

    private ResourceStoreRequest decorateRequest(ResourceStoreRequest request) {
        String path = request.getRequestPath();
        if (!path.endsWith(RepositoryItemUid.PATH_SEPARATOR)) {
            path = path + RepositoryItemUid.PATH_SEPARATOR;
        }
        request.setRequestPath(path + JSON_CONTENT_FILE_NAME);
        return request;
    }

    public final boolean shouldNotGotRemote(ResourceStoreRequest request) {
        return request.getRequestPath().toLowerCase().endsWith(JSON_CONTENT_FILE_NAME);
    }

    public final boolean shouldNotCache(ResourceStoreRequest request) {
        // TODO: This does not work yet, always returns full "all"
        return "/-/all/since".equals(request.getRequestPath());
    }

    static final String NPM_PACKAGE = "npm.package";

    static final String NPM_VERSION = "npm.version";

    /**
     * Adds npm metadata to the request context, mostly to classify URL as per http://wiki.commonjs.org/wiki/Packages/Registry#URLs
     *
     * @param request request we want to decorate
     */
    public void addNpmMeta(@Nonnull ResourceStoreRequest request) {
        String requestPath = request.getRequestPath();
        if (requestPath == null) {
            // wtf?
            return;
        }

        if (RepositoryItemUid.PATH_SEPARATOR.equals(requestPath)) {
            return;
        }

        RequestContext context = request.getRequestContext();

        String correctedPath =
                requestPath.startsWith(RepositoryItemUid.PATH_SEPARATOR) ? requestPath.substring(
                        1, requestPath.length())
                        : requestPath;

        String[] explodedPath = correctedPath.split(RepositoryItemUid.PATH_SEPARATOR);

        if (explodedPath.length >= 1) {
            context.put(NPM_PACKAGE, explodedPath[0]);
        }

        if (explodedPath.length >= 2) {
            context.put(NPM_VERSION, explodedPath[1]);
        }
    }
}