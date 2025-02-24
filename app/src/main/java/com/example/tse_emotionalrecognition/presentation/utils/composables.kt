package com.example.tse_emotionalrecognition.presentation.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import kotlinx.coroutines.delay

/**
 * TODO rausbekommen, wie man Icons hinzufügen kann.
 *  die waren schon drinnen vgl. Code in EIS
 */

@Composable
fun ScreenHeight(): Float {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = remember {
        val displayMetrics = context.resources.displayMetrics
        val screenHeightPx = displayMetrics.heightPixels
        screenHeightPx / displayMetrics.density
    }
    return screenHeight.dp.value
}

class LabelButton (
    val label: String,
    val color: Long,
    val func: ()->Unit
)

@Composable
fun RowButton(
    text:String,
    onClick:()->Unit,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
){
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null){
                    Icon(
                        imageVector = icon,
                        contentDescription = null
                    )
                }
                Text(
                    text = text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
fun Accept(
    accept:()->Unit,
    decline:()->Unit,
    text:String?=null,
    enabled: Boolean = true,
    space: Dp = 5.dp,
    fontSize: TextUnit = 15.sp,
    iconSize: Dp = 28.dp,
    modifier: Modifier = Modifier
){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (text!=null){
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(10.dp))

        }
        Button(
            onClick = accept,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary
            ),
            enabled = enabled,
            modifier = Modifier.size(iconSize),
        ) {
//            Icon(
//                imageVector = Icons.Default.Check,
//                contentDescription = null
//            )
        }
        Spacer(modifier = Modifier.width(space))
        Button(
            onClick = decline,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary
            ),
            modifier = Modifier.size(iconSize),
        ) {
//            Icon(
//                imageVector = Icons.Default.Close,
//                contentDescription = null,
//            )
        }
    }
}

@Composable
fun Divider(color: Color, thickness: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color = color)
    )
}

