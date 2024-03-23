package sk.rsvoboda.zettai.fp

fun <U : Any> CharSequence?.unlessNullOrEmpty(f: (CharSequence) -> U): U? =
    if (isNullOrEmpty())
        null
    else
        f(this)

fun <T> T.printIt(prefix: String = ">"): T =
    also {
        println("$prefix $this")
    }

fun <T:Any> tryOrNull(block: () -> T): T? =
    try{
        block()
    } catch (e: Exception) {
        null
    }

fun <T> T.discardUnless(predicate: T.() -> Boolean): T? =
    if (predicate())
        this
    else
        null

typealias FUN<A, B> = (A) -> B

infix fun <A : Any, B : Any, C : Any> FUN<A, B?>.andUnlessNull(other: FUN<B, C?>): FUN<A, C?> =
    { a: A -> this(a)?.let(other) }