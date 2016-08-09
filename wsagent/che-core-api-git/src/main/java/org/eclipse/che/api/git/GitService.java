/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.git;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.shared.AddRequest;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchCreateRequest;
import org.eclipse.che.api.git.shared.BranchDeleteRequest;
import org.eclipse.che.api.git.shared.BranchListRequest;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.git.shared.CloneRequest;
import org.eclipse.che.api.git.shared.CommitRequest;
import org.eclipse.che.api.git.shared.Commiters;
import org.eclipse.che.api.git.shared.ConfigRequest;
import org.eclipse.che.api.git.shared.DiffRequest;
import org.eclipse.che.api.git.shared.FetchRequest;
import org.eclipse.che.api.git.shared.InitRequest;
import org.eclipse.che.api.git.shared.LogRequest;
import org.eclipse.che.api.git.shared.MergeRequest;
import org.eclipse.che.api.git.shared.MergeResult;
import org.eclipse.che.api.git.shared.MoveRequest;
import org.eclipse.che.api.git.shared.PullRequest;
import org.eclipse.che.api.git.shared.PullResponse;
import org.eclipse.che.api.git.shared.PushRequest;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.RebaseRequest;
import org.eclipse.che.api.git.shared.RebaseResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.git.shared.RemoteAddRequest;
import org.eclipse.che.api.git.shared.RemoteListRequest;
import org.eclipse.che.api.git.shared.RemoteUpdateRequest;
import org.eclipse.che.api.git.shared.RepoInfo;
import org.eclipse.che.api.git.shared.ResetRequest;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.RmRequest;
import org.eclipse.che.api.git.shared.ShowFileContentRequest;
import org.eclipse.che.api.git.shared.ShowFileContentResponse;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.StatusFormat;
import org.eclipse.che.api.git.shared.Tag;
import org.eclipse.che.api.git.shared.TagCreateRequest;
import org.eclipse.che.api.git.shared.TagDeleteRequest;
import org.eclipse.che.api.git.shared.TagListRequest;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author andrew00x */
@Path("git")
public class GitService {

    private static final Logger LOG = LoggerFactory.getLogger(GitService.class);

    @Inject
    private GitConnectionFactory gitConnectionFactory;

    @Inject
    private ProjectRegistry projectRegistry;

    @QueryParam("projectPath")
    private String projectPath;

