package com.zubair.social.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Comment (chat bubble outline) icon. material-icons-core mein ye nahi hota, aur poora "extended" set
 * (~10MB) sirf ek icon ke liye jodna theek nahi, isliye ek path yahin define hai.
 */
val CommentIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "CommentOutline",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = PathParser().parsePathString(
            "M20,2H4c-1.1,0 -2,0.9 -2,2v18l4,-4h14c1.1,0 2,-0.9 2,-2V4c0,-1.1 -0.9,-2 -2,-2zM20,16H6l-2,2V4h16V16z"
        ).toNodes(),
        fill = SolidColor(Color.Black),   // asli rang Icon(tint) se aata hai
    ).build()
}
