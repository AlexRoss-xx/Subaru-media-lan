/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.models

import android.content.Context
import androidx.paging.DataSource
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.lang.Exception


@Entity
data class Vehicle(
        @PrimaryKey val uid: Int,
        @ColumnInfo(name = "motor_hours")
        val motorHours: Long
)

@Entity(primaryKeys = arrayOf("fuel_consumption", "added_at"))
data class FuelConsumption(

        @ColumnInfo(name = "fuel_consumption")
        var fuel_consumption: Float,

        @ColumnInfo(name = "added_at")
        var added_at: Long

) {

}

class fcData(var rowid: Int, var fuel_consumption: Float, var added_at: Long)


@Dao
interface VehicleDao {
    @Query("SELECT  * FROM Vehicle")
    fun getVehicles(): List<Vehicle>

    @Query("SELECT * FROM Vehicle WHERE uid = (:vehicleId)")
    fun loadById(vehicleId: Int): Vehicle

    @Insert()
    fun insert(vararg Vehicls: Vehicle)

    @Update
    fun update(vehicle: Vehicle)

    @Query("UPDATE Vehicle  SET motor_hours = motor_hours+(:mh)   WHERE uid =1")
    fun updateMH(mh: Long)

    @Query("UPDATE Vehicle  SET motor_hours = 0   WHERE uid =1")
    fun clearMH()

    @Insert()
    fun insertFuelConsumption(vararg FuelConsumption: FuelConsumption)

    @Query("SELECT   rowid  , fuel_consumption, added_at FROM FuelConsumption ORDER BY  added_at DESC LIMIT 100 ")
    fun getFuelConsumption(): List<fcData>

    @Query("SELECT   AVG(fuel_consumption) FROM FuelConsumption")
    fun getAvgFuelConsumption(): Float

    @Query("DELETE FROM FuelConsumption")
    fun clearFuelConsumption()
}

@Database(entities = arrayOf(Vehicle::class, FuelConsumption::class), version = 2, exportSchema =true)
abstract class VehicleDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao

    companion object {

        @Volatile
        private var INSTANCE: VehicleDatabase? = null

        fun getInstance(context: Context): VehicleDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        VehicleDatabase::class.java, "Sample.db").allowMainThreadQueries()
                        // prepopulate the database after onCreate was called
                        .addCallback(object : Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                // insert the data on the IO Thread
                                //   Executors.newSingleThreadScheduledExecutor().execute(Runnable {   getInstance(context).vehicleDao().insert(PREPOPULATE_DATA)})
                            }
                        })
                        .addMigrations(MIGRATION_1_2)
                        .build()

        val PREPOPULATE_DATA = Vehicle(1, 0)

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS FuelConsumption (fuel_consumption REAL NOT NULL, added_at INTEGER NOT NULL ,  PRIMARY KEY(fuel_consumption,added_at)) ")
            }
        }

    }
}

class VehicleRepository(private val vehicleDao: VehicleDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.

    fun vehicle(): Vehicle {
        if (vehicleDao.loadById(1) == null)
            vehicleDao.insert(Vehicle(1, 0))
        return vehicleDao.loadById(1)
    }

    fun update(mh: Long) {
        vehicleDao.updateMH(mh)
    }

    fun clear() {
        vehicleDao.clearMH()
        vehicleDao.clearFuelConsumption()
    }


    fun insertFuelConsumption(f: Float) {
        var fc: FuelConsumption = FuelConsumption(f, System.currentTimeMillis() / 100)
        try {
            vehicleDao.insertFuelConsumption(fc)
        } catch (e:Exception)
        {}

    }

    fun getFuelConsumption(): List<fcData> {
        return vehicleDao.getFuelConsumption()
    }
    fun clearFuelConsumption(){
          vehicleDao.clearFuelConsumption()
    }

    fun getAvgFuelConsumption(): Float {
        return vehicleDao.getAvgFuelConsumption()
    }
}