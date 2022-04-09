package me.ndts.tumark


fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> =
    this.fold(
        onSuccess = transform,
        onFailure = { Result.failure(it) }
    )

fun <T> T.asSuccess(): Result<T> = Result.success(this)

fun <T> Result<T>.filter(exception: Throwable, predicate: (T) -> Boolean): Result<T> =
    this.fold(
        onSuccess = {
            if (predicate(it)) {
                this
            } else {
                Result.failure(exception)
            }
        },
        onFailure = { this }
    )
