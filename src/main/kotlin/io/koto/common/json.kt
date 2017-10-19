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


