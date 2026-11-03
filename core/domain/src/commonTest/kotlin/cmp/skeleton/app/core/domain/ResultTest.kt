package cmp.skeleton.app.core.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class ResultTest {

    @Test
    fun map_transforma_apenas_o_sucesso() {
        val success: Result<Int, DataError> = Result.Success(2)
        val failure: Result<Int, DataError> = Result.Error(DataError.Local.UNKNOWN)

        assertThat(success.map { it * 10 }).isEqualTo(Result.Success(20))
        assertThat(failure.map { it * 10 }).isEqualTo(Result.Error(DataError.Local.UNKNOWN))
    }

    @Test
    fun mapError_troca_o_tipo_do_erro() {
        val failure: Result<Int, DataError.Network> = Result.Error(DataError.Network.NO_INTERNET)

        val mapped = failure.mapError { DataError.Local.UNKNOWN }

        assertThat(mapped).isEqualTo(Result.Error(DataError.Local.UNKNOWN))
    }
}
