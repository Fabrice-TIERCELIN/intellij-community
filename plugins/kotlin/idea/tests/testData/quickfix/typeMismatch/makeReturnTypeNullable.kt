// "Change return type of enclosing function 'foo' to 'String?'" "true"

fun foo(): String {
    return <caret>null
}
// FUS_QUICKFIX_NAME: org.jetbrains.kotlin.idea.quickfix.ChangeCallableReturnTypeFix$ForEnclosing