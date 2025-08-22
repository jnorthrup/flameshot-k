package org.flameshot

/**
 * Minimal implementation used to drive the ExampleGapTest from red to green.
 * This is intentionally tiny and safe; it can be expanded later per real user stories.
 */
object Gap {
    /**
     * Returns true when the intentional demo gap is resolved.
     */
    fun isResolved(): Boolean = true

    // Simple in-memory reasons store for demo/GAP purposes.
    private val _reasons: MutableList<String> = mutableListOf()

    /** Record a reason for the gap (append). */
    fun record(reason: String) {
        _reasons.add(reason)
    }

    /** Return a snapshot of recorded reasons. */
    fun reasons(): List<String> = _reasons.toList()

    /** Clear recorded reasons. */
    fun clear() { _reasons.clear() }
}
