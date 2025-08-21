// SPDX-License-Identifier: MIT
package org.flameshot.densifier

/**
 * Tiny, non-invasive representation of the densifier "Join<A,B>" concept used
 * in documentation: a pair of a value and a projection function. This is a
 * lightweight utility intended for examples and small transformations inside
 * the kotlin-port module. It is intentionally simple and safe.
 */
data class Join<A, B>(val a: A, val f: (A) -> B) {
    /** Apply the projection to the stored value. */
    fun apply(): B = f(a)
}

/** Convenience aliases inspired by the densifier axioms (illustrative only). */
typealias Indexed<T> = Join<Int, (Int) -> T>
typealias Projection<X, T> = Join<X, (X) -> T>
