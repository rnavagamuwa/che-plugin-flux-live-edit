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
package org.eclipse.che.plugin.docker.client.params;

import org.eclipse.che.plugin.docker.client.dto.AuthConfigs;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * @author Mykola Morhun
 */
public class BuildImageParamsTest {

    private static final String             REPOSITORY                           = "repository";
    private static final String             TAG                                  = "tag";
    private static final AuthConfigs        AUTH_CONFIGS                         = mock(AuthConfigs.class);
    private static final Boolean            DO_FORCE_PULL                        = true;
    private static final Long               MEMORY_LIMIT                         = 12345L;
    private static final Long               MEMORY_SWAP_LIMIT                    = 67890L;
    private static final File               FILE                                 = new File(".");
    private static final List<File>         FILES;
    private static final String             DOCKERFILE                           = "/tmp/Dockerfile";
    private static final String             REMOTE                               = "https://github.com/someuser/remote.git";
    private static final Boolean            QUIET                                = false;
    private static final Boolean            NO_CACHE                             = false;
    private static final Boolean            REMOVE_INTERMEDIATE_CONTAINER        = true;
    private static final Boolean            FORCE_REMOVE_INTERMEDIATE_CONTAINERS = true;
    private static final Map<String,String> BUILD_ARGS                           = new HashMap<>();

    static {
        FILES = new ArrayList<>();
        FILES.add(FILE);
    }

    private BuildImageParams buildImageParams;

    @BeforeMethod
    private void prepare() {
        buildImageParams = BuildImageParams.create(FILE);
    }

    @Test
    public void shouldCreateParamsObjectWithRequiredParametersFromFiles() {
        buildImageParams = BuildImageParams.create(FILE);

        assertEquals(buildImageParams.getFiles(), FILES);

        assertNull(buildImageParams.getRepository());
        assertNull(buildImageParams.getTag());
        assertNull(buildImageParams.getAuthConfigs());
        assertNull(buildImageParams.isDoForcePull());
        assertNull(buildImageParams.getMemoryLimit());
        assertNull(buildImageParams.getMemorySwapLimit());
        assertNull(buildImageParams.getDockerfile());
        assertNull(buildImageParams.getRemote());
        assertNull(buildImageParams.isQuiet());
        assertNull(buildImageParams.isNoCache());
        assertNull(buildImageParams.isRemoveIntermediateContainer());
        assertNull(buildImageParams.isForceRemoveIntermediateContainers());
        assertNull(buildImageParams.getBuildArgs());
    }

    @Test
    public void shouldCreateParamsObjectWithRequiredParametersFromRemote() {
        buildImageParams = BuildImageParams.create(REMOTE);

        assertEquals(buildImageParams.getRemote(), REMOTE);

        assertNull(buildImageParams.getRepository());
        assertNull(buildImageParams.getTag());
        assertNull(buildImageParams.getAuthConfigs());
        assertNull(buildImageParams.isDoForcePull());
        assertNull(buildImageParams.getMemoryLimit());
        assertNull(buildImageParams.getMemorySwapLimit());
        assertNull(buildImageParams.getFiles());
        assertNull(buildImageParams.getDockerfile());
        assertNull(buildImageParams.isQuiet());
        assertNull(buildImageParams.isNoCache());
        assertNull(buildImageParams.isRemoveIntermediateContainer());
        assertNull(buildImageParams.isForceRemoveIntermediateContainers());
        assertNull(buildImageParams.getBuildArgs());
    }


