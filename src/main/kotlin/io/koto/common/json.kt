package io.koto.common

import com.google.gson.*

fun JsonObject.put(name:String, value:Any) {
    when (value) {
        is JsonElement -> add(name, value)
        is Boolean -> addProperty(name, value)
        is Number -> addProperty(name, value)
        is String -> addProperty(name, value)
        is Char -> addProperty(name, value)
        else -> throw JsonIOException("value is not json element!")
    }
}

fun JsonArray.put(value:Any) {
    when (value) {
        is JsonElement -> add(value)
        is Boolean -> add(value)
        is Number -> add(value)
        is String -> add(value)
        is Char -> add(value)
        else -> throw JsonIOException("value is not json element!")
    }
}


fun obj(s:String): JsonObject = JsonParser().parse(s).asJsonObject
fun array(s:String): JsonArray = JsonParser().parse(s).asJsonArray
inline fun obj(block: JsonObject.()->Unit) = JsonObject().apply(block)
inline fun array(block: JsonArray.() -> Unit) = JsonArray().apply(block)
inline fun obj(vararg pairs : Pair<String, Any>) = obj { pairs.forEach { put(it.first, it.second) } }
inline fun array(vararg values : Any) = array { values.forEach { put(it) } }

fun JsonObject.obj(s:String) = get(s).asJsonObject
fun JsonObject.int(s:String) = get(s).asInt
fun JsonObject.bool(s:String) = get(s).asBoolean
fun JsonObject.float(s:String) = get(s).asFloat
fun JsonObject.array(s:String) = get(s).asJsonArray
fun JsonObject.str(s:String) = get(s).asString
fun JsonObject.long(s:String) = get(s).asLong
inline fun <reified T, R> JsonObject.one(s:String, predicate: T.() -> R?) : R = array(s).one(predicate)?:throw JsonIOException("not that object")
inline fun <reified T> JsonObject.one(s:String) : T = array(s).one()?:throw JsonIOException("not that object")
inline fun <reified R> JsonObject.oneobj(s:String, predicate: JsonObject.() -> R?) : R = one(s, predicate)?:throw JsonIOException("not that object")
inline fun JsonObject.oneobj(s:String) : JsonObject = one(s)?:throw JsonIOException("not that object")
inline fun <reified T> JsonObject.array(s: String, action: (T) -> Unit) = array(s).forEach(action)
inline fun JsonObject.objs(s: String, action: JsonObject.() -> Unit) = array(s, action)

inline fun <reified T> JsonArray.forEach(action: T.() -> Unit): Unit {
    for (i in 0..(size()-1)) {
        val o = get(i)
        if (o is T) o.action()
    }
}

inline fun <reified T, R> JsonArray.one(predicate: T.() -> R?) : R? {
    forEach<T> {
        val r = predicate()
        if (r!=null) return r
    }
    return null
}

inline fun <reified T> JsonArray.one() : T? {
    forEach<T> {
        return this
    }
    return null
}

class JException(message: String?) : Exception(message)

interface JElement {
    operator fun get(key: String): JElement? {
        throw JException("Not Json Object")
    }
    operator fun set(key: String, obj: JElement) {
        throw JException("Not Json Object")
    }
    operator fun get(index: Int): JElement? {
        throw JException("Not Json Array")
    }
    operator fun set(index: Int, obj: JElement) {
        throw JException("Not Json Array")
    }
    fun add(obj: JElement) {
        throw JException("Not Json Array")
    }
    operator fun plusAssign(obj: JElement) {
        throw JException("Not Json Array")
    }
    fun bool() : Boolean {
        throw JException("Not Boolean")
    }
    fun string() : String {
        throw JException("Not String")
    }
    fun int() : Int {
        throw JException("Not Int")
    }
    fun float() : Float {
        throw JException("Not Float")
    }
    fun double() : Double {
        throw JException("Not Double")
    }
    fun jnull() : JNull{
        throw JException("Not Json Null")
    }
}

object JNull : JElement {
    override fun jnull() = this
}

class JBool(private val v:Boolean) : JElement {
    override fun bool(): Boolean = v
}

//class JInt(val v:Int) : JElement {
//    override fun int(): Int = v
//}
//
//class JFloat(val v:Double) : JElement {
//    override fun float(): Double = v
//}

class JNumber(private val v:Number) : JElement {
    override fun int(): Int = v.toInt()
    override fun float(): Float = v.toFloat()
    override fun double(): Double = v.toDouble()
}

