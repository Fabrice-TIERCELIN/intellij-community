// "Remove supertype" "true"
open class C1 {}
open class C2 {}
class C3: C1(),C2<caret>() {}

/* IGNORE_FIR */
// FUS_QUICKFIX_NAME: org.jetbrains.kotlin.idea.quickfix.RemoveSupertypeFix