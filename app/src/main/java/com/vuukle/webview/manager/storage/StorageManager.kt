package litparty.app.manager.storage

interface StorageManager {

    fun putData(key: String, value: String)

    fun putData(key: String, value: Int)

    fun putData(key: String, value: Boolean)

    fun putData(key: String, value: Float)

    fun getBooleanData(key: String): Boolean?

    fun getFloatData(key: String): Float?

    fun getIntData(key: String): Int?

    fun getStringData(key: String): String?
}