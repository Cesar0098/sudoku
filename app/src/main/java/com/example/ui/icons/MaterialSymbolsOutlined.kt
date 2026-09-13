package com.example.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Iconos de Material Symbols Outlined que no existen (o no con este trazo)
 * en el paquete clasico de Material Icons que ya usa el resto de la app.
 * Bajados con los ejes variables pedidos: opsz 24, wght 400, FILL 0, GRAD 0,
 * ROND 50 (terminales redondeadas al 50%). El color del path queda en negro
 * a proposito - Icon() lo tinea igual que a cualquier otro ImageVector via
 * su parametro `tint`, exactamente como hacen los Icons.Filled.* de siempre.
 */
object MaterialSymbolsOutlined {

    val Delete: ImageVector
        get() {
            if (_delete != null) return _delete!!
            _delete = ImageVector.Builder(
                name = "delete",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1f,
                    stroke = null,
                    strokeAlpha = 1f,
                    strokeLineWidth = 1f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Bevel,
                    strokeLineMiter = 1f,
                    pathFillType = PathFillType.NonZero,
                ) {
                    moveTo(7f, 21f)
                    quadTo(6.18f, 21f, 5.59f, 20.41f)
                    reflectiveQuadTo(5f, 19f)
                    verticalLineTo(6f)
                    horizontalLineTo(4f)
                    verticalLineTo(4f)
                    horizontalLineTo(9f)
                    verticalLineTo(3f)
                    horizontalLineToRelative(6f)
                    verticalLineTo(4f)
                    horizontalLineToRelative(5f)
                    verticalLineTo(6f)
                    horizontalLineTo(19f)
                    verticalLineTo(19f)
                    quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                    reflectiveQuadTo(17f, 21f)
                    horizontalLineTo(7f)
                    close()
                    moveTo(17f, 6f)
                    horizontalLineTo(7f)
                    verticalLineTo(19f)
                    horizontalLineTo(17f)
                    verticalLineTo(6f)
                    close()
                    moveTo(9f, 17f)
                    horizontalLineToRelative(2f)
                    verticalLineTo(8f)
                    horizontalLineTo(9f)
                    verticalLineToRelative(9f)
                    close()
                    moveToRelative(4f, 0f)
                    horizontalLineToRelative(2f)
                    verticalLineTo(8f)
                    horizontalLineTo(13f)
                    verticalLineToRelative(9f)
                    close()
                    moveTo(7f, 6f)
                    verticalLineTo(19f)
                    verticalLineTo(6f)
                    close()
                }
            }.build()
            return _delete!!
        }

    val BorderColor: ImageVector
        get() {
            if (_borderColor != null) return _borderColor!!
            _borderColor = ImageVector.Builder(
                name = "border_color",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1f,
                    stroke = null,
                    strokeAlpha = 1f,
                    strokeLineWidth = 1f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Bevel,
                    strokeLineMiter = 1f,
                    pathFillType = PathFillType.NonZero,
                ) {
                    moveTo(2f, 24f)
                    verticalLineTo(20f)
                    horizontalLineTo(22f)
                    verticalLineToRelative(4f)
                    horizontalLineTo(2f)
                    close()
                    moveTo(6f, 16f)
                    horizontalLineTo(7.4f)
                    lineTo(15.2f, 8.23f)
                    lineTo(14.48f, 7.5f)
                    lineTo(13.78f, 6.8f)
                    lineTo(6f, 14.6f)
                    verticalLineTo(16f)
                    close()
                    moveTo(4f, 18f)
                    verticalLineTo(13.75f)
                    lineTo(15.2f, 2.57f)
                    quadTo(15.48f, 2.3f, 15.84f, 2.15f)
                    reflectiveQuadTo(16.6f, 2f)
                    reflectiveQuadToRelative(0.77f, 0.15f)
                    reflectiveQuadTo(18.05f, 2.6f)
                    lineTo(19.43f, 4f)
                    quadToRelative(0.3f, 0.27f, 0.44f, 0.65f)
                    reflectiveQuadTo(20f, 5.43f)
                    quadTo(20f, 5.8f, 19.86f, 6.16f)
                    reflectiveQuadTo(19.43f, 6.82f)
                    lineTo(8.25f, 18f)
                    horizontalLineTo(4f)
                    close()
                    moveTo(18f, 5.4f)
                    lineTo(16.6f, 4f)
                    lineTo(18f, 5.4f)
                    close()
                    moveTo(15.2f, 8.23f)
                    lineTo(14.48f, 7.5f)
                    lineTo(13.78f, 6.8f)
                    lineTo(15.2f, 8.23f)
                    close()
                }
            }.build()
            return _borderColor!!
        }

    val ArrowTopLeft: ImageVector
        get() {
            if (_arrowTopLeft != null) return _arrowTopLeft!!
            _arrowTopLeft = ImageVector.Builder(
                name = "arrow_top_left",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1f,
                    stroke = null,
                    strokeAlpha = 1f,
                    strokeLineWidth = 1f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Bevel,
                    strokeLineMiter = 1f,
                    pathFillType = PathFillType.NonZero,
                ) {
                    moveTo(17f, 20f)
                    verticalLineTo(10f)
                    horizontalLineTo(7.83f)
                    lineToRelative(3.6f, 3.6f)
                    lineToRelative(-1.4f, 1.43f)
                    lineTo(4f, 9f)
                    lineTo(10f, 3f)
                    lineToRelative(1.43f, 1.42f)
                    lineTo(7.83f, 8f)
                    horizontalLineTo(19f)
                    verticalLineTo(20f)
                    horizontalLineTo(17f)
                    close()
                }
            }.build()
            return _arrowTopLeft!!
        }

    val Edit: ImageVector
        get() {
            if (_edit != null) return _edit!!
            _edit = ImageVector.Builder(
                name = "edit",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1f,
                    stroke = null,
                    strokeAlpha = 1f,
                    strokeLineWidth = 1f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Bevel,
                    strokeLineMiter = 1f,
                    pathFillType = PathFillType.NonZero,
                ) {
                    moveTo(5f, 19f)
                    horizontalLineTo(6.43f)
                    lineTo(16.2f, 9.23f)
                    lineTo(14.78f, 7.8f)
                    lineTo(5f, 17.58f)
                    verticalLineTo(19f)
                    close()
                    moveTo(3f, 21f)
                    verticalLineTo(16.75f)
                    lineTo(16.2f, 3.57f)
                    quadTo(16.5f, 3.3f, 16.86f, 3.15f)
                    reflectiveQuadTo(17.63f, 3f)
                    quadToRelative(0.4f, 0f, 0.78f, 0.15f)
                    reflectiveQuadTo(19.05f, 3.6f)
                    lineTo(20.43f, 5f)
                    quadToRelative(0.3f, 0.27f, 0.44f, 0.65f)
                    reflectiveQuadTo(21f, 6.4f)
                    quadToRelative(0f, 0.4f, -0.14f, 0.76f)
                    reflectiveQuadTo(20.43f, 7.82f)
                    lineTo(7.25f, 21f)
                    horizontalLineTo(3f)
                    close()
                    moveTo(19f, 6.4f)
                    lineTo(17.6f, 5f)
                    lineTo(19f, 6.4f)
                    close()
                    moveTo(15.48f, 8.52f)
                    lineTo(14.78f, 7.8f)
                    lineTo(16.2f, 9.23f)
                    lineTo(15.48f, 8.52f)
                    close()
                }
            }.build()
            return _edit!!
        }

    val Timer: ImageVector
        get() {
            if (_timer != null) return _timer!!
            _timer = ImageVector.Builder(
                name = "timer",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1f,
                    stroke = null,
                    strokeAlpha = 1f,
                    strokeLineWidth = 1f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Bevel,
                    strokeLineMiter = 1f,
                    pathFillType = PathFillType.NonZero,
                ) {
                    moveTo(9f, 3f)
                    verticalLineTo(1f)
                    horizontalLineToRelative(6f)
                    verticalLineTo(3f)
                    horizontalLineTo(9f)
                    close()
                    moveToRelative(2f, 11f)
                    horizontalLineToRelative(2f)
                    verticalLineTo(8f)
                    horizontalLineTo(11f)
                    verticalLineToRelative(6f)
                    close()
                    moveTo(8.51f, 21.29f)
                    quadTo(6.88f, 20.58f, 5.65f, 19.35f)
                    reflectiveQuadTo(3.71f, 16.49f)
                    reflectiveQuadTo(3f, 13f)
                    reflectiveQuadTo(3.71f, 9.51f)
                    reflectiveQuadTo(5.65f, 6.65f)
                    quadTo(6.88f, 5.43f, 8.51f, 4.71f)
                    reflectiveQuadTo(12f, 4f)
                    quadToRelative(1.55f, 0f, 2.98f, 0.5f)
                    reflectiveQuadToRelative(2.68f, 1.45f)
                    lineToRelative(1.4f, -1.4f)
                    lineToRelative(1.4f, 1.4f)
                    lineToRelative(-1.4f, 1.4f)
                    quadTo(20f, 8.6f, 20.5f, 10.02f)
                    reflectiveQuadTo(21f, 13f)
                    quadToRelative(0f, 1.85f, -0.71f, 3.49f)
                    reflectiveQuadToRelative(-1.94f, 2.86f)
                    reflectiveQuadToRelative(-2.86f, 1.94f)
                    reflectiveQuadTo(12f, 22f)
                    reflectiveQuadTo(8.51f, 21.29f)
                    close()
                    moveToRelative(8.44f, -3.34f)
                    quadTo(19f, 15.9f, 19f, 13f)
                    reflectiveQuadTo(16.95f, 8.05f)
                    reflectiveQuadTo(12f, 6f)
                    reflectiveQuadTo(7.05f, 8.05f)
                    reflectiveQuadTo(5f, 13f)
                    reflectiveQuadToRelative(2.05f, 4.95f)
                    reflectiveQuadTo(12f, 20f)
                    reflectiveQuadToRelative(4.95f, -2.05f)
                    close()
                    moveTo(12f, 13f)
                    close()
                }
            }.build()
            return _timer!!
        }

    private var _delete: ImageVector? = null
    private var _borderColor: ImageVector? = null
    private var _arrowTopLeft: ImageVector? = null
    private var _edit: ImageVector? = null
    private var _timer: ImageVector? = null
}