    @Test
    public void shouldCreateParamsObjectWithAllPossibleParametersFromFiles() {
        buildImageParams = BuildImageParams.create(FILE)
                                           .withRepository(REPOSITORY)
                                           .withTag(TAG)
                                           .withAuthConfigs(AUTH_CONFIGS)
                                           .withDoForcePull(DO_FORCE_PULL)
                                           .withMemoryLimit(MEMORY_LIMIT)
                                           .withMemorySwapLimit(MEMORY_SWAP_LIMIT)
                                           .withDockerfile(DOCKERFILE)
                                           .withQuiet(QUIET)
                                           .withNoCache(NO_CACHE)
                                           .withRemoveIntermediateContainers(REMOVE_INTERMEDIATE_CONTAINER)
                                           .withForceRemoveIntermediateContainers(FORCE_REMOVE_INTERMEDIATE_CONTAINERS)
                                           .withBuildArgs(BUILD_ARGS);

        assertNull(buildImageParams.getRemote());

        assertEquals(buildImageParams.getFiles(), FILES);
        assertEquals(buildImageParams.getTag(), TAG);
        assertEquals(buildImageParams.getRepository(), REPOSITORY);
        assertEquals(buildImageParams.getAuthConfigs(), AUTH_CONFIGS);
        assertEquals(buildImageParams.isDoForcePull(), DO_FORCE_PULL);
        assertEquals(buildImageParams.getMemoryLimit(), MEMORY_LIMIT);
        assertEquals(buildImageParams.getMemorySwapLimit(), MEMORY_SWAP_LIMIT);
        assertEquals(buildImageParams.getDockerfile(), DOCKERFILE);
        assertEquals(buildImageParams.isQuiet(), QUIET);
        assertEquals(buildImageParams.isNoCache(), NO_CACHE);
        assertEquals(buildImageParams.isRemoveIntermediateContainer(), REMOVE_INTERMEDIATE_CONTAINER);
        assertEquals(buildImageParams.isForceRemoveIntermediateContainers(), FORCE_REMOVE_INTERMEDIATE_CONTAINERS);
        assertEquals(buildImageParams.getBuildArgs(), BUILD_ARGS);
    }

