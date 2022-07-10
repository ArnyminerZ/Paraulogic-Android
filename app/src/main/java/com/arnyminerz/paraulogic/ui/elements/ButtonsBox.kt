package com.arnyminerz.paraulogic.ui.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arnyminerz.paraulogic.ui.shapes.HexagonalShape

private const val BUTTON_SIDE = 100

private const val MID_SIDE_PADDING = 2 * BUTTON_SIDE - 0.15 * BUTTON_SIDE
private const val TOP_SIDE_PADDING = BUTTON_SIDE - 0.075 * BUTTON_SIDE
private const val TOP_BOTT_PADDING = 2 * BUTTON_SIDE - 0.4 * BUTTON_SIDE

/**
 * A button with an hexagonal shape, with a letter in it.
 * @author Arnau Mora
 * @since 20220307
 * @param letter The letter that should be shown in the button's text.
 * @param isCentral If the color should be accent or primary.
 * @param modifier Modifiers to apply to the button.
 * @param onClick Will be called when the button is tapped.
 */
@Composable
private fun LetterButton(
    letter: Char,
    isCentral: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = letter != '\u0000',
        shape = HexagonalShape,
        modifier = modifier.size(BUTTON_SIDE.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor =
            if (isCentral)
                MaterialTheme.colorScheme.tertiary
            else
                MaterialTheme.colorScheme.primary,
            contentColor =
            if (isCentral)
                MaterialTheme.colorScheme.onTertiary
            else
                MaterialTheme.colorScheme.onPrimary,
        )
    ) {
        Text(
            (letter
                .takeIf { it == '\u0000' }
                ?: letter).toString()
        )
    }
}

/**
 * Displays a box with 7 hexagonal buttons displayed in a circular manner.
 * @author Arnau Mora
 * @since 20220307
 * @param letters Must be a 6 character string. Each letter of the String matches the letter to
 * display on the button at that index.
 * @param onClick Will get called when any button is tapped.
 */
@Composable
fun ButtonsBox(
    letters: String,
    onClick: (index: Int, letter: Char) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        // Top row
        LetterButton(
            letters[0],
            false,
            Modifier
                .align(Alignment.Center)
                .padding(
                    end = TOP_SIDE_PADDING.dp,
                    bottom = TOP_BOTT_PADDING.dp,
                )
        ) { onClick(0, letters[0]) }
        LetterButton(
            letters[1],
            false,
            Modifier
                .align(Alignment.Center)
                .padding(
                    start = TOP_SIDE_PADDING.dp,
                    bottom = TOP_BOTT_PADDING.dp,
                )
        ) { onClick(1, letters[1]) }

        // Middle row
        LetterButton(
            letters[2],
            false,
            Modifier
                .align(Alignment.Center)
                .padding(end = MID_SIDE_PADDING.dp)
        ) { onClick(2, letters[2]) }
        LetterButton(
            letters[3],
            true,
            Modifier.align(Alignment.Center)
        ) { onClick(3, letters[3]) }
        LetterButton(
            letters[4],
            false,
            Modifier
                .align(Alignment.Center)
                .padding(start = MID_SIDE_PADDING.dp)
        ) { onClick(4, letters[4]) }

        // Bottom row
        LetterButton(
            letters[5],
            false,
            Modifier
                .align(Alignment.Center)
                .padding(
                    end = TOP_SIDE_PADDING.dp,
                    top = TOP_BOTT_PADDING.dp,
                )
        ) { onClick(5, letters[5]) }
        LetterButton(
            letters[6],
            false,
            Modifier
                .align(Alignment.Center)
                .padding(
                    start = TOP_SIDE_PADDING.dp,
                    top = TOP_BOTT_PADDING.dp,
                )
        ) { onClick(6, letters[6]) }
    }
}