package jwgs.blob

sealed class Loadable<PAYLOAD, ERROR> {
    class Init<PAYLOAD, ERROR>: Loadable<PAYLOAD, ERROR>()
    class Loading<PAYLOAD, ERROR>: Loadable<PAYLOAD, ERROR>()
    data class Loaded<PAYLOAD, ERROR>(val value: PAYLOAD): Loadable<PAYLOAD, ERROR>()
    data class Error<PAYLOAD, ERROR>(val error: ERROR): Loadable<PAYLOAD, ERROR>()
}