package com.example.tse_emotionalrecognition.tile

import android.content.Context
import android.service.quicksettings.Tile
import android.util.Log
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.ARC_DIRECTION_NORMAL
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.TypeBuilders
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.EventBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.tse_emotionalrecognition.common.data.database.UserDataStore
import com.example.tse_emotionalrecognition.common.data.database.entities.TAG
import com.example.tse_emotionalrecognition.presentation.MainActivity
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val RESOURCES_VERSION = "0"

@OptIn(ExperimentalHorologistApi::class)
class InterventionStatsTileService : SuspendingTileService() {

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ) = resources()

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val tile = tile(requestParams, this)
        Log.d("TileDebug", "Tile erstellt: $tile")
        return tile
    }

    override fun onTileEnterEvent(requestParams: EventBuilders.TileEnterEvent) {
        Log.d("TileDebug", "Tile wurde betätigt: $requestParams")
        getUpdater(this).requestUpdate(InterventionStatsTileService::class.java)
    }

}

private fun resources(): ResourceBuilders.Resources {
    return ResourceBuilders.Resources.Builder()
        .setVersion(RESOURCES_VERSION)
        .build()
}

@Preview(WearDevices.SMALL_ROUND)
fun preview(context: Context) = TilePreviewData() {
    previewTile(context)
}

private fun previewLayout(context: Context): LayoutElementBuilders.LayoutElement {
    // Dummy Daten für die Vorschau
    val completed = 5
    val missed = 2

    val donutArc = createDonutArc(completed, missed)
    val titleText = createText("Completed/Missed")

    return PrimaryLayout.Builder(DeviceParametersBuilders.DeviceParameters.Builder().build()).setResponsiveContentInsetEnabled(true)
        .setResponsiveContentInsetEnabled(true)
        .setContent(
            LayoutElementBuilders.Column.Builder()
                .addContent(titleText)
                .addContent(donutArc)
                .build()
        )
        .build()
}

fun previewTile(context: Context): TileBuilders.Tile {
    val singleTileTimeline = TimelineBuilders.Timeline.Builder()
        .addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder()
                .setLayout(
                    LayoutElementBuilders.Layout.Builder()
                        .setRoot(
                            previewLayout(
                                context
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

private suspend fun tile(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
): TileBuilders.Tile {
    val layout = try {
        tileLayout(requestParams, context)
    } catch (e: Exception) {
        Log.e("TileDebug", "Fehler beim Laden des Layouts", e)
        createTilePreview(requestParams) // Falls Fehler auftreten, nutze Dummy-Layout
    }

    val singleTileTimeline = TimelineBuilders.Timeline.Builder()
        .addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder()
                .setLayout(
                    LayoutElementBuilders.Layout.Builder()
                        .setRoot(layout)
                        .build()
                )
                .build()
        )
        .build()

    return TileBuilders.Tile.Builder()
        .setResourcesVersion(RESOURCES_VERSION)
        .setTileTimeline(singleTileTimeline)
        .build()
}

private suspend fun tileLayout(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
): LayoutElementBuilders.LayoutElement = withContext(Dispatchers.IO) {
    val userRepository = UserDataStore.getUserRepository(context)
    val interventionStats = userRepository.getInterventionStatsById(MainActivity.trackerID)

    val completed = interventionStats.triggeredCount ?: 3  // Default-Werte für Debugging
    val missed = interventionStats.dismissedCount ?: 2

    Log.d("TileDebug", "Daten geladen: completed=$completed, missed=$missed")

    val donutArc = createDonutArc(completed, missed)

    val titleText = createText("Completed/Missed")
    //val missedText = createText("Missed")

    PrimaryLayout.Builder(requestParams.deviceConfiguration)
        .setContent(
            LayoutElementBuilders.Column.Builder()
                .addContent(titleText)
                .addContent(donutArc)
                .build()
        )
        .build()
}

/**
 * Erstellt den Donut Arc für abgeschlossene & verpasste Interventionen.
 */
private fun createDonutArc(completed: Int, missed: Int): LayoutElementBuilders.Arc {
    val total = completed + missed
    if (total == 0) {
        Log.d("TileDebug", "Kein Fortschritt vorhanden")
        return LayoutElementBuilders.Arc.Builder().build()
    }

    val completedFraction = completed / total.toFloat()
    val completedAngle = (completedFraction * 360f).coerceIn(0f, 359.9f)

    Log.d("TileDebug", "Donut Arc erstellt: completedAngle=$completedAngle")

    return LayoutElementBuilders.Arc.Builder()
        .addContent(
            LayoutElementBuilders.ArcLine.Builder()
                .setLength(DimensionBuilders.DegreesProp.Builder().setValue(completedAngle).build())
                .setThickness(DimensionBuilders.DpProp.Builder().setValue(20f).build())
                .setColor(argb(0xFF00FF00.toInt())) // Grün für abgeschlossene Interventionen
                .build()
        )
        .addContent(
            LayoutElementBuilders.ArcLine.Builder()
                .setLength(DimensionBuilders.DegreesProp.Builder().setValue(360f - completedAngle).build())
                .setThickness(DimensionBuilders.DpProp.Builder().setValue(20f).build())
                .setColor(argb(0xFFFF0000.toInt())) // Rot für verpasste Interventionen
                .build()
        )
        .build()
}

private fun createText(text: String): LayoutElementBuilders.Text {
    return LayoutElementBuilders.Text.Builder()
        .setText(text) // Stil anwenden
        .build()
}

/**
 * Falls Fehler auftreten, wird ein einfaches Layout für die Vorschau erstellt.
 */
private fun createTilePreview(requestParams: RequestBuilders.TileRequest): LayoutElementBuilders.LayoutElement {
    Log.d("TileDebug", "Erstelle Tile-Vorschau")

    val previewArc = createDonutArc(4, 1) // Feste Werte für Vorschau

    return PrimaryLayout.Builder(requestParams.deviceConfiguration).setResponsiveContentInsetEnabled(true)
        .setContent(previewArc)
        .build()
}

