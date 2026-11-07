package com.lucaslima.musicsearch.feature.library.presentation.components

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class TimeFormatTest {

    @Test
    fun minutos_e_segundos_com_dois_digitos() {
        assertThat(86.seconds.toClock()).isEqualTo("1:26")
        assertThat(5.seconds.toClock()).isEqualTo("0:05")
    }

    @Test
    fun a_partir_de_uma_hora_mostra_as_horas() {
        assertThat(3723.seconds.toClock()).isEqualTo("1:02:03")
    }

    @Test
    fun fracao_de_segundo_e_negativo_nao_aparecem() {
        assertThat(29_999.milliseconds.toClock()).isEqualTo("0:29")
        assertThat((-3).seconds.toClock()).isEqualTo("0:00")
        assertThat(Duration.ZERO.toClock()).isEqualTo("0:00")
    }
}
