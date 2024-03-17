package sk.rsvoboda.zettai.fp

fun <U : Any> CharSequence?.unlessNullOrEmpty(f: (CharSequence) -> U): U? =
    if (isNullOrEmpty()) null else f(this)

fun <T> T.printIt(prefix: String = ">"): T =
    also{
        println("$prefix $this")
    }

//    fun <A,B,C,R> partial(f: (A,B,C) -> R, a: A) : (B,C)->R = {
//        b,c -> f(a,b,c)
//    }