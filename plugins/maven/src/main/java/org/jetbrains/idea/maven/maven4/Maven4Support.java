// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.idea.maven.maven4;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.roots.ui.distribution.DistributionInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.MavenVersionAwareSupportExtension;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.project.MavenProjectBundle;
import org.jetbrains.idea.maven.server.MavenDistribution;
import org.jetbrains.idea.maven.server.MavenDistributionsCache;
import org.jetbrains.idea.maven.server.MavenServer;
import org.jetbrains.idea.maven.server.MavenServerManager;
import org.jetbrains.idea.maven.utils.MavenLog;
import org.jetbrains.idea.maven.utils.MavenUtil;
import org.jetbrains.intellij.build.dependencies.BuildDependenciesCommunityRoot;
import org.jetbrains.intellij.build.impl.BundledMavenDownloader;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.jetbrains.idea.maven.server.MavenServerManager.BUNDLED_MAVEN_4;

final class Maven4Support implements MavenVersionAwareSupportExtension {
  @Override
  public boolean isSupportedByExtension(@Nullable File mavenHome) {
    String version = MavenUtil.getMavenVersion(mavenHome);
    return StringUtil.compareVersionNumbers(version, "4") >= 0;
  }

  @Override
  public @Nullable File getMavenHomeFile(@Nullable String mavenHome) {
    if (mavenHome == null) return null;
    if (StringUtil.equals(BUNDLED_MAVEN_4, mavenHome) ||
        StringUtil.equals(MavenProjectBundle.message("maven.bundled.version.4.title"), mavenHome)) {
      return MavenDistributionsCache.resolveEmbeddedMavenHome().getMavenHome().toFile();
    }
    return null;
  }

  @Override
  public @Nullable String asMavenHome(DistributionInfo distribution) {
    if (distribution instanceof Bundled4DistributionInfo) return BUNDLED_MAVEN_4;
    return null;
  }

  @Override
  public @Nullable DistributionInfo asDistributionInfo(String mavenHome) {
    if (StringUtil.equals(BUNDLED_MAVEN_4, mavenHome)) {
      return new Bundled4DistributionInfo(MavenDistributionsCache.resolveEmbeddedMavenHome().getVersion());
    }
    return null;
  }

  @Override
  public @NotNull List<String> supportedBundles() {
    return List.of();
    //return Collections.singletonList(BUNDLED_MAVEN_4);
  }

  @Override
  public @NotNull List<File> collectClassPathAndLibsFolder(@NotNull MavenDistribution distribution) {
    final File pluginFileOrDir = new File(PathUtil.getJarPathForClass(MavenServerManager.class));
    final String root = pluginFileOrDir.getParent();

    final List<File> classpath = new ArrayList<>();

    if (pluginFileOrDir.isDirectory()) {
      MavenLog.LOG.debug("collecting classpath for local run");
      prepareClassPathForLocalRunAndUnitTests(distribution.getVersion(), classpath, root);
    }
    else {
      MavenLog.LOG.debug("collecting classpath for production");
      prepareClassPathForProduction(distribution.getVersion(), classpath, root);
    }

    addMavenLibs(classpath, distribution.getMavenHome().toFile());
    MavenLog.LOG.debug("Collected classpath = ", classpath);
    return classpath;
  }

  private static void prepareClassPathForProduction(@NotNull String mavenVersion,
                                                    List<File> classpath,
                                                    String root) {
    classpath.add(new File(PathUtil.getJarPathForClass(MavenId.class)));
    classpath.add(new File(PathUtil.getJarPathForClass(MavenServer.class)));

    addDir(classpath, new File(root, "maven4-server-lib"), f -> true);

    classpath.add(new File(root, "maven40-server.jar"));
  }

  private static void prepareClassPathForLocalRunAndUnitTests(@NotNull String mavenVersion, List<File> classpath, String root) {
    BuildDependenciesCommunityRoot communityRoot = new BuildDependenciesCommunityRoot(Path.of(PathManager.getCommunityHomePath()));
    BundledMavenDownloader.INSTANCE.downloadMaven4LibsSync(communityRoot);

    classpath.add(new File(PathUtil.getJarPathForClass(MavenId.class)));
    classpath.add(new File(root, "intellij.maven.server"));
    File parentFile = MavenUtil.getMavenPluginParentFile();
    addDir(classpath, new File(parentFile, "maven40-server-impl/lib"), f -> true);

    classpath.add(new File(root, "intellij.maven.server.m40"));
  }

  private static void addMavenLibs(List<File> classpath, File mavenHome) {
    addDir(classpath, new File(mavenHome, "lib"), f -> !f.getName().contains("maven-slf4j-provider"));
    File bootFolder = new File(mavenHome, "boot");
    File[] classworldsJars = bootFolder.listFiles((dir, name) -> StringUtil.contains(name, "classworlds"));
    if (classworldsJars != null) {
      Collections.addAll(classpath, classworldsJars);
    }
  }

  private static void addDir(List<File> classpath, File dir, Predicate<File> filter) {
    File[] files = dir.listFiles();
    if (files == null) return;

    for (File jar : files) {
      if (jar.isFile() && jar.getName().endsWith(".jar") && filter.test(jar)) {
        classpath.add(jar);
      }
    }
  }
}
