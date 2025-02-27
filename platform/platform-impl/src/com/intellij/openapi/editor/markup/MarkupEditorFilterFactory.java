// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.openapi.editor.markup;

import com.intellij.openapi.diff.impl.DiffUtil;
import org.jetbrains.annotations.NotNull;

public final class MarkupEditorFilterFactory {
  private static final MarkupEditorFilter IS_DIFF_FILTER = editor -> DiffUtil.isDiffEditor(editor);
  private static final MarkupEditorFilter NOT_DIFF_FILTER = createNotFilter(IS_DIFF_FILTER);

  @NotNull
  public static MarkupEditorFilter createNotFilter(@NotNull MarkupEditorFilter filter) {
    return editor -> !filter.avaliableIn(editor);
  }

  @NotNull
  public static MarkupEditorFilter createIsDiffFilter() {
    return IS_DIFF_FILTER;
  }

  @NotNull
  public static MarkupEditorFilter createIsNotDiffFilter() {
    return NOT_DIFF_FILTER;
  }
}
