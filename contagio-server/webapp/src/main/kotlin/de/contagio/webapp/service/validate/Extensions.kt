package de.contagio.webapp.service.validate

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.reflect.CodeSignature

@Suppress("UNCHECKED_CAST")
inline fun <reified T> JoinPoint.getFirstParameterWithType(): T? {
    var result: T? = null

    val signature = this.signature as CodeSignature?
    signature?.let { cs ->
        for (i in this.args.indices) {
            if (cs.parameterTypes[i] == T::class.java) {
                result = this.args[i] as T?
                break
            }
        }
    }

    return result
}

inline fun <reified T> JoinPoint.getParameterWithName(name: String): T? {
    var result: T? = null

    val signature = this.signature as CodeSignature?
    signature?.let { cs ->
        for (i in this.args.indices) {
            if (cs.parameterNames[i] == name) {
                result = this.args[i] as T?
                break
            }
        }
    }

    return result
}
