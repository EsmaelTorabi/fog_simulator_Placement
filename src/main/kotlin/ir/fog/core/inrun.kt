package ir.fog.core

/**
 * @author mohsen on 1/9/22
 */

class inrun():testInterface {
    init {
        var a = a(this)
        var b = b(this)
    }

    override fun doSth(s: String) {
        println(s)
    }
}

class a(testInterface: testInterface) {

    init {
        testInterface.doSth("hello a")
    }
}

class b(testInterface: testInterface) {

    init {
        testInterface.doSth("hello b")
    }
}

interface testInterface{
    fun doSth(s:String);
}

object mewo{
    @JvmStatic
    fun main(args: Array<String>) {
        inrun()
    }
}
