package com.example.tse_emotionalrecognition.data.database.entities

import android.util.Log
import com.example.teamprojekttest.data.database.UserDatabase
import com.example.teamprojekttest.data.database.entities.HeartRateMeasurement
import com.example.teamprojekttest.data.database.entities.SkinTemperature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserRepository(db: UserDatabase) {
    private val affectDao = db.getAffectDao()
    private val skinTemperatureDao = db.getSkinTemperatureDao()
    private val heartRateMeassurementDao = db.getHeartRateMeassurementDao()

    fun insertHeartRateMeasurementList(
        scope: CoroutineScope,
        entities: MutableList<HeartRateMeasurement>,
        onFinished: (entity: MutableList<HeartRateMeasurement>) -> Unit = {}
    ) {
        scope.launch(Dispatchers.IO) {
            val listOfIds: List<Long> = heartRateMeassurementDao.insertAll(entities)
            entities.forEachIndexed() {index, element ->
                element.id = listOfIds[index]
            }
            withContext(Dispatchers.Main) {
                onFinished(entities)
            }
        }
    }

    fun insertSkinTemperatureList(
        scope: CoroutineScope,
        entities: MutableList<SkinTemperature>,
        onFinished: (entity: MutableList<SkinTemperature>) -> Unit = {}
    ) {
        scope.launch(Dispatchers.IO) {
            val listOfIds: List<Long> = skinTemperatureDao.insertAll(entities)
            entities.forEachIndexed() { index, element ->
                element.id = listOfIds[index]
            }
            withContext(Dispatchers.Main) {
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
            val entity = affectDao.getAffectById(id)

            Log.d("Affect", "updating $entity")

            Log.d("Affect", "affect id: $id")

            when (column) {
                AffectColumns.TIME_OF_ENGAGEMENT -> entity.timeOfEngagement = value as Long
                AffectColumns.TIME_OF_FINISHED -> entity.timeOfFinished = value as Long
                AffectColumns.AFFECT -> {
                    entity.affect =  value as AffectType
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

}