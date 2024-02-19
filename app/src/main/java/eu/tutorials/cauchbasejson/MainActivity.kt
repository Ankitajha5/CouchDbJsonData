package eu.tutorials.cauchbasejson

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfigurationFactory
import com.couchbase.lite.Expression
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import com.couchbase.lite.newConfig
import com.google.gson.Gson



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CouchbaseLite.init(this)

        // Initialize Couchbase Lite
        val database = Database("MyDatabase", DatabaseConfigurationFactory.newConfig())
        var collection = database.createCollection("CollectionName")
        collection = database.getCollection("CollectionName")!!
        collection = database.defaultScope.getCollection("CollectionName")!!

        // Create your JSON object (example using a Map)
        val jsonObject: Map<String, Any> = mapOf(
            "id" to "123",
            "name" to "John Doe",
            "age" to 30
        )

        // Convert JSON object to MutableDocument
        val gson = Gson()
        val mutableDocument = MutableDocument()
        val jsonString = gson.toJson(jsonObject)
        mutableDocument.setString("data", jsonString)
//        Log.d("mut", "checking document" + mutableDocument)
        collection.save(mutableDocument)


        val query = QueryBuilder
            .select(SelectResult.property("data"))
            .from(DataSource.collection(collection))
            .where(Expression.property("data").equalTo(Expression.string(jsonString)))



        query.execute().use { rs ->
            rs.allResults().forEach { result ->
                val jsonData = result.toMap()["data"]
                Log.d("json", "checking value $jsonData")
                if (jsonData != null) { // Handle potential null values
                    var detailsItem = DetailsItem("defaultId", "defaultName", 0)
                    processData(jsonData.toString(), detailsItem) // Call your parsing function
                }
                result.toMap().let {
                    Log.d("check", "data ->${it["data"]}")
                }

            }
        }

    }

    private fun processData(jsonData: String?, detailsItem: DetailsItem) {
        val gson = Gson()
        try {

            val parsedItem = gson.fromJson(jsonData, DetailsItem::class.java)
            // Use the parsed data (detailsItem)
            detailsItem.id = parsedItem.id ?: detailsItem.id
            detailsItem.name = parsedItem.name ?: detailsItem.name
            detailsItem.age = parsedItem.age

            findViewById<TextView>(R.id.id).text = "ID: ${detailsItem.id}"
            findViewById<TextView>(R.id.name).text = "Name: ${detailsItem.name}"
            findViewById<TextView>(R.id.age).text = "Age: ${detailsItem.age}"

            Log.d("ParsedData", "ID: ${detailsItem.id}, Name: ${detailsItem.name}, Age: ${detailsItem.age}")
        } catch (e: Exception) {
            Log.e("ParsingError", "Error parsing JSON: ${e.message}")
        }
    }
}







