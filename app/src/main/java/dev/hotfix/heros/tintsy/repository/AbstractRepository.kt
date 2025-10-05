package dev.hotfix.heros.tintsy.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

abstract class AbstractRepository<T> (val dispatcher: CoroutineDispatcher){

    protected suspend fun execute(cleanUpWork: () -> Unit = {}, work: () -> T): Result<T> {
        return withContext(dispatcher) {
            try {
                val result = work()
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                cleanUpWork()
            }
        }
    }
}