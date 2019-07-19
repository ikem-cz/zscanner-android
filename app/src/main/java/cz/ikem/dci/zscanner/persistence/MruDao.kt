package cz.ikem.dci.zscanner.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import cz.ikem.dci.zscanner.MAX_MRUS

@Dao
interface MruDao {

    @Query("select * from mru order by timestamp desc")
    fun mru(): LiveData<List<Mru>>

    @Insert
    fun insert(mru: Mru)

    @Delete
    fun delete(mru: Mru)

    @Query("delete from mru")
    fun deleteAll()

    @Query("delete from mru where bid = :bid")
    fun deleteByBid(bid: String)

    @Query("select count(*) from mru")
    fun count(): Int

    @Query("select * from mru order by timestamp asc limit 1")
    fun getOldest(): Mru

    @Transaction
    fun initializeIfEmpty(mrus: List<Mru>) {
        val cnt = count()
        if (cnt == 0) {
            deleteAll()
            mrus.forEach {
                insert(it)
            }
        }
    }

    @Transaction
    fun smartInsert(mru: Mru) {
        if (mru.id != null) {
            deleteByBid(mru.bid!!)
        }
        insert(mru)
        while (count() > MAX_MRUS) {
            val oldest = getOldest()
            delete(oldest)
        }
    }

}