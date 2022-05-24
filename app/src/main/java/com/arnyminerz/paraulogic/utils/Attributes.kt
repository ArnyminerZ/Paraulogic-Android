package com.arnyminerz.paraulogic.utils

import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.AttrRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

inline fun <T> Resources.Theme.getAttribute(
    @AttrRes attr: Int,
    block: (TypedArray) -> T,
): T {
    val a = obtainStyledAttributes(intArrayOf(attr))
    return block(a).also { a.recycle() }
}

fun Resources.Theme.getDrawableAttribute(@AttrRes attr: Int): Drawable =
    getAttribute(attr) { it.getDrawable(0)!! }

fun Resources.Theme.getDimensionAttribute(@AttrRes attr: Int, defaultValue: Float = 0f): Float =
    getAttribute(attr) { it.getDimension(0, defaultValue) }

fun Resources.Theme.getStringAttribute(@AttrRes attr: Int): String? =
    getAttribute(attr) { it.getString(0) }

fun Resources.Theme.getColorAttribute(@AttrRes attr: Int, defaultValue: Int = 0): Int =
    getAttribute(attr) { it.getColor(0, defaultValue) }


@Composable
fun getDimensionAttribute(@AttrRes attr: Int, defaultValue: Float = 0f): Float =
    LocalContext.current.theme.getDimensionAttribute(attr, defaultValue)

@Composable
fun getStringAttribute(@AttrRes attr: Int): String? =
    LocalContext.current.theme.getStringAttribute(attr)

@Composable
fun getColorAttribute(@AttrRes attr: Int, defaultValue: Int = 0): Int =
    LocalContext.current.theme.getColorAttribute(attr, defaultValue)
