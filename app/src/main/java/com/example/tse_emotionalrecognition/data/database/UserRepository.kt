package com.example.tse_emotionalrecognition.data.database

import android.util.Log
import com.example.tse_emotionalrecognition.data.database.entities.AffectColumns
import com.example.tse_emotionalrecognition.data.database.entities.AffectData
import com.example.tse_emotionalrecognition.data.database.entities.AffectType
import com.example.tse_emotionalrecognition.data.database.entities.HeartRateMeasurement
import com.example.tse_emotionalrecognition.data.database.entities.SkinTemperatureMeasurement
import com.example.tse_emotionalrecognition.data.database.entities.SessionData
import com.example.tse_emotionalrecognition.data.database.entities.SessionDataColumns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserRepository(db: UserDatabase) {

    val affectDao = db.getAffectDao()
    val sessionDao = db.getSessionDao()
    val heartRateDao = db.getHeartRateMeasurementDao()
    val skinTemperatureDao = db.getSkinTemperatureMeasurementDao()

    fun getActiveSession(
        scope: CoroutineScope,
        onFinished: (entity: SessionData) -> Unit,
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

    fun insertHeartRateMeasurementList(
        scope: CoroutineScope,
        entities: MutableList<HeartRateMeasurement>,
        onFinished: (entity: MutableList<HeartRateMeasurement>) -> Unit = {}
    ) {
        scope.launch(Dispatchers.IO) {
            val listOfIds = heartRateDao.insertAll(entities)
            entities.forEachIndexed() {index, element ->
                element.id = listOfIds[index]
            }
            withContext(Dispatchers.IO) {
                onFinished(entities)
            }
        }
    }

    fun insertSkinTemperatureMeasurementList(
        scope: CoroutineScope,
        entities: MutableList<SkinTemperatureMeasurement>,
        onFinished: (entity: MutableList<SkinTemperatureMeasurement>) -> Unit = {}
    ) {
        scope.launch(Dispatchers.IO) {
            val listOfIds = skinTemperatureDao.insertAll(entities)
            entities.forEachIndexed() {index, element ->
                element.id = listOfIds[index]
            }
            withContext(Dispatchers.IO) {
                onFinished(entities)
            }
        }
    }

    fun insertAffect(
        scope: CoroutineScope,
        entity: AffectData,
        onFinished: (entity: AffectData) -> Unit
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
    fun updateAffectColumn(
        scope: CoroutineScope,
        id: Long,
        column: AffectColumns,
        value: Any,
        onFinished: (entity: AffectData) -> Unit = {}
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val entity = affectDao.getAffectById(id)
                if (entity != null) {
                    when (column) {
                        AffectColumns.TIME_OF_ENGAGEMENT -> {
                            if (value is Long) {
                                entity.timeOfEngagement = value
                            } else {
                                Log.e("Affect", "Invalid type for TIME_OF_ENGAGEMENT: ${value.javaClass.simpleName}")
                            }
                        }
                        AffectColumns.TIME_OF_FINISHED -> {
                            if (value is Long) {
                                entity.timeOfFinished = value
                            } else {
                                Log.e("Affect", "Invalid type for TIME_OF_FINISHED: ${value.javaClass.simpleName}")
                            }
                        }
                        AffectColumns.AFFECT -> {
                            if (value is AffectType) {
                                entity.affect = value
                                entity.timeOfAffect = System.currentTimeMillis()
                            } else {
                                Log.e("Affect", "Invalid type for AFFECT: ${value.javaClass.simpleName}")
                            }
                        }
                    }
                    affectDao.insert(entity)
                    Log.v("Affect", "updated to ${entity.toString()}")
                    onFinished(entity)
                } else {
                    Log.e("Affect", "AffectData with ID $id not found")
                }
            } catch (e: Exception) {
                Log.e("Affect", "Error updating AffectData: ${e.message}")
            }
        }
    }

    fun insertSession(
        scope: CoroutineScope,
        entity: SessionData,
        onFinished: (entity: SessionData) -> Unit
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
        column: SessionDataColumns,
        value: Any,
        onFinished: (entity: SessionData) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            val entity= sessionDao.getSessionById(id)
            when (column) {
                SessionDataColumns.START_TIME_MILLIS -> entity.startTimeMillis = value as Long
                SessionDataColumns.END_TIME_MILLIS -> entity.endTimeMillis = value as Long
                SessionDataColumns.SYNCED -> entity.synced = value as Long
                SessionDataColumns.COUNT -> entity.count = value as Int
            }
            sessionDao.insert(entity)
            withContext(Dispatchers.IO) {
                Log.v("Session", "updated to ${entity.toString()}")
                onFinished(entity)
            }
        }
    }

}