// "Add 2nd parameter to function 'bar'" "true"
interface Foo

private fun Foo.bar(s: String, i: Int) {}

fun test(foo: Foo, b: Boolean) {
    foo.bar("", b<caret>, 0)
}
// FUS_QUICKFIX_NAME: org.jetbrains.kotlin.idea.quickfix.AddFunctionParametersFix