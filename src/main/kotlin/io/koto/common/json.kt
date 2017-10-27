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



sealed class Json {

    companion object {
        fun parse(json: String) = JParser(json).parse()
        fun obj(json: String) = parse(json)
        fun array(json: String) = parse(json)
        fun obj(vararg pairs : Pair<String, Any?>) = JObject().apply {
            pairs.forEach { set(it.first, it.second) }
        }
        fun array(vararg objs: Any?) = JArray().apply {
            objs.forEach { add(it) }
        }
    }

    class JException(message: String?) : Exception(message)

    open operator fun get(key: String): Json? {
        throw JException("Not Json Object")
    }
    open operator fun set(key: String, obj: Any?) {
        throw JException("Not Json Object")
    }
    open operator fun get(index: Int): Json? {
        throw JException("Not Json Array")
    }
    open operator fun set(index: Int, obj: Any?) {
        throw JException("Not Json Array")
    }
    open fun add(obj: Any?) {
        throw JException("Not Json Array")
    }
    operator fun plusAssign(obj: Any?) {
        add(obj)
    }
    open fun bool() : Boolean {
        throw JException("Not Boolean")
    }
    open fun string() : String {
        throw JException("Not String")
    }
    open fun int() : Int {
        throw JException("Not Int")
    }
    open fun float() : Float {
        throw JException("Not Float")
    }
    open fun double() : Double {
        throw JException("Not Double")
    }
    open fun jnull() : JNull{
        throw JException("Not Json Null")
    }
    fun jelement(obj:Any?) : Json {
        return if (obj == null) { JNull } else when(obj) {
            is Number -> JNumber(obj)
            is Boolean -> JBool(obj)
            is String -> JString(obj)
            is Json -> obj
            else -> error("type error!")
        }
    }

    object JNull : Json() {
        override fun jnull() = this
        override fun toString(): String = "null"
    }

    class JBool(private val v:Boolean) : Json() {
        override fun bool(): Boolean = v
        override fun toString(): String = v.toString()
    }

    class JNumber(private val v:Number) : Json() {
        override fun int(): Int = v.toInt()
        override fun float(): Float = v.toFloat()
        override fun double(): Double = v.toDouble()
        override fun toString(): String = v.toString()
    }

    class JString(private val v:String) : Json() {
        override fun string(): String = v
        override fun toString(): String = "\"$v\""
    }

    class JObject : Json() {
        private val map = mutableMapOf<String, Json>()
        override fun get(key: String): Json? {
            return map[key]
        }

        override fun set(key: String, obj: Any?) {
            map[key] = jelement(obj)
        }

        override fun toString(): String {
            return map.entries.map { "\"${it.key}\":${it.value}" }.joinToString(",","{","}")
        }
    }

    class JArray : Json() {
        val list = mutableListOf<Json>()
        override fun get(index: Int): Json? {
            return list[index]
        }
        override fun set(index: Int, obj: Any?) {
            list[index] = jelement(obj)
        }

        override fun add(obj: Any?) {
            list += jelement(obj)
        }

        override fun toString(): String {
            return list.joinToString(",","[","]")
        }
    }

    class JParser(val json:String) {
        private var at = 0
        private fun next() = json[at++]
        private val c : Char
            get() = json[at]
        private fun s(n:Int) : String {
            return json.substring(at until at+n)
        }
        private fun error(msg:String? = null) : Nothing {
            throw JException(msg)
        }
        private val spaces = listOf(' ', '\t', '\b', '\n', 'r')
        private fun escapeSpace() {
            while (c in spaces) {
                next()
            }
        }
        private fun isNext(str:String):Boolean {
            return tryOr(false) {
                s(str.length) == str
            }.also { at += str.length }
        }
        private fun parseNull() : JNull {
            if (isNext("null")) {
                return JNull
            } else {
                error()
            }
        }
        private fun parseTrue() : Boolean {
            if (isNext("true")) {
                return true
            } else {
                error()
            }
        }
        private fun parseFalse() : Boolean {
            if (isNext("false")) {
                return false
            } else {
                error()
            }
        }
        private val escapes = mapOf(
                'b' to '\b',
                'n' to '\n',
                't' to '\t',
                'r' to '\r',
                '\"' to '\"',
                '\\' to '\\'
        )
        private fun parseString() : String {
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
                    next()
                } catch (e:Throwable) {
                    error()
                }
            }
        }
        private fun parseDigit() : String {
            return buildString {
                do {
                    append(c)
                    next()
                } while (c in '0'..'9')
            }
        }
        private fun parseNumber() : Number {
            return buildString {
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
                            error()
                        }
                    }
                } else {
                    error()
                }
            }.toDouble()
        }

        private fun parseArray():JArray {
            return JArray().apply {
                next()
                escapeSpace()
                if (c != ']') {
                    loop@ while (true) {
                        add(parse())
                        escapeSpace()
                        when (next()) {
                            ',' -> continue@loop
                            ']' -> break@loop
                            else -> error()
                        }
                    }
                }
            }
        }
        private fun parsePair() : Pair<String, Json> {
            escapeSpace()
            if (c != '\"') error()
            val key = parseString()
            escapeSpace()
            if (next() != ':') error()
            val value = parse()
            return key to value
        }
        private fun parseObject():JObject {
            return JObject().apply {
                next()
                escapeSpace()
                if (c != '}') {
                    loop@ while (true) {
                        parsePair().let { set(it.first, it.second) }
                        escapeSpace()
                        when (next()) {
                            ',' -> continue@loop
                            '}' -> break@loop
                            else -> error()
                        }
                    }
                }
            }
        }
        fun parse() : Json {
            escapeSpace()
            return when(c) {
                '{'->parseObject()
                '['->parseArray()
                '\"'->JString(parseString())
                't'->JBool(parseTrue())
                'f'->JBool(parseFalse())
                'n'->parseNull()
                else->{
                    if (c=='-'||c=='+'||c in '0'..'9') {
                        JNumber(parseNumber())
                    } else {
                        error()
                    }
                }
            }
        }
    }
}