    @Test
    public void shouldCreateParamsObjectWithAllPossibleParametersFromRemote() {
        buildImageParams = BuildImageParams.create(REMOTE)
                                           .withRepository(REPOSITORY)
                                           .withTag(TAG)
                                           .withAuthConfigs(AUTH_CONFIGS)
                                           .withDoForcePull(DO_FORCE_PULL)
                                           .withMemoryLimit(MEMORY_LIMIT)
                                           .withMemorySwapLimit(MEMORY_SWAP_LIMIT)
                                           .withDockerfile(DOCKERFILE)
                                           .withQuiet(QUIET)
                                           .withNoCache(NO_CACHE)
                                           .withRemoveIntermediateContainers(REMOVE_INTERMEDIATE_CONTAINER)
                                           .withForceRemoveIntermediateContainers(FORCE_REMOVE_INTERMEDIATE_CONTAINERS)
                                           .withBuildArgs(BUILD_ARGS);

        assertNull(buildImageParams.getFiles());

        assertEquals(buildImageParams.getRemote(), REMOTE);
        assertEquals(buildImageParams.getTag(), TAG);
        assertEquals(buildImageParams.getRepository(), REPOSITORY);
        assertEquals(buildImageParams.getAuthConfigs(), AUTH_CONFIGS);
        assertEquals(buildImageParams.isDoForcePull(), DO_FORCE_PULL);
        assertEquals(buildImageParams.getMemoryLimit(), MEMORY_LIMIT);
        assertEquals(buildImageParams.getMemorySwapLimit(), MEMORY_SWAP_LIMIT);
        assertEquals(buildImageParams.getDockerfile(), DOCKERFILE);
        assertEquals(buildImageParams.isQuiet(), QUIET);
        assertEquals(buildImageParams.isNoCache(), NO_CACHE);
        assertEquals(buildImageParams.isRemoveIntermediateContainer(), REMOVE_INTERMEDIATE_CONTAINER);
        assertEquals(buildImageParams.isForceRemoveIntermediateContainers(), FORCE_REMOVE_INTERMEDIATE_CONTAINERS);
        assertEquals(buildImageParams.getBuildArgs(), BUILD_ARGS);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfFilesRequiredParameterIsNull() {
        File file = null;
        buildImageParams = BuildImageParams.create(file);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfFilesRequiredParameterResetWithNull() {
        File file = null;
        buildImageParams.withFiles(file);
    }

    @Test (expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfSetEmptyFilesArray() {
        File[] files = new File[0];
        buildImageParams.withFiles(files);
    }

    @Test (expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfSetFilesArrayWithNullElement() {
        buildImageParams.withFiles(FILE, null, FILE);
    }

    @Test (expectedExceptions = IllegalStateException.class)
    public void shouldThrowIllegalStateExceptionIfSetRemoteAfterSetFiles() {
        buildImageParams = BuildImageParams.create(FILE)
                                           .withRemote(REMOTE);
    }

    @Test (expectedExceptions = IllegalStateException.class)
    public void shouldThrowIllegalStateExceptionIfSetFileAfterSetRemote() {
        buildImageParams = BuildImageParams.create(REMOTE)
                                           .withFiles(FILE);
    }

    @Test (expectedExceptions = IllegalStateException.class)
    public void shouldThrowIllegalStateExceptionIfAddFileAfterSetRemote() {
        buildImageParams = BuildImageParams.create(REMOTE)
                                           .addFiles(FILE);
    }

    @Test
    public void repositoryParameterShouldEqualsNullIfItNotSet() {
        buildImageParams.withTag(TAG)
                        .withAuthConfigs(AUTH_CONFIGS)
                        .withDoForcePull(DO_FORCE_PULL)
                        .withMemoryLimit(MEMORY_LIMIT)
                        .withMemorySwapLimit(MEMORY_SWAP_LIMIT)
                        .withDockerfile(DOCKERFILE)
                        .withQuiet(QUIET)
                        .withNoCache(NO_CACHE)
                        .withRemoveIntermediateContainers(REMOVE_INTERMEDIATE_CONTAINER)
                        .withForceRemoveIntermediateContainers(FORCE_REMOVE_INTERMEDIATE_CONTAINERS)
                        .withBuildArgs(BUILD_ARGS);

        assertNull(buildImageParams.getRepository());
    }

    @Test
    public void tagParameterShouldEqualsNullIfItNotSet() {
        buildImageParams.withRepository(REPOSITORY)
                        .withAuthConfigs(AUTH_CONFIGS)
                        .withDoForcePull(DO_FORCE_PULL)
                        .withMemoryLimit(MEMORY_LIMIT)
                        .withMemorySwapLimit(MEMORY_SWAP_LIMIT)
                        .withDockerfile(DOCKERFILE)
                        .withQuiet(QUIET)
                        .withNoCache(NO_CACHE)
                        .withRemoveIntermediateContainers(REMOVE_INTERMEDIATE_CONTAINER)
                        .withForceRemoveIntermediateContainers(FORCE_REMOVE_INTERMEDIATE_CONTAINERS)
                        .withBuildArgs(BUILD_ARGS);

        assertNull(buildImageParams.getTag());
    }

    @Test
    public void authConfigParameterShouldEqualsNullIfItNotSet() {
        buildImageParams.withRepository(REPOSITORY)
                        .withTag(TAG)
                        .withDoForcePull(DO_FORCE_PULL)
                        .withMemoryLimit(MEMORY_LIMIT)
                        .withMemorySwapLimit(MEMORY_SWAP_LIMIT)
                        .withDockerfile(DOCKERFILE)
                        .withQuiet(QUIET)
                        .withNoCache(NO_CACHE)
                        .withRemoveIntermediateContainers(REMOVE_INTERMEDIATE_CONTAINER)
                        .withForceRemoveIntermediateContainers(FORCE_REMOVE_INTERMEDIATE_CONTAINERS)
                        .withBuildArgs(BUILD_ARGS);

        assertNull(buildImageParams.getAuthConfigs());
    }

    @Test
    public void doForcePullParameterShouldEqualsNullIfItNotSet() {
        buildImageParams.withRepository(REPOSITORY)
                        .withTag(TAG)
                        .withAuthConfigs(AUTH_CONFIGS)
                        .withMemoryLimit(MEMORY_LIMIT)
                        .withMemorySwapLimit(MEMORY_SWAP_LIMIT)
                        .withDockerfile(DOCKERFILE)
                        .withQuiet(QUIET)
                        .withNoCache(NO_CACHE)
                        .withRemoveIntermediateContainers(REMOVE_INTERMEDIATE_CONTAINER)
                        .withForceRemoveIntermediateContainers(FORCE_REMOVE_INTERMEDIATE_CONTAINERS)
                        .withBuildArgs(BUILD_ARGS);

        assertNull(buildImageParams.isDoForcePull());
    }

    @Test
    public void memoryLimitParameterShouldEqualsNullIfItNotSet() {
        buildImageParams.withRepository(REPOSITORY)
                        .withTag(TAG)
                        .withAuthConfigs(AUTH_CONFIGS)
                        .withDoForcePull(DO_FORCE_PULL)
                        .withMemorySwapLimit(MEMORY_SWAP_LIMIT)
                        .withDockerfile(DOCKERFILE)
                        .withQuiet(QUIET)
                        .withNoCache(NO_CACHE)
                        .withRemoveIntermediateContainers(REMOVE_INTERMEDIATE_CONTAINER)
                        .withForceRemoveIntermediateContainers(FORCE_REMOVE_INTERMEDIATE_CONTAINERS)
                        .withBuildArgs(BUILD_ARGS);

        assertNull(buildImageParams.getMemoryLimit());
    }

    @Test
    public void memorySwapLimitParameterShouldEqualsNullIfItNotSet() {
        buildImageParams.withRepository(REPOSITORY)
                        .withTag(TAG)
                        .withAuthConfigs(AUTH_CONFIGS)
                        .withDoForcePull(DO_FORCE_PULL)
                        .withMemoryLimit(MEMORY_LIMIT)
                        .withDockerfile(DOCKERFILE)
                        .withQuiet(QUIET)
                        .withNoCache(NO_CACHE)
                        .withRemoveIntermediateContainers(REMOVE_INTERMEDIATE_CONTAINER)
                        .withForceRemoveIntermediateContainers(FORCE_REMOVE_INTERMEDIATE_CONTAINERS)
                        .withBuildArgs(BUILD_ARGS);

        assertNull(buildImageParams.getMemorySwapLimit());
    }

    @Test
    public void dockerfileParameterShouldEqualsNullIfItNotSet() {
        buildImageParams.withRepository(REPOSITORY)
                        .withTag(TAG)
                        .withAuthConfigs(AUTH_CONFIGS)
                        .withDoForcePull(DO_FORCE_PULL)
                        .withMemoryLimit(MEMORY_LIMIT)
                        .withMemorySwapLimit(MEMORY_SWAP_LIMIT)
                        .withQuiet(QUIET)
                        .withNoCache(NO_CACHE)
                        .withRemoveIntermediateContainers(REMOVE_INTERMEDIATE_CONTAINER)
                        .withForceRemoveIntermediateContainers(FORCE_REMOVE_INTERMEDIATE_CONTAINERS)
                        .withBuildArgs(BUILD_ARGS);

        assertNull(buildImageParams.getDockerfile());
    }

    @Test
    public void quietParameterShouldEqualsNullIfItNotSet() {
        buildImageParams.withRepository(REPOSITORY)
                        .withTag(TAG)
                        .withAuthConfigs(AUTH_CONFIGS)
                        .withDoForcePull(DO_FORCE_PULL)
                        .withMemoryLimit(MEMORY_LIMIT)
                        .withMemorySwapLimit(MEMORY_SWAP_LIMIT)
                        .withDockerfile(DOCKERFILE)
                        .withNoCache(NO_CACHE)
                        .withRemoveIntermediateContainers(REMOVE_INTERMEDIATE_CONTAINER)
                        .withForceRemoveIntermediateContainers(FORCE_REMOVE_INTERMEDIATE_CONTAINERS)
                        .withBuildArgs(BUILD_ARGS);

        assertNull(buildImageParams.isQuiet());
    }

    @Test
    public void noCacheParameterShouldEqualsNullIfItNotSet() {
        buildImageParams.withRepository(REPOSITORY)
                        .withTag(TAG)
                        .withAuthConfigs(AUTH_CONFIGS)
                        .withDoForcePull(DO_FORCE_PULL)
                        .withMemoryLimit(MEMORY_LIMIT)
                        .withMemorySwapLimit(MEMORY_SWAP_LIMIT)
                        .withDockerfile(DOCKERFILE)
                        .withQuiet(QUIET)
                        .withRemoveIntermediateContainers(REMOVE_INTERMEDIATE_CONTAINER)
                        .withForceRemoveIntermediateContainers(FORCE_REMOVE_INTERMEDIATE_CONTAINERS)
                        .withBuildArgs(BUILD_ARGS);

        assertNull(buildImageParams.isNoCache());
    }

    @Test
    public void removeIntermediateContainerParameterShouldEqualsNullIfItNotSet() {
        buildImageParams.withRepository(REPOSITORY)
                        .withTag(TAG)
                        .withAuthConfigs(AUTH_CONFIGS)
                        .withDoForcePull(DO_FORCE_PULL)
                        .withMemoryLimit(MEMORY_LIMIT)
                        .withMemorySwapLimit(MEMORY_SWAP_LIMIT)
                        .withDockerfile(DOCKERFILE)
                        .withQuiet(QUIET)
                        .withNoCache(NO_CACHE)
                        .withForceRemoveIntermediateContainers(FORCE_REMOVE_INTERMEDIATE_CONTAINERS)
                        .withBuildArgs(BUILD_ARGS);

        assertNull(buildImageParams.isRemoveIntermediateContainer());
    }

    @Test
    public void forceRemoveIntermediateContainersParameterShouldEqualsNullIfItNotSet() {
        buildImageParams.withRepository(REPOSITORY)
                        .withTag(TAG)
                        .withAuthConfigs(AUTH_CONFIGS)
                        .withDoForcePull(DO_FORCE_PULL)
                        .withMemoryLimit(MEMORY_LIMIT)
                        .withMemorySwapLimit(MEMORY_SWAP_LIMIT)
                        .withDockerfile(DOCKERFILE)
                        .withQuiet(QUIET)
                        .withNoCache(NO_CACHE)
                        .withRemoveIntermediateContainers(REMOVE_INTERMEDIATE_CONTAINER)
                        .withBuildArgs(BUILD_ARGS);

        assertNull(buildImageParams.isForceRemoveIntermediateContainers());
    }

    @Test
    public void buildArgsParameterShouldEqualsNullIfItNotSet() {
        buildImageParams.withRepository(REPOSITORY)
                        .withTag(TAG)
                        .withAuthConfigs(AUTH_CONFIGS)
                        .withDoForcePull(DO_FORCE_PULL)
                        .withMemoryLimit(MEMORY_LIMIT)
                        .withMemorySwapLimit(MEMORY_SWAP_LIMIT)
                        .withDockerfile(DOCKERFILE)
                        .withQuiet(QUIET)
                        .withNoCache(NO_CACHE)
                        .withRemoveIntermediateContainers(REMOVE_INTERMEDIATE_CONTAINER)
                        .withForceRemoveIntermediateContainers(FORCE_REMOVE_INTERMEDIATE_CONTAINERS);

        assertNull(buildImageParams.getBuildArgs());
    }

    @Test (expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfAddNullAsFile() {
        File file = null;
        buildImageParams.addFiles(file);
    }

    @Test (expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfAddFilesArrayWithNullElement() {
        buildImageParams.addFiles(FILE, null, FILE);
    }

    @Test
    public void shouldAddFileToFilesList() {
        File file = new File("../");
        List<File> files = new ArrayList<>();
        files.add(FILE);
        files.add(file);

        buildImageParams.addFiles(file);

        assertEquals(buildImageParams.getFiles(), files);
    }

    @Test
    public void shouldAddBuildArgToBuildArgs() {
        String key = "ba_key";
        String value = "ba_value";

        buildImageParams.addBuildArg(key, value);

        assertNotNull(buildImageParams.getBuildArgs());
        assertEquals(buildImageParams.getBuildArgs().get(key), value);
    }

}