    @Path("add")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void add(AddRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            gitConnection.add(request);
        }
    }

    @Path("checkout")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void checkout(CheckoutRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            gitConnection.checkout(request);
        }
    }

    @Path("branch-create")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Branch branchCreate(BranchCreateRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return gitConnection.branchCreate(request);
        }
    }

    @Path("branch-delete")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void branchDelete(BranchDeleteRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            gitConnection.branchDelete(request);
        }
    }

    @Path("branch-rename")
    @POST
    public void branchRename(@QueryParam("oldName") String oldName,
                             @QueryParam("newName") String newName) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            gitConnection.branchRename(oldName, newName);
        }
    }

    @Path("branch-list")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public GenericEntity<List<Branch>> branchList(BranchListRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return new GenericEntity<List<Branch>>(gitConnection.branchList(request)) {
            };
        }
    }

    @Path("clone")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RepoInfo clone(final CloneRequest request) throws URISyntaxException, ApiException {
        long start = System.currentTimeMillis();
        // On-the-fly resolving of repository's working directory.
        request.setWorkingDir(getAbsoluteProjectPath(request.getWorkingDir()));
        LOG.info("Repository clone from '" + request.getRemoteUri() + "' to '" + request.getWorkingDir() + "' started");
        GitConnection gitConnection = getGitConnection();
        try {
            gitConnection.clone(request);
            return DtoFactory.getInstance().createDto(RepoInfo.class).withRemoteUri(request.getRemoteUri());
        } finally {
            long end = System.currentTimeMillis();
            long seconds = (end - start) / 1000;
            LOG.info("Repository clone from '" + request.getRemoteUri() + "' to '" + request.getWorkingDir()
                     + "' finished. Process took " + seconds + " seconds (" + seconds / 60 + " minutes)");
            gitConnection.close();
        }
    }

    @Path("commit")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Revision commit(CommitRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return gitConnection.commit(request);
        }
    }

    @Path("diff")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public InfoPage diff(DiffRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return gitConnection.diff(request);
        }
    }

    /**
     * Show file content from specified revision or branch.
     *
     * @param request
     *         request that contains file name with its full path and revision or branch
     * @return response that contains content of the file
     * @throws ApiException
     *         when some error occurred while retrieving the content of the file
     */
    @Path("show")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public ShowFileContentResponse showFileContent(ShowFileContentRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return gitConnection.showFileContent(request);
        }
    }

    @Path("fetch")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void fetch(FetchRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            gitConnection.fetch(request);
        }
    }

    @Path("init")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void init(final InitRequest request) throws ApiException {
        request.setWorkingDir(getAbsoluteProjectPath(projectPath));
        try (GitConnection gitConnection = getGitConnection()) {
            gitConnection.init(request);
        }
        projectRegistry.setProjectType(projectPath, GitProjectType.TYPE_ID, true);
    }

    @Path("log")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public LogPage log(LogRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return gitConnection.log(request);
        }
    }

    @Path("merge")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public MergeResult merge(MergeRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return gitConnection.merge(request);
        }
    }

    @Path("rebase")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RebaseResponse rebase(RebaseRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return gitConnection.rebase(request);
        }
    }

    @Path("mv")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void mv(MoveRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            gitConnection.mv(request);
        }
    }

    @Path("pull")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public PullResponse pull(PullRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return gitConnection.pull(request);
        }
    }

    @Path("push")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public PushResponse push(PushRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return gitConnection.push(request);
        }
    }

    @Path("remote-add")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void remoteAdd(RemoteAddRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            gitConnection.remoteAdd(request);
        }
    }

    @Path("remote-delete/{name}")
    @POST
    public void remoteDelete(@PathParam("name") String name) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            gitConnection.remoteDelete(name);
        }
    }

    @Path("remote-list")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public GenericEntity<List<Remote>> remoteList(RemoteListRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return new GenericEntity<List<Remote>>(gitConnection.remoteList(request)) {
            };
        }
    }

    @Path("remote-update")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void remoteUpdate(RemoteUpdateRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            gitConnection.remoteUpdate(request);
        }
    }

    @Path("reset")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void reset(ResetRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            gitConnection.reset(request);
        }
    }

    @Path("rm")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void rm(RmRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            gitConnection.rm(request);
        }
    }

    @Path("status")
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Status status(@QueryParam("format") StatusFormat format) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return gitConnection.status(format);
        }
    }

    @Path("tag-create")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Tag tagCreate(TagCreateRequest request) throws ApiException {
        GitConnection gitConnection = getGitConnection();
        try {
            return gitConnection.tagCreate(request);
        } finally {
            gitConnection.close();
        }
    }

    @Path("tag-delete")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void tagDelete(TagDeleteRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            gitConnection.tagDelete(request);
        }
    }


    @Path("config")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getConfig(ConfigRequest request) throws ApiException {
        Map<String, String> result = new HashMap<>();
        try (GitConnection gitConnection = getGitConnection()) {
            Config config = gitConnection.getConfig();
            if (request.isGetAll()) {
                for (String row : config.getList()) {
                    String[] keyValues = row.split("=", 2);
                    result.put(keyValues[0], keyValues[1]);
                }
            } else {
                for (String entry : request.getConfigEntry()) {
                    try {
                        String value = config.get(entry);
                        result.put(entry, value);
                    } catch (GitException exception) {
                        //value for this config property non found. Do nothing
                    }
                }
            }
        }
        return result;
    }

    @Path("tag-list")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public GenericEntity<List<Tag>> tagList(TagListRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return new GenericEntity<List<Tag>>(gitConnection.tagList(request)) {
            };
        }
    }

    @GET
    @Path("commiters")
    public Commiters getCommiters(@Context UriInfo uriInfo) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return DtoFactory.getInstance().createDto(Commiters.class).withCommiters(gitConnection.getCommiters());
        }
    }

    @GET
    @Path("delete-repository")
    public void deleteRepository(@Context UriInfo uriInfo) throws ApiException {
        final RegisteredProject project = projectRegistry.getProject(projectPath);
        final FolderEntry gitFolder = project.getBaseFolder().getChildFolder(".git");
        gitFolder.getVirtualFile().delete();
        projectRegistry.removeProjectType(projectPath, GitProjectType.TYPE_ID);
    }

    private String getAbsoluteProjectPath(String wsRelatedProjectPath) throws ApiException {
        final RegisteredProject project = projectRegistry.getProject(wsRelatedProjectPath);
        return project.getBaseFolder().getVirtualFile().toIoFile().getAbsolutePath();
    }

    private GitConnection getGitConnection() throws ApiException {
        return gitConnectionFactory.getConnection(getAbsoluteProjectPath(projectPath));
    }
}