@Composable
fun InterventionText(
    heading:String,
    text:String,
    finished: (()->Unit)? = null,
    modifier: Modifier = Modifier
){
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(
                    if (finished == null){
                        (0.5f * ScreenHeight()).dp - 6.dp
                    }else {
                        (0.4f * ScreenHeight()).dp - 6.dp
                    }
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = heading,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Divider(color = Color.White, thickness = 2.dp)
        Spacer(modifier = Modifier.height(5.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(
                    if (finished == null){
                        (0.5f * ScreenHeight()).dp - 6.dp
                    }else {
                        (0.4f * ScreenHeight()).dp - 6.dp
                    }
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
        if (finished != null){
            RowButton(
                text="Finished",
                onClick = finished,
                enabled = true,
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}




@Composable
fun FullText(
    text:String,
    backgroundColor: MainColor = MainColor.RED,
    finished: (()->Unit)? = null,
    buttonDelay: Long = 5000L,
    modifier: Modifier = Modifier
){
    var enableButton by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = enableButton) {
        delay(buttonDelay)
        enableButton = true
    }


    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor.colorValue)
            .verticalScroll(scrollState)
        ,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .padding(18.dp)
                .defaultMinSize(
                    minHeight = (0.7f * ScreenHeight()).dp
                )
        ) {
            Text(
                text = text,
                fontSize = 20.sp,
                color = backgroundColor.fontColor,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
        if (finished != null){
            RowButton(
                text="Finished",
                onClick = finished,
                enabled = enableButton,
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun LabelView(
    text: String,
    accept: ()->Unit,
    decline: ()->Unit,
    buttonDelay: Long =1000L,
    modifier: Modifier = Modifier
) {
    var enableButton by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = enableButton) {
        delay(buttonDelay)
        enableButton = true
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height((0.5f * ScreenHeight()).dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = text,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height((0.5f * ScreenHeight()).dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Accept(
                accept=accept,
                decline=decline,
                enabled = enableButton,
                iconSize = 36.dp,
                space = 20.dp,
            )
        }
    }
}

@Composable
fun ColorPicker (
    text: String,
    color: Int,
    defaultColor: MainColor = MainColor.AQUA,
    onValueChange:(Int)-> Unit
){
    var choosenColor: Int by remember { mutableStateOf(color) }
    var colorSet: MainColor by remember { mutableStateOf(colorDict.getOrDefault(color, MainColor.GREEN)) }

    Spacer(modifier = Modifier.height(5.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(5.dp,0.dp)
            .background(colorSet.colorValue, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            // Left column (15%)
            Box(
                modifier = Modifier
                    .weight(0.15f) // 15% of the available width
            ) {
                Button(
                    onClick = {
                        choosenColor = (choosenColor - 1 + colorDict.size) % colorDict.size
                        colorSet = colorDict.getOrDefault(choosenColor.toInt(),defaultColor)
                        onValueChange(choosenColor)
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = colorSet.colorValue,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
//                    Icon(
//                        imageVector = Icons.Default.ArrowBack,
//                        contentDescription = null,
//                        modifier = Modifier
//                            .size(24.dp)
//                    )
                }
            }
            // Middle column (rest of the available width)
            Box(
                modifier = Modifier
                    .weight(0.7f) // Takes the remaining space
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().height(50.dp)

                ) {
                    Text(
                        colorSet.colorName,
                        fontSize = 12.sp,
                        color = colorSet.fontColor,
                    )
                    Text(
                        text,
                        fontWeight = FontWeight.SemiBold,
                        color = colorSet.fontColor,
                        fontSize = 8.sp
                    )
                }
            }

            // Right column (15%)
            Box(
                modifier = Modifier
                    .weight(0.15f)
            ) {
                Button(
                    onClick = {
                        choosenColor = (choosenColor + 1 + colorDict.size) % colorDict.size
                        colorSet = colorDict.getOrDefault(choosenColor.toInt(),defaultColor)
                        onValueChange(choosenColor)
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = colorSet.colorValue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
//                    Icon(
//                        imageVector = Icons.Default.ArrowForward,
//                        contentDescription = null,
//                        modifier = Modifier
//                            .size(24.dp)
//                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(5.dp))
}

enum class MainColor(val colorName: String, val colorValue: Color, val fontColor: Color) {
    RED("Red", Color(0x99AA0000), Color.White), // 1 represents white
    ORANGE("Orange", Color(0x99FFA500), Color.Black), // 0 represents black
    YELLOW("Yellow", Color(0x99FFFF00), Color.Black), // 0 represents black
    GREEN("Green", Color(0x99008000), Color.White), // 0 represents black
    LIME("Lime", Color(0x9900FF00), Color.Black), // 0 represents black
    AQUA("Aqua", Color(0x9900AAFF), Color.Black), // 0 represents black
    CYAN("Cyan", Color(0x9900FFFF), Color.Black), // 0 represents black
    BLUE("Blue", Color(0x990000FF), Color.White), // 1 represents white
    INDIGO("Indigo", Color(0x994B0082), Color.White), // 1 represents white
    VIOLET("Violet", Color(0x99EE82EE), Color.Black), // 0 represents black
    FUCHSIA("Fuchsia", Color(0x99FF00FF), Color.White), // 0 represents black
    PURPLE("Purple", Color(0x99800080), Color.White), // 1 represents white
    PINK("Pink", Color(0x99FFC0CB), Color.Black), // 0 represents black
    SILVER("Silver", Color(0x99C0C0C0), Color.Black), // 0 represents black
    GOLD("Gold", Color(0x99FFD700), Color.Black); // 0 represents black
    companion object {
        fun fromInt(colorValue: Color): MainColor? {
            return values().find { it.colorValue == colorValue }
        }
    }
}

public val colorDict = mapOf(
    1 to MainColor.RED,
    2 to MainColor.ORANGE,
    3 to MainColor.YELLOW,
    4 to MainColor.GREEN,
    5 to MainColor.LIME,
    6 to MainColor.AQUA,
    7 to MainColor.CYAN,
    8 to MainColor.BLUE,
    9 to MainColor.INDIGO,
    10 to MainColor.VIOLET,
    11 to MainColor.FUCHSIA,
    12 to MainColor.PURPLE,
    13 to MainColor.PINK,
    14 to MainColor.SILVER,
    15 to MainColor.GOLD
)

@Composable
fun TextPicker (
    text: String,
    selected: String = "",
    textList: List<String> = emptyList(),
    random: Boolean = false,
    onValueChange: (String) -> Unit = {}
){
    val textSelect = if (selected in textList) selected else ""
    val shuffledTextList = if (random) textList.shuffled() else textList

    var index: Int? by remember {
        mutableStateOf(
            shuffledTextList.indexOf(textSelect).takeIf { it != -1 }
        )
    }

    Spacer(modifier = Modifier.height(5.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp)
            .padding(5.dp,0.dp)
            .background(Color.DarkGray, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            // Left column (15%)
            Box(
                modifier = Modifier
                    .weight(0.15f) // 15% of the available width
            ) {
                Button(
                    onClick = {
                        index = if (index != null) {
                            (index!! - 1 + shuffledTextList.size) % shuffledTextList.size
                        } else {
                            (0 - 1 + shuffledTextList.size) % shuffledTextList.size
                        }
                        onValueChange(shuffledTextList[index!!])
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Gray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
//                    Icon(
//                        imageVector = Icons.Default.ArrowBack,
//                        contentDescription = null,
//                        modifier = Modifier
//                            .size(24.dp)
//                    )
                }
            }
            // Middle column (rest of the available width)
            Box(
                modifier = Modifier
                    .weight(0.7f) // Takes the remaining space
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(4.dp)
                ) {
                    Text(
                        text = if (index != null){
                            shuffledTextList[index!!]
                        } else {
                            textSelect
                        },
                        maxLines = 4,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.sp
                    )
                }
            }
            // Right column (15%)
            Box(
                modifier = Modifier
                    .weight(0.15f)
            ) {
                Button(
                    onClick = {
                        index = if (index != null) {
                            (index!! - 1 + shuffledTextList.size) % shuffledTextList.size
                        } else {
                            (0 + 1 + shuffledTextList.size) % shuffledTextList.size
                        }
                        onValueChange(shuffledTextList[index!!])
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Gray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
//                    Icon(
//                        imageVector = Icons.Default.ArrowForward,
//                        contentDescription = null,
//                        modifier = Modifier
//                            .size(24.dp)
//                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(5.dp))
}