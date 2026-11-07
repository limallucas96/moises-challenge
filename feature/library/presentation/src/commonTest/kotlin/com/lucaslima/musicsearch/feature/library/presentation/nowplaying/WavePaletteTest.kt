package com.lucaslima.musicsearch.feature.library.presentation.nowplaying

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlin.test.Test

class WavePaletteTest {

    @Test
    fun capa_cinza_usa_a_paleta_padrao() {
        val gray = IntArray(100) { argb(0x80, 0x80, 0x80) }

        assertThat(wavePaletteFrom(gray)).isEqualTo(DefaultWavePalette)
    }

    @Test
    fun capa_escura_usa_a_paleta_padrao() {
        val dark = IntArray(100) { argb(0x20, 0x05, 0x05) }

        assertThat(wavePaletteFrom(dark)).isEqualTo(DefaultWavePalette)
    }

    @Test
    fun cores_vivas_vem_da_mais_presente_para_a_menos() {
        val pixels = IntArray(70) { argb(0xE0, 0x10, 0x10) } + IntArray(30) { argb(0x10, 0x20, 0xE0) }

        val palette = wavePaletteFrom(pixels)

        assertThat(palette).hasSize(2)
        assertThat(palette[0].red > palette[0].blue).isTrue()
        assertThat(palette[1].blue > palette[1].red).isTrue()
    }

    @Test
    fun uma_cor_so_ganha_uma_vizinha_para_virar_degrade() {
        val red = IntArray(100) { argb(0xE0, 0x10, 0x10) }

        assertThat(wavePaletteFrom(red)).hasSize(2)
    }

    @Test
    fun cor_com_presenca_pequena_fica_de_fora() {
        val pixels = IntArray(99) { argb(0x80, 0x80, 0x80) } + IntArray(1) { argb(0xE0, 0x10, 0x10) }

        assertThat(wavePaletteFrom(pixels)).isEqualTo(DefaultWavePalette)
    }

    private fun argb(
        r: Int,
        g: Int,
        b: Int,
    ) = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
}
