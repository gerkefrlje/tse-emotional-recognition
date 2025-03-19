package com.example.phone

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.example.phone.ui.theme.TSEEmotionalRecognitionTheme
import smile.classification.gbm
import smile.data.DataFrame
import smile.data.Tuple
import smile.data.formula.Formula
import smile.data.formula.Term
import smile.data.vector.ByteVector
import smile.data.vector.CharVector
import smile.data.vector.DoubleVector
import smile.data.vector.IntVector
import smile.data.vector.StringVector
import java.lang.reflect.Array.set

class MLTest : ComponentActivity() {

    private val values = arrayOf(
        doubleArrayOf(75.0, 76.0, 77.0, 78.0, 79.0),
        doubleArrayOf(0.1, 0.6, 0.0, 0.7, 0.1)
    )

    private val newValues = arrayOf(
        doubleArrayOf(75.0, 76.0, 77.0, 78.0, 79.0),
        doubleArrayOf(0.1, 0.6, 0.0, 0.7, 0.1))

    private val label = intArrayOf(1, 1, 0, 1, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = DataFrame.of(
            DoubleVector.of("hr" ,values[0]),
            DoubleVector.of("st", values[1]),
            ByteVector.of("label", label.map { it.toByte() }.toByteArray()) ) // Label als ByteVector            )


        Log.d("MLTest", "DataFrame columns: ${data.names().contentToString()}")
        val formula = Formula.lhs("label")
        //val predictors = formula.predictors()

        gemini()

        //Log.d("MLTest", "Predictors: ${predictors.contentToString()}")


        val model = gbm(formula, data)


        var predictionText = ""

        setContent {
            TSEEmotionalRecognitionTheme {
                Box {
                    Button(
                        onClick = {
                            val newData = DataFrame.of(
                                DoubleVector.of("hr", newValues[0]),
                                DoubleVector.of("st", newValues[1])
                            )

                            val newDataArray = doubleArrayOf(75.0, 0.69)

                            val prediction = model.predict(newData)

                            Log.d("MLTest", "Prediction: $prediction")
                        }
                    ) {

                        Text(predictionText)
                    }
                }
            }
        }

    }

    fun gemini(){
        val data = DataFrame.of(
            DoubleVector.of("feature1", doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0)),
            DoubleVector.of("feature2",doubleArrayOf(2.1, 3.9, 5.2, 6.8, 8.1)),
            IntVector.of("target", intArrayOf(1,2, 3, 1, 2))
        )
        // Die Formel definieren, die die Zielvariable und die Prädiktoren angibt
        val formula = Formula.lhs("target")

        try {
            // Das Gradient Boosting Modell trainieren
            val gbt = gbm(
                formula = formula,
                data = data,
                ntrees = 100,
                maxDepth = 5,
                shrinkage = 0.1,
                subsample = 0.8
            )

            Log.d("MLTest", "Gradient Boosting Modell erfolgreich trainiert.")
            Log.d("MLTest", "Anzahl der Bäume: ${gbt.size()}")


            val newFeature1 = doubleArrayOf(1.5, 4.5)
            val newFeature2 = doubleArrayOf(3.0, 7.5)
            val dummyTarget = IntArray(newFeature1.size) { 0 } // Initialisieren mit 0 oder einem anderen Standardwert


            val newData = DataFrame.of(
                DoubleVector.of("feature1", newFeature1),
                DoubleVector.of("feature2", newFeature2),
                IntVector.of("target", dummyTarget)
            )

            // --- Vorhersagen treffen ---
            val predictions = gbt.predict(newData)

            Log.d("MLTest", "Vorhersagen")
            for (prediction in predictions) {
                Log.d("MLTest", "Vorhersage: $prediction")
            }


            // Beispiel für die Vorhersage (erfordert mehr Daten und eine entsprechende Implementierung in Smile)
            // Hier demonstrieren wir nur, wie das trainierte Modell verwendet werden könnte.
            // In einer echten Anwendung müssten Sie neue Daten vorbereiten und die Vorhersagemethode des Modells aufrufen.

            // Hinweis: Dieser Code ist ein illustratives Beispiel.
            // Die 'smile' Bibliothek und ihre spezifischen Klassen und Methoden müssten in Ihrem Projekt eingebunden sein,
            // und die Daten müssten das richtige Format für die 'gbm'-Funktion haben.
        } catch (e: Exception) {
            println("Fehler beim Trainieren des Gradient Boosting Modells: ${e.message}")
            e.printStackTrace()
        }

    }
}