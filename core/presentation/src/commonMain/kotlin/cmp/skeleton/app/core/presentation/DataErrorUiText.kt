package cmp.skeleton.app.core.presentation

import cmp.skeleton.app.core.domain.DataError
import cmp.skeleton.app.core.presentation.resources.Res
import cmp.skeleton.app.core.presentation.resources.error_disk_full
import cmp.skeleton.app.core.presentation.resources.error_no_internet
import cmp.skeleton.app.core.presentation.resources.error_request_timeout
import cmp.skeleton.app.core.presentation.resources.error_serialization
import cmp.skeleton.app.core.presentation.resources.error_server
import cmp.skeleton.app.core.presentation.resources.error_too_many_requests
import cmp.skeleton.app.core.presentation.resources.error_unauthorized
import cmp.skeleton.app.core.presentation.resources.error_unknown

fun DataError.toUiText(): UiText = UiText.Resource(
    when (this) {
        DataError.Network.NO_INTERNET -> Res.string.error_no_internet

        DataError.Network.REQUEST_TIMEOUT -> Res.string.error_request_timeout

        DataError.Network.UNAUTHORIZED,
        DataError.Network.FORBIDDEN,
        -> Res.string.error_unauthorized

        DataError.Network.TOO_MANY_REQUESTS -> Res.string.error_too_many_requests

        DataError.Network.SERVER_ERROR,
        DataError.Network.SERVICE_UNAVAILABLE,
        -> Res.string.error_server

        DataError.Network.SERIALIZATION -> Res.string.error_serialization

        DataError.Local.DISK_FULL -> Res.string.error_disk_full

        else -> Res.string.error_unknown
    },
)
