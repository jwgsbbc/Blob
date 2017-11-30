package jwgs.blob

sealed class Result<PAYLOAD, ERROR> {
    data class Success<PAYLOAD, ERROR>(val payload: PAYLOAD): Result<PAYLOAD, ERROR>()
    data class Error<PAYLOAD, ERROR>(val error: ERROR): Result<PAYLOAD, ERROR>()
}