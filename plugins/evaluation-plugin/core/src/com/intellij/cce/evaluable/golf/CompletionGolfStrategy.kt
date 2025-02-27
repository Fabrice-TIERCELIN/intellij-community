// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.evaluable.golf

import com.intellij.cce.core.SuggestionSource
import com.intellij.cce.evaluable.EvaluationStrategy
import com.intellij.cce.evaluable.completion.CompletionType
import com.intellij.cce.filter.EvaluationFilter


/**
 * @param checkLine Check if expected line starts with suggestion from completion
 * @param invokeOnEachChar Close popup after unsuccessful completion and invoke again
 * @param checkToken In case first token in suggestion equals to first token in expected string, we can pick only first token from suggestion.

If completion suggest only one token - this option is useless (see checkLine ↑). Suitable for full line or multiple token completions
 * @param source Take suggestions, with specific source
 *  - STANDARD  - standard non-full line completion
 *  - CODOTA    - <a href="https://plugins.jetbrains.com/plugin/7638-codota">https://plugins.jetbrains.com/plugin/7638-codota</a>
 *  - TAB_NINE  - <a href="https://github.com/codota/tabnine-intellij">https://github.com/codota/tabnine-intellij</a>
 *  - INTELLIJ  - <a href="https://jetbrains.team/p/ccrm/code/fl-inference">https://jetbrains.team/p/ccrm/code/fl-inference</a>
 * @param topN Take only N top suggestions, applying after filtering by source
 * @param suggestionsProvider Name of provider of suggestions (use DEFAULT for IDE completion)
 */
data class CompletionGolfStrategy(
  val mode: CompletionGolfMode,
  val checkLine: Boolean,
  val invokeOnEachChar: Boolean,

  val checkToken: Boolean,
  val source: SuggestionSource?,
  var topN: Int,

  val suggestionsProvider: String,
  val pathToZipModel: String?,
  val completionType: CompletionType) : EvaluationStrategy {
  override val filters: Map<String, EvaluationFilter> = emptyMap()

  fun isDefaultProvider(): Boolean = suggestionsProvider == DEFAULT_PROVIDER

  companion object {
    const val DEFAULT_PROVIDER: String = "DEFAULT"
  }

  class Builder constructor(val mode: CompletionGolfMode) {
    var checkLine: Boolean = true
    var invokeOnEachChar: Boolean = false

    var checkToken: Boolean = true
    var source: SuggestionSource? = null
    var topN: Int = -1

    var suggestionsProvider: String = DEFAULT_PROVIDER
    var pathToZipModel: String? = null
    var completionType: CompletionType = CompletionType.ML

    fun build(): CompletionGolfStrategy = CompletionGolfStrategy(
      mode = mode,
      checkLine = checkLine,
      invokeOnEachChar = invokeOnEachChar,
      checkToken = checkToken,
      source = source,
      topN = topN,
      suggestionsProvider = suggestionsProvider,
      pathToZipModel = pathToZipModel,
      completionType = completionType
    )
  }
}


enum class CompletionGolfMode {
  ALL,
  TOKENS
}