class JString(private val v:String) : JElement {
    override fun string(): String = v
}

class JObject : JElement {
    val map = mutableMapOf<String, JElement>()
    override fun get(key: String): JElement? {
        return map[key]
    }

    override fun set(key: String, obj: JElement) {
        map[key] = obj
    }
}

class JArray : JElement {
    val list = mutableListOf<JElement>()
    override fun get(index: Int): JElement? {
        return list[index]
    }
    override fun set(index: Int, obj: JElement) {
        list[index] = obj
    }

    override fun plusAssign(obj: JElement) {
        list += obj
    }

    override fun add(obj: JElement) {
        list += obj
    }
}

class JParser(val json:String) {
//    val array = json.toCharArray()
    private var at = 0
    private fun next() = at++
    private val c : Char
        get() = json[at]
    private fun s(n:Int) : String {
        return json.substring(at until at+n)
    }
    private fun error(msg:String) : Nothing {
        throw JException(msg)
    }
    val spaces = listOf(' ', '\t', '\b', '\n', 'r')
    fun escapeSpace() {
        while (c in spaces) {
            next()
        }
    }
    fun parseChar(ch:Char) {
        escapeSpace()
        if (c != ch) error("need comma!")
        escapeSpace()
    }
    fun isNext(str:String):Boolean {
        return tryOr(false) {
            s(str.length) == str
        }
    }
    fun parseNull() : JNull {
        if (isNext("null")) {
            return JNull
        } else {
            error("Null parse fail!")
        }
    }
    fun parseTrue() : JBool {
        if (isNext("true")) {
            return JBool(true)
        } else {
            error("Boolean(true) parse fail!")
        }
    }
    fun parseFalse() : JBool {
        if (isNext("false")) {
            return JBool(false)
        } else {
            error("Boolean(false) parse fail!")
        }
    }
    val escapes = mapOf(
            'b' to '\b',
            'n' to '\n',
            't' to '\t',
            'r' to '\r',
            '\"' to '\"',
            '\\' to '\\'
    )
    fun parseString() : String {
        return buildString {
            try {
                next()
                while (c!='\"') {
                    if (c == '\\') {
                        next()
                        if (c == 'u') {
                            appendCodePoint(s(4).toInt(16))
                        } else {
                            append(escapes[c])
                        }
                    } else {
                        append(c)
                    }
                    next()
                }
            } catch (e:Throwable) {
                error("String parse fail!")
            }
        }
    }
    fun parseDigit() : String {
        return buildString {
            do {
                append(c)
                next()
            } while (c in '0'..'9')
        }
    }
    fun parseNumber() : JNumber {
        return JNumber(buildString {
            if (c=='+' || c=='-') {
                append(c)
                next()
            }
            if (c in '0'..'9') {
                append(parseDigit())
                if (c == '.') {
                    append(c)
                    next()
                    if (c in '0'..'9') {
                        append(parseDigit())
                    }
                }
                if (c == 'e' || c == 'E') {
                    append(c)
                    next()
                    if (c == '+' || c == '-') {
                        append(c)
                        next()
                    }
                    if (c in '0'..'9') {
                        append(parseDigit())
                    } else {
                        error("Number parse fail!")
                    }
                }
            } else {
                error("Number parse fail!")
            }
        }.toDouble())
    }

    fun parseArray():JArray {
        return JArray().apply {
            next()
            escapeSpace()
            while (true) {
                if (c == ']') {
                    break
                }
                add(parse())
                parseChar(',')
            }
        }
    }
    fun parseObject():JObject {
        return JObject().apply {
            next()
            escapeSpace()
            loop@ while (true) {
                if (c == '}') {
                    break
                } else if (c == '\"') {
                    val key = parseString()
                    parseChar(':')
                    set(key, parse())
                    parseChar(',')
                } else {
                    error("Object parse fail!")
                }
            }
        }
    }
    fun parse() : JElement {
        escapeSpace()
        return when(c) {
            '{'->parseObject()
            '['->parseArray()
            '\"'->JString(parseString())
            't'->parseTrue()
            'f'->parseFalse()
            'n'->parseNull()
            else->{
                if (c=='-'||c=='+'||c in '0'..'9') {
                    parseNumber()
                } else {
                    error("json parse fail!")
                }
            }
        }
    }
}

