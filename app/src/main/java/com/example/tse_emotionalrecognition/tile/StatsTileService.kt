package com.example.tse_emotionalrecognition.tile

import android.content.Context
import androidx.annotation.FloatRange
import androidx.compose.ui.unit.dp
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.layouts.EdgeContentLayout
import androidx.wear.protolayout.material.CircularProgressIndicator
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.ProgressIndicatorColors
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.layouts.MultiSlotLayout
import androidx.wear.tiles.EventBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper.singleTimelineEntryTileBuilder
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.tse_emotionalrecognition.common.data.database.UserDataStore
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import com.google.common.util.concurrent.ListenableFuture

@OptIn(ExperimentalHorologistApi::class)
class StatsTileService : SuspendingTileService() {

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest) = resources()

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        return dynamicTile(requestParams, this)
    }

    override fun onTileEnterEvent(requestParams: EventBuilders.TileEnterEvent) {
        super.onTileEnterEvent(requestParams)
        getUpdater(this).requestUpdate(StatsTileService::class.java)
    }

    private fun resources(): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion("0")
            .build()
    }

    @Preview(device = WearDevices.SMALL_ROUND)
    @Preview(device = WearDevices.LARGE_ROUND)
    fun preview(context: Context) = TilePreviewData() {
        tile(
            requestParams = it,
            context = context,
            completed = 1,
            missed = 2,
            missed_to_completed = 0.6f
        )
    }

    private suspend fun dynamicTile(
        requestParams: RequestBuilders.TileRequest,
        context: Context,
    ): TileBuilders.Tile {

        val repo = UserDataStore.getUserRepository(context)
        val interventionStats = repo.getInterventionStatsById(1)

        val completed = interventionStats.triggeredCount ?: 3  // Default-Werte für Debugging
        val missed = interventionStats.dismissedCount ?: 2

        val missed_to_completed = if(completed == 0 || missed == 0) 1f else (missed.toFloat() / completed.toFloat())

        val singleTileTimeline = TimelineBuilders.Timeline.Builder()
            .addTimelineEntry(
                TimelineBuilders.TimelineEntry.Builder()
                    .setLayout(
                        LayoutElementBuilders.Layout.Builder()
                            .setRoot(
                                layout(
                                    context,
                                    requestParams.deviceConfiguration,
                                    completed,
                                    missed,
                                    missed_to_completed
                                )
                            )
                            .build()
                    )
                    .build()
            )
            .build()

        return TileBuilders.Tile.Builder()
            .setTileTimeline(singleTileTimeline)
            .build()
    }

    private fun tile(
        requestParams: RequestBuilders.TileRequest,
        context: Context,
        completed: Int,
        missed: Int,
        missed_to_completed: Float
    ): TileBuilders.Tile {



        val singleTileTimeline = TimelineBuilders.Timeline.Builder()
            .addTimelineEntry(
                TimelineBuilders.TimelineEntry.Builder()
                    .setLayout(
                        LayoutElementBuilders.Layout.Builder()
                            .setRoot(
                                layout(
                                    context,
                                    requestParams.deviceConfiguration,
                                    completed,
                                    missed,
                                    missed_to_completed
                                )
                            )
                            .build()
                    )
                    .build()
            )
            .build()

        return TileBuilders.Tile.Builder()
            .setTileTimeline(singleTileTimeline)
            .build()
    }


    fun layout(
        context: Context,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
        completed: Int,
        missed: Int,
        @FloatRange(from = 0.0, to = 1.0) missed_to_completed: Float
    ): LayoutElementBuilders.LayoutElement {
        val angle = 0f

        return EdgeContentLayout.Builder(deviceParameters)
            .setResponsiveContentInsetEnabled(true)
            .setEdgeContent(
                CircularProgressIndicator.Builder()
                    .setProgress(missed_to_completed)
                    .setCircularProgressIndicatorColors(
                        ProgressIndicatorColors(
                            ColorBuilders.argb(0xFFFF0000.toInt()),
                            ColorBuilders.argb(0xFF00FF00.toInt())
                        )
                    )
                    .setStartAngle(-1 * angle)
                    .setEndAngle(angle)

                    .build()
            )
            .setEdgeContentThickness(2.5f)
//            .setEdgeContentBehindAllOtherContent(true)
            .setContentAndSecondaryLabelSpacing(DimensionBuilders.dp(0f))

            .setPrimaryLabelTextContent(
                Text.Builder(context, "Intervention Statistik")
                    .setMaxLines(2)
                    .setTypography(Typography.TYPOGRAPHY_TITLE3)
                    .setMultilineAlignment(LayoutElementBuilders.TEXT_ALIGN_CENTER)
                    .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                    .build()
            )
            .setSecondaryLabelTextContent(
                Text.Builder(
                    context,
                    if (missed_to_completed <= 0.5f) "Weiter so!" else "Das geht besser"
                )
                    .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                    .setColor(
                        ColorBuilders.argb(
                            if (missed_to_completed <= 0.5f) 0xFFFFFFFF.toInt() else 0xFFFF0000.toInt()
                        )
                    )
                    .build()
            )
            .setContent(
                MultiSlotLayout.Builder()
                    .addSlotContent(
                        Column.Builder()
                            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                            .addContent(
                                Text.Builder(context, completed.toString())
                                    .setTypography(Typography.TYPOGRAPHY_DISPLAY1)
                                    .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                                    .build()
                            )
                            .addContent(
                                Text.Builder(context, "gemacht")
                                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                                    .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                                    .build()
                            )
                            .build()
                    )
                    .addSlotContent(
                        LayoutElementBuilders.Column.Builder()
                            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                            .addContent(
                                Text.Builder(context, missed.toString())
                                    .setTypography(Typography.TYPOGRAPHY_DISPLAY1)
                                    .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                                    .build()
                            )
                            .addContent(
                                Text.Builder(context, "ignoriert")
                                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                                    .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            ).build()
    }

}