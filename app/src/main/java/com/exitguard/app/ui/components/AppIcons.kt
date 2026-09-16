package com.exitguard.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {

    val Security: ImageVector = ImageVector.Builder(
        name = "Security",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12.0f, 1.0f)
            lineTo(3.0f, 5.0f)
            verticalLineToRelative(6.0f)
            curveToRelative(0.0f, 5.55f, 3.84f, 10.74f, 9.0f, 12.0f)
            curveToRelative(5.16f, -1.26f, 9.0f, -6.45f, 9.0f, -12.0f)
            verticalLineTo(5.0f)
            lineToRelative(-9.0f, -4.0f)
            close()
        }
    }.build()

    val TouchApp: ImageVector = ImageVector.Builder(
        name = "TouchApp",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(9.0f, 11.24f)
            verticalLineTo(7.5f)
            curveToRelative(0.0f, -1.38f, 1.12f, -2.5f, 2.5f, -2.5f)
            reflectiveCurveToRelative(2.5f, 1.12f, 2.5f, 2.5f)
            verticalLineToRelative(3.74f)
            curveToRelative(1.21f, -0.81f, 2.0f, -2.18f, 2.0f, -3.74f)
            curveToRelative(0.0f, -2.48f, -2.02f, -4.5f, -4.5f, -4.5f)
            reflectiveCurveTo(7.0f, 5.02f, 7.0f, 7.5f)
            curveToRelative(0.0f, 1.56f, 0.79f, 2.93f, 2.0f, 3.74f)
            close()
            moveToRelative(9.84f, 4.63f)
            lineToRelative(-4.54f, -2.26f)
            curveToRelative(-0.17f, -0.07f, -0.35f, -0.11f, -0.54f, -0.11f)
            horizontalLineTo(13.0f)
            verticalLineTo(7.5f)
            curveToRelative(0.0f, -0.83f, -0.67f, -1.5f, -1.5f, -1.5f)
            reflectiveCurveTo(10.0f, 6.67f, 10.0f, 7.5f)
            verticalLineToRelative(10.74f)
            curveToRelative(-3.6f, -0.76f, -3.72f, -0.78f, -3.92f, -0.78f)
            curveToRelative(-0.4f, 0.0f, -0.77f, 0.16f, -1.04f, 0.43f)
            lineToRelative(-0.93f, 0.94f)
            lineToRelative(5.37f, 5.38f)
            curveToRelative(0.6f, 0.6f, 1.41f, 0.94f, 2.26f, 0.94f)
            horizontalLineToRelative(6.88f)
            curveToRelative(1.53f, 0.0f, 2.82f, -1.09f, 3.09f, -2.59f)
            lineToRelative(0.79f, -4.4f)
            curveToRelative(0.14f, -0.77f, -0.24f, -1.53f, -0.94f, -1.87f)
            close()
        }
    }.build()

    val Settings: ImageVector = ImageVector.Builder(
        name = "Settings",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(19.14f, 12.94f)
            curveToRelative(0.04f, -0.3f, 0.06f, -0.61f, 0.06f, -0.94f)
            curveToRelative(0.0f, -0.32f, -0.02f, -0.64f, -0.07f, -0.94f)
            lineToRelative(2.03f, -1.58f)
            curveToRelative(0.18f, -0.14f, 0.23f, -0.41f, 0.12f, -0.61f)
            lineToRelative(-1.92f, -3.32f)
            curveToRelative(-0.12f, -0.22f, -0.37f, -0.29f, -0.59f, -0.22f)
            lineToRelative(-2.39f, 0.96f)
            curveToRelative(-0.5f, -0.38f, -1.03f, -0.7f, -1.62f, -0.94f)
            lineTo(14.4f, 2.81f)
            curveToRelative(-0.04f, -0.24f, -0.24f, -0.41f, -0.48f, -0.41f)
            horizontalLineToRelative(-3.84f)
            curveToRelative(-0.24f, 0.0f, -0.43f, 0.17f, -0.47f, 0.41f)
            lineTo(9.25f, 5.35f)
            curveToRelative(-0.59f, 0.24f, -1.13f, 0.56f, -1.62f, 0.94f)
            lineToRelative(-2.39f, -0.96f)
            curveToRelative(-0.22f, -0.08f, -0.47f, 0.0f, -0.59f, 0.22f)
            lineTo(2.74f, 8.87f)
            curveToRelative(-0.12f, 0.21f, -0.08f, 0.47f, 0.12f, 0.61f)
            lineToRelative(2.03f, 1.58f)
            curveToRelative(-0.05f, 0.3f, -0.09f, 0.63f, -0.09f, 0.94f)
            reflectiveCurveToRelative(0.02f, 0.64f, 0.07f, 0.94f)
            lineToRelative(-2.03f, 1.58f)
            curveToRelative(-0.18f, 0.14f, -0.23f, 0.41f, -0.12f, 0.61f)
            lineToRelative(1.92f, 3.32f)
            curveToRelative(0.12f, 0.22f, 0.37f, 0.29f, 0.59f, 0.22f)
            lineToRelative(2.39f, -0.96f)
            curveToRelative(0.5f, 0.38f, 1.03f, 0.7f, 1.62f, 0.94f)
            lineToRelative(0.36f, 2.54f)
            curveToRelative(0.05f, 0.24f, 0.24f, 0.41f, 0.48f, 0.41f)
            horizontalLineToRelative(3.84f)
            curveToRelative(0.24f, 0.0f, 0.44f, -0.17f, 0.47f, -0.41f)
            lineToRelative(0.36f, -2.54f)
            curveToRelative(0.59f, -0.24f, 1.13f, -0.56f, 1.62f, -0.94f)
            lineToRelative(2.39f, 0.96f)
            curveToRelative(0.22f, 0.08f, 0.47f, 0.0f, 0.59f, -0.22f)
            lineToRelative(1.92f, -3.32f)
            curveToRelative(0.12f, -0.22f, 0.07f, -0.47f, -0.12f, -0.61f)
            lineToRelative(-2.01f, -1.58f)
            close()
            moveTo(12.0f, 15.6f)
            curveToRelative(-1.98f, 0.0f, -3.6f, -1.62f, -3.6f, -3.6f)
            reflectiveCurveToRelative(1.62f, -3.6f, 3.6f, -3.6f)
            reflectiveCurveToRelative(3.6f, 1.62f, 3.6f, 3.6f)
            reflectiveCurveToRelative(-1.62f, 3.6f, -3.6f, 3.6f)
            close()
        }
    }.build()

    val Android: ImageVector = ImageVector.Builder(
        name = "Android",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(6.0f, 18.0f)
            curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
            horizontalLineToRelative(1.0f)
            verticalLineToRelative(3.5f)
            curveToRelative(0.0f, 0.83f, 0.67f, 1.5f, 1.5f, 1.5f)
            reflectiveCurveToRelative(1.5f, -0.67f, 1.5f, -1.5f)
            verticalLineTo(19.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(3.5f)
            curveToRelative(0.0f, 0.83f, 0.67f, 1.5f, 1.5f, 1.5f)
            reflectiveCurveToRelative(1.5f, -0.67f, 1.5f, -1.5f)
            verticalLineTo(19.0f)
            horizontalLineToRelative(1.0f)
            curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
            verticalLineTo(8.0f)
            horizontalLineTo(6.0f)
            verticalLineToRelative(10.0f)
            close()
            moveTo(3.5f, 8.0f)
            curveTo(2.67f, 8.0f, 2.0f, 8.67f, 2.0f, 9.5f)
            verticalLineToRelative(7.0f)
            curveToRelative(0.0f, 0.83f, 0.67f, 1.5f, 1.5f, 1.5f)
            reflectiveCurveTo(5.0f, 17.33f, 5.0f, 16.5f)
            verticalLineToRelative(-7.0f)
            curveTo(5.0f, 8.67f, 4.33f, 8.0f, 3.5f, 8.0f)
            close()
            moveToRelative(17.0f, 0.0f)
            curveToRelative(-0.83f, 0.0f, -1.5f, 0.67f, -1.5f, 1.5f)
            verticalLineToRelative(7.0f)
            curveToRelative(0.0f, 0.83f, 0.67f, 1.5f, 1.5f, 1.5f)
            reflectiveCurveToRelative(1.5f, -0.67f, 1.5f, -1.5f)
            verticalLineToRelative(-7.0f)
            curveToRelative(0.0f, -0.83f, -0.67f, -1.5f, -1.5f, -1.5f)
            close()
            moveToRelative(-4.97f, -4.84f)
            lineToRelative(1.3f, -1.3f)
            curveToRelative(0.2f, -0.2f, 0.2f, -0.51f, 0.0f, -0.71f)
            curveToRelative(-0.2f, -0.2f, -0.51f, -0.2f, -0.71f, 0.0f)
            lineToRelative(-1.48f, 1.48f)
            curveTo(13.85f, 2.23f, 12.95f, 2.0f, 12.0f, 2.0f)
            curveToRelative(-0.96f, 0.0f, -1.86f, 0.23f, -2.66f, 0.63f)
            lineTo(7.85f, 1.15f)
            curveToRelative(-0.2f, -0.2f, -0.51f, -0.2f, -0.71f, 0.0f)
            curveToRelative(-0.2f, 0.2f, -0.2f, 0.51f, 0.0f, 0.71f)
            lineToRelative(1.31f, 1.31f)
            curveTo(6.97f, 4.26f, 6.0f, 6.01f, 6.0f, 8.0f)
            horizontalLineToRelative(12.0f)
            curveToRelative(0.0f, -1.99f, -0.97f, -3.75f, -2.47f, -4.84f)
            close()
            moveTo(10.0f, 5.0f)
            horizontalLineTo(9.0f)
            verticalLineTo(4.0f)
            horizontalLineToRelative(1.0f)
            verticalLineToRelative(1.0f)
            close()
            moveToRelative(5.0f, 0.0f)
            horizontalLineToRelative(-1.0f)
            verticalLineTo(4.0f)
            horizontalLineToRelative(1.0f)
            verticalLineToRelative(1.0f)
            close()
        }
    }.build()
}
