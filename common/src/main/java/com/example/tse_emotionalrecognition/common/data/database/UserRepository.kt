package com.example.tse_emotionalrecognition.common.data.database

import android.util.Log
import com.example.tse_emotionalrecognition.common.data.database.entities.AffectData
import com.example.tse_emotionalrecognition.common.data.database.entities.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserRepository(db: com.example.tse_emotionalrecognition.common.data.database.UserDatabase) {

    val affectDao = db.getAffectDao()
    val sessionDao = db.getSessionDao()
    val heartRateDao = db.getHeartRateMeasurementDao()
    val skinTemperatureDao = db.getSkinTemperatureMeasurementDao()
    val interventionStatsDao = db.getInterventionStatsDao()

    fun getActiveSession(
        scope: CoroutineScope,
        onFinished: (entity: com.example.tse_emotionalrecognition.common.data.database.entities.SessionData) -> Unit,
        onError: (e: Exception) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val result = sessionDao.getActiveSession()
                withContext(Dispatchers.Main) {
                    onFinished(result)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    suspend fun getHeartRateMeasurements(): List<com.example.tse_emotionalrecognition.common.data.database.entities.HeartRateMeasurement> {
        return try {
            heartRateDao.getAll()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getSkinTemperatureMeasurements(): List<com.example.tse_emotionalrecognition.common.data.database.entities.SkinTemperatureMeasurement> {
        return try {
            skinTemperatureDao.getAll()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun insertHeartRateMeasurementList(
        scope: CoroutineScope,
        entities: MutableList<com.example.tse_emotionalrecognition.common.data.database.entities.HeartRateMeasurement>,
        onFinished: (entity: MutableList<com.example.tse_emotionalrecognition.common.data.database.entities.HeartRateMeasurement>) -> Unit = {}
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val listOfIds = heartRateDao.insertAll(entities)
                entities.forEachIndexed() { index, element ->
                    element.id = listOfIds[index]
                }
                withContext(Dispatchers.IO) {
                    onFinished(entities)
                }
            }catch (
                e: Exception
            ){
                Log.e("insertHeartRateMeasurementList", "Error inserting heart rate measurements", e)
            }

        }
    }

    fun insertSkinTemperatureMeasurementList(
        scope: CoroutineScope,
        entities: MutableList<com.example.tse_emotionalrecognition.common.data.database.entities.SkinTemperatureMeasurement>,
        onFinished: (entity: MutableList<com.example.tse_emotionalrecognition.common.data.database.entities.SkinTemperatureMeasurement>) -> Unit = {}
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val listOfIds = skinTemperatureDao.insertAll(entities)
                entities.forEachIndexed() { index, element ->
                    element.id = listOfIds[index]
                }
                withContext(Dispatchers.IO) {
                    onFinished(entities)
                }
            }catch (e: Exception){
                Log.e("insertSkinTemperatureMeasurementList", "Error inserting skin temperature measurements", e)
            }
        }
    }

    fun insertAffect(
        scope: CoroutineScope,
        entity: com.example.tse_emotionalrecognition.common.data.database.entities.AffectData,
        onFinished: (entity: com.example.tse_emotionalrecognition.common.data.database.entities.AffectData) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            entity.id = affectDao.insert(entity)
            Log.v("insertAffect", "$entity")
            withContext(Dispatchers.IO) {
                Log.v("Affect", "created ${entity.toString()}")
                onFinished(entity)
            }
        }
    }

    fun deleteAffect(
        scope: CoroutineScope,
        id: Long,
        onFinished: (entity: Long) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            affectDao.deleteAffectById(id)
            withContext(Dispatchers.IO) {
                Log.v("Affect", "deleted $id")
                onFinished(id)
            }
        }
    }

        fun updateAffectColumn(
            scope: CoroutineScope,
            id: Long,
            column: com.example.tse_emotionalrecognition.common.data.database.entities.AffectColumns,
            value: Any,
            onFinished: (entity: com.example.tse_emotionalrecognition.common.data.database.entities.AffectData) -> Unit = {}
        ) {
            scope.launch(Dispatchers.IO) {
                val entity = affectDao.getAffectById(id)
                when (column) {
                    com.example.tse_emotionalrecognition.common.data.database.entities.AffectColumns.TIME_OF_ENGAGEMENT -> entity.timeOfEngagement =
                        value as Long

                    com.example.tse_emotionalrecognition.common.data.database.entities.AffectColumns.TIME_OF_FINISHED -> entity.timeOfFinished =
                        value as Long

                    com.example.tse_emotionalrecognition.common.data.database.entities.AffectColumns.AFFECT -> {
                        entity.affect =
                            value as com.example.tse_emotionalrecognition.common.data.database.entities.AffectType
                        entity.timeOfAffect = System.currentTimeMillis()
                    }
                }
                affectDao.insert(entity)
                withContext(Dispatchers.IO) {
                    Log.v("Affect", "updated to ${entity.toString()}")
                    onFinished(entity)
                }
            }
        }

        fun insertInterventionStats(
            scope: CoroutineScope,
            entity: com.example.tse_emotionalrecognition.common.data.database.entities.InterventionStats,
            onFinished: (entity: com.example.tse_emotionalrecognition.common.data.database.entities.InterventionStats) -> Unit = {}
            ) {
            scope.launch(Dispatchers.IO) {
                entity.id = interventionStatsDao.insert(entity)
                withContext(Dispatchers.Main) {
                    Log.v("InterventionStats", "created to ${entity.toString()}")
                    onFinished(entity)
                }
            }
        }

        suspend fun getInterventionStatsByTag(tag: TAG) : com.example.tse_emotionalrecognition.common.data.database.entities.InterventionStats {
            return interventionStatsDao.getInterventionStatsByTag(tag)
        }

        suspend fun getInterventionStatsById(id: Long) : com.example.tse_emotionalrecognition.common.data.database.entities.InterventionStats{
            return interventionStatsDao.getInterventionStatsById(id)
        }


        fun incrementTriggered(
            scope: CoroutineScope,
            id: Long,
            onFinished: ((entity: com.example.tse_emotionalrecognition.common.data.database.entities.InterventionStats) -> Unit)? = null
        ){
            scope.launch(Dispatchers.IO) {
                val entity = interventionStatsDao.getInterventionStatsById(id)
                if(entity != null) {
                    entity.triggeredCount++

                    interventionStatsDao.update(entity)
                    Log.v("InterventionStats", "updated to ${entity.toString()}")
                    onFinished?.invoke(entity)

                }

            }
        }

        fun incrementDismissed(
            scope: CoroutineScope,
            id: Long,
            onFinished: ((entity: com.example.tse_emotionalrecognition.common.data.database.entities.InterventionStats) -> Unit)? = null
        ) {
            scope.launch(Dispatchers.IO) {
                val entity = interventionStatsDao.getInterventionStatsById(id)
                if(entity != null) {
                    entity.dismissedCount++
                    interventionStatsDao.update(entity)
                    Log.v("InterventionStats", "updated to ${entity.toString()}")
                    onFinished?.invoke(entity)

                }

            }
        }


        fun insertSession(
            scope: CoroutineScope,
            entity: com.example.tse_emotionalrecognition.common.data.database.entities.SessionData,
            onFinished: (entity: com.example.tse_emotionalrecognition.common.data.database.entities.SessionData) -> Unit
        ) {
            scope.launch(Dispatchers.IO) {
                entity.id = sessionDao.insert(entity)
                withContext(Dispatchers.Main) {
                    Log.v("Session", "created to ${entity.toString()}")
                    onFinished(entity)
                }
            }
        }

        fun updateSessionColumn(
            scope: CoroutineScope,
            id: Long,
            column: com.example.tse_emotionalrecognition.common.data.database.entities.SessionDataColumns,
            value: Any,
            onFinished: (entity: com.example.tse_emotionalrecognition.common.data.database.entities.SessionData) -> Unit
        ) {
            scope.launch(Dispatchers.IO) {
                val entity = sessionDao.getSessionById(id)
                when (column) {
                    com.example.tse_emotionalrecognition.common.data.database.entities.SessionDataColumns.START_TIME_MILLIS -> entity.startTimeMillis =
                        value as Long

                    com.example.tse_emotionalrecognition.common.data.database.entities.SessionDataColumns.END_TIME_MILLIS -> entity.endTimeMillis =
                        value as Long

                    com.example.tse_emotionalrecognition.common.data.database.entities.SessionDataColumns.SYNCED -> entity.synced =
                        value as Long

                    com.example.tse_emotionalrecognition.common.data.database.entities.SessionDataColumns.COUNT -> entity.count =
                        value as Int
                }
                sessionDao.insert(entity)
                withContext(Dispatchers.IO) {
                    Log.v("Session", "updated to ${entity.toString()}")
                    onFinished(entity)
                }
            }
        }

    }